import configparser


class Settings:
    def __init__(self, path):
        config = configparser.ConfigParser()
        config.read(path)
        self.cdrRuleEndpoint = config['ENDPOINTS']['cdrRuleEndpoint']
        self.billRuleEndpoint = config['ENDPOINTS']['billRuleEndpoint']
        self.forecastEndpoint = config['ENDPOINTS']['udrEndpoint']
        self.billEndpoint = config['ENDPOINTS']['billEndpoint']
        self.dbuser = config['DB']['user']
        self.dbpass = config['DB']['password']
        self.dbhost = config['DB']['host']
        self.dbport = config['DB']['port']
        self.udrdb = config['DB']['cyclops_udr']
        self.cdrdb = config['DB']['cyclops_cdr']
        self.billdb = config['DB']['cyclops_billing']

