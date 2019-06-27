import psycopg2


class DBAccess:
    def __init__(self, settings):
        self.settings = settings

    def printbill(self, target):
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
            print("Account = ", row[0], )
            print("Charge = ", row[1])
            print("Currency  = ", row[2], "\n")
        cursor.close()
        connection.close()

    def deleterule(self, db, target):
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

