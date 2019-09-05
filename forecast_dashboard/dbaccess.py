import psycopg2
import requests



class DBAccess:
    def __init__(self, settings):
        self.settings = settings

    def printbill(self, target):
        bills = {}
        connection = psycopg2.connect(user=self.settings.dbuser,
                                      password=self.settings.dbpass,
                                      host=self.settings.dbhost,
                                      port=self.settings.dbport,
                                      database=self.settings.billdb)
        cursor = connection.cursor()
        postgreSQL_select_Query = "select account,charge,currency from bill where " \
                                  "account like '%{}%'".format(target)
        cursor.execute(postgreSQL_select_Query)
        records = cursor.fetchall()
        for row in records:
            bills[row[0]] = row[1]
        cursor.close()
        connection.close()
        return bills

    def printaccounts(self):
        accounts = []
        connection = psycopg2.connect(user=self.settings.dbuser,
                                      password=self.settings.dbpass,
                                      host=self.settings.dbhost,
                                      port=self.settings.dbport,
                                      database=self.settings.udrdb)
        cursor = connection.cursor()
        postgreSQL_select_Query = "select distinct account from usage"
        cursor.execute(postgreSQL_select_Query)
        records = cursor.fetchall()
        for row in records:
            accounts.append(row[0])
        cursor.close()
        connection.close()
        return accounts

    def printcdrrules(self):
        rules = {}
        connection = psycopg2.connect(user=self.settings.dbuser,
                                      password=self.settings.dbpass,
                                      host=self.settings.dbhost,
                                      port=self.settings.dbport,
                                      database=self.settings.cdrdb)
        cursor = connection.cursor()
        postgreSQL_select_Query = "select name,rule from instanceorm"
        cursor.execute(postgreSQL_select_Query)
        records = cursor.fetchall()
        for row in records:
            rules[row[0]] = row[1]
        cursor.close()
        connection.close()
        return rules

    def printbillrules(self):
        rules = {}
        connection = psycopg2.connect(user=self.settings.dbuser,
                                      password=self.settings.dbpass,
                                      host=self.settings.dbhost,
                                      port=self.settings.dbport,
                                      database=self.settings.billdb)
        cursor = connection.cursor()
        postgreSQL_select_Query = "select name,rule from instanceorm"
        cursor.execute(postgreSQL_select_Query)
        records = cursor.fetchall()
        for row in records:
            rules[row[0]] = row[1]
        cursor.close()
        connection.close()
        return rules

    def deleterule(self, db, target):
        connection = psycopg2.connect(user=self.settings.dbuser,
                                      password=self.settings.dbpass,
                                      host=self.settings.dbhost,
                                      port=self.settings.dbport,
                                      database=db)
        cursor = connection.cursor()
        postgreSQL_select_Query = "select * from instanceorm where " \
                                  "name like '{}'".format(target)
        cursor.execute(postgreSQL_select_Query)
        records = cursor.fetchall()
        if db == self.settings.cdrdb:
            for row in records:
                print(row[0])
                requests.delete(self.settings.cdrRuleEndpoint + '/' + str(row[0]))
        if db == self.settings.billdb:
            for row in records:
                print(row[0])
                requests.delete(self.settings.billRuleEndpoint + '/' + str(row[0]))
        cursor.close()
        connection.close()
        connection = psycopg2.connect(user=self.settings.dbuser,
                                      password=self.settings.dbpass,
                                      host=self.settings.dbhost,
                                      port=self.settings.dbport,
                                      database=db)
        cursor = connection.cursor()
        postgreSQL_delete_Query = "delete from instanceorm where " \
                                  "name like '{}'".format(target)
        cursor.execute(postgreSQL_delete_Query)
        connection.commit()
        count = cursor.rowcount
        print(count, "rules deleted successfully ")
        cursor.close()
        connection.close()
        return count

    def deleteusage(self, target):
        connection = psycopg2.connect(user=self.settings.dbuser,
                                      password=self.settings.dbpass,
                                      host=self.settings.dbhost,
                                      port=self.settings.dbport,
                                      database=self.settings.udrdb)
        cursor = connection.cursor()
        postgreSQL_delete_Query = "delete from usage where " \
                                  "account like '{}'".format(target)
        cursor.execute(postgreSQL_delete_Query)
        connection.commit()
        count = cursor.rowcount
        print(count, "usage records deleted successfully ")
        cursor.close()
        connection.close()
        return count

    def deleteudr(self, target):
        connection = psycopg2.connect(user=self.settings.dbuser,
                                      password=self.settings.dbpass,
                                      host=self.settings.dbhost,
                                      port=self.settings.dbport,
                                      database=self.settings.udrdb)
        cursor = connection.cursor()
        postgreSQL_delete_Query = "delete from udr where " \
                                  "account like '{}'".format(target)
        cursor.execute(postgreSQL_delete_Query)
        connection.commit()
        count = cursor.rowcount
        print(count, "UDRs deleted successfully ")
        cursor.close()
        connection.close()
        return count

    def deletecdr(self, target):
        connection = psycopg2.connect(user=self.settings.dbuser,
                                      password=self.settings.dbpass,
                                      host=self.settings.dbhost,
                                      port=self.settings.dbport,
                                      database=self.settings.cdrdb)
        cursor = connection.cursor()
        postgreSQL_delete_Query = "delete from cdr where " \
                                  "account like '{}'".format(target)
        cursor.execute(postgreSQL_delete_Query)
        connection.commit()
        count = cursor.rowcount
        print(count, "CDRs deleted successfully ")
        cursor.close()
        connection.close()
        return count

    def deletebill(self, target):
        connection = psycopg2.connect(user=self.settings.dbuser,
                                      password=self.settings.dbpass,
                                      host=self.settings.dbhost,
                                      port=self.settings.dbport,
                                      database=self.settings.billdb)
        cursor = connection.cursor()
        postgreSQL_delete_Query = "delete from bill where " \
                                  "account like '{}'".format(target)
        cursor.execute(postgreSQL_delete_Query)
        connection.commit()
        count = cursor.rowcount
        print(count, "Bills deleted successfully ")
        cursor.close()
        connection.close()
        return count

