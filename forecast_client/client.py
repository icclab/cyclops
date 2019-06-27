import requests
import time
import settings
import dbaccess
import arguments


settings = settings.Settings('conf.ini')
dbaccess = dbaccess.DBAccess(settings)


def addrule(target, path):
    with open(path, 'r') as file:
        rule = file.read()
    if target == 'cdr':
        r = requests.post(settings.cdrRuleEndpoint, data=rule)
        print(r.status_code)
    elif target == 'bill':
        r = requests.post(settings.billRuleEndpoint, data=rule)
        print(r.status_code)
    print(rule)


def removerule(target, rule):
    if target == 'cdr':
        db = settings.cdrdb
    elif target == 'bill':
        db = settings.billdb
    dbaccess.deleterule(db, rule)


def generatesingleforecast(account, target, size):
    r = requests.post(settings.forecastEndpoint, json={"command": "Forecast",
                                                       "account": account,
                                                       "target": target,
                                                       "forecastSize": size})
    print(r.status_code)
    time.sleep(1)
    dbaccess.printbill(target)


def generateglobalforecast(target, size):
    r = requests.post(settings.forecastEndpoint, json={"command": "Forecast",
                                                       "target": target,
                                                       "forecastSize": size})
    print(r.status_code)
    time.sleep(1)
    dbaccess.printbill(target)


def generatepatternforecast(target, size):
    r = requests.post(settings.forecastEndpoint, json={"command": "GlobalForecast",
                                                       "target": target,
                                                       "forecastSize": size})
    print(r.status_code)
    time.sleep(1)
    dbaccess.printbill(target)


def cleanup(target, rules):
    if rules:
        dbaccess.deleterule(settings.cdrdb, "%{}%".format(target))
        dbaccess.deleterule(settings.billdb, "%{}%".format(target))
    dbaccess.deleteusage("%{}%".format(target))
    dbaccess.deleteudr("%{}%".format(target))
    dbaccess.deletecdr("%{}%".format(target))
    dbaccess.deletebill("%{}%".format(target))


if __name__ == '__main__':
    arguments = arguments.Arguments()
    args = arguments.args
    print("Estimation Engine Client\n")
    if args.command == 'rule':
        if args.delete:
            print("Deleting from {}, rule: {}".format(args.engine, args.target))
            removerule(args.engine, args.target)
        else:
            print("Adding to {}, from file: {}".format(args.engine, args.target))
            addrule(args.engine, args.target)
    elif args.command == 'cleanup':
        clear_rules = False
        if args.all:
            print("Also deleting rules")
            clear_rules = True
        print("cleaning up model: {}".format(args.target))
        cleanup(args.target, clear_rules)
    elif args.command == 'forecast':
        if args.forecast == 'single':
            print("Making forecast for account {} with model {} for {} days".format(args.account, args.model,
                                                                                    args.length))
            generatesingleforecast(args.account, args.model, args.length)
        elif args.forecast == 'global':
            print("Making global forecast with model {} for {} days".format(args.model, args.length))
            generateglobalforecast(args.model, args.length)
        elif args.forecast == 'pattern':
            print("Making pattern forecast with model {} for {} days".format(args.model, args.length))
            generatepatternforecast(args.model, args.length)
