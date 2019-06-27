"""
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 """
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
