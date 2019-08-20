import requests
import time
import settings
import dbaccess


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
    return rule


def listbills():
    r = requests.get(settings.billEndpoint)
    return r.json()


def listaccounts():
    return dbaccess.printaccounts()


def listcdrrules():
    return dbaccess.printcdrrules()


def listbillrules():
    return dbaccess.printbillrules()


def addrulestring(target, string):
    if target == 'cdr':
        r = requests.post(settings.cdrRuleEndpoint, data=string)
        print(r.status_code)
    elif target == 'bill':
        r = requests.post(settings.billRuleEndpoint, data=string)
        print(r.status_code)
    print(string)
    return string


def removerule(target, rule):
    if target == 'cdr':
        db = settings.cdrdb
    elif target == 'bill':
        db = settings.billdb
    return dbaccess.deleterule(db, rule)


def generatesingleforecast(account, target, size):
    r = requests.post(settings.forecastEndpoint, json={"command": "Forecast",
                                                       "account": account,
                                                       "target": target,
                                                       "forecastSize": size})
    print(r.status_code)
    time.sleep(1)
    return dbaccess.printbill(target)


def generateglobalforecast(target, size):
    r = requests.post(settings.forecastEndpoint, json={"command": "Forecast",
                                                       "target": target,
                                                       "forecastSize": size})
    print(r.status_code)
    time.sleep(1)
    return dbaccess.printbill(target)


def generatepatternforecast(target, size):
    r = requests.post(settings.forecastEndpoint, json={"command": "GlobalForecast",
                                                       "target": target,
                                                       "forecastSize": size})
    print(r.status_code)
    time.sleep(1)
    return dbaccess.printbill(target)


def cleanup(target, rules):
    cdr_rules = 0
    bill_rules = 0
    if rules:
        cdr_rules = dbaccess.deleterule(settings.cdrdb, "%{}%".format(target))
        bill_rules = dbaccess.deleterule(settings.billdb, "%{}%".format(target))
    usages = dbaccess.deleteusage("%{}%".format(target))
    udrs = dbaccess.deleteudr("%{}%".format(target))
    cdrs = dbaccess.deletecdr("%{}%".format(target))
    bills = dbaccess.deletebill("%{}%".format(target))
    return {"cdr_rules": cdr_rules, "Bill_rules": bill_rules, "usages": usages, "udrs": udrs, "cdrs": cdrs,
            "bills": bills}
