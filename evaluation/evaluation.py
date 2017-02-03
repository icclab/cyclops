import json
import re
import datetime
import subprocess
import time as timing
from time import strftime

import pika
import requests

from generate_usages import generate_usages


db_url = "http://localhost:8086"
udr_url = "http://localhost:4567"
bill_url = "http://localhost:4569"


def clean_measurement(measurement, database, url):
    print "Deleting measurement " + measurement + "..."
    queue = "q=DROP MEASUREMENT \"" + measurement + "\"&db=" + database
    requests.post(url+"/query?" + queue)


def parse_line(line, methods, database):
    parser = re.findall('\[(.*?)\]', line)
    for method in methods:
        if len(parser) == 3 and "/" + method + "?" in line and database in line:
            return {method: parser[2]}


def clean():
    clean_measurement("OpenStackCeilometerCpu", "cyclops.udr", db_url)
    clean_measurement("UDR", "cyclops.udr", db_url)
    clean_measurement("CDR", "cyclops.cdr", db_url)


class Evaluation:
    def __init__(self, number_of_records, size_of_metadata, unique_metadata, unique_users):
        self.number_of_records = number_of_records
        self.size_of_metadata = size_of_metadata
        self.unique_metadata = unique_metadata
        self.unique_users = unique_users
        self.start_time = timing.time()

    def print_start(self):
        print "started: " + str(timing.time() - self.start_time)

    def print_end(self):
        print "ended: " + str(timing.time() - self.start_time)

    @staticmethod
    def publish_data(body, endpoint):
        headers = {"Content-Type": "text/plain"}
        requests.post(endpoint+"/data", json.dumps(body), headers=headers)
        return

    def publish_usages_over_data(self, endpoint, batch_size=0):
        print "Publishing usages to " + endpoint + "..."
        queue = "q=CREATE DATABASE \"" + "test" + "\"&db=" + "_internal"
        requests.post(db_url + "/query?" + queue)
        body = generate_usages(self.number_of_records, self.size_of_metadata, self.unique_metadata, self.unique_users)
        if batch_size:
            from_time = strftime("%Y-%m-%d %H:%M:%S")
            lists = [body[x:x+batch_size] for x in xrange(0, len(body), batch_size)]
            self.print_start()
            print datetime.datetime.utcnow().isoformat()
            for one_list in lists:
                self.publish_data(one_list, endpoint)
            queue = "q=DROP DATABASE \"" + "test" + "\"&db=" + "_internal"
            requests.post(db_url + "/query?" + queue)
            self.monitor_dropped_database(from_time)
            return
        self.print_start()
        print datetime.datetime.utcnow().isoformat()
        self.publish_data(body, endpoint)
        from_time = strftime("%Y-%m-%d %H:%M:%S")
        self.monitor_database("cyclops.udr", from_time)
        return

    def publish_usage_over_queue(self, batch_size=0):
        print "Publishing usages to over rabbitmq"
        queue = "q=CREATE DATABASE \"" + "test" + "\"&db=" + "_internal"
        requests.post(db_url + "/query?" + queue)
        from_time = strftime("%Y-%m-%d %H:%M:%S")
        body = generate_usages(self.number_of_records, self.size_of_metadata, self.unique_metadata, self.unique_users)
        if batch_size:
            lists = [body[x:x + batch_size] for x in xrange(0, len(body), batch_size)]
            connection = pika.BlockingConnection(pika.ConnectionParameters(
                host='localhost'))
            channel = connection.channel()
            self.print_start()
            print datetime.datetime.utcnow().isoformat()
            for one_list in lists:
                channel.basic_publish(exchange='',
                                      routing_key='cyclops.udr.consume',
                                      body=json.dumps(one_list))
            queue = "q=DROP DATABASE \"" + "test" + "\"&db=" + "_internal"
            requests.post(db_url + "/query?" + queue)
            self.monitor_dropped_database(from_time)
            return
        self.print_start()
        print datetime.datetime.utcnow().isoformat()
        connection = pika.BlockingConnection(pika.ConnectionParameters(
            host='localhost'))
        channel = connection.channel()
        channel.basic_publish(exchange='',
                              routing_key='cyclops.udr.consume',
                              body=json.dumps(body))
        from_time = strftime("%Y-%m-%d %H:%M:%S")
        self.monitor_database("cyclops.udr", from_time)
        return

    def generate_udr(self, endpoint):
        timing.sleep(3)
        from_time = strftime("%Y-%m-%d %H:%M:%S")
        print "Generating UDR ..."
        body = {"_class": "GenerateUDR", "broadcast": True, "from": 0,  "to": self.number_of_records}
        headers = {"Content-Type": "text/plain"}
        self.print_start()
        print datetime.datetime.utcnow().isoformat()
        requests.post(endpoint + "/command", json.dumps(body), headers=headers)
        self.monitor_database("cyclops.udr", from_time)
        print "Generating CDR ..."
        self.print_start()
        print datetime.datetime.utcnow().isoformat()
        self.monitor_database("cyclops.cdr", from_time)

    def generate_bill(self, endpoint, users):
        print "Generating Bill ..."
        if type(users) == str:
            body = {"_class": "BillRequest", "from": 0,  "to": int(timing.time()), "account": users}
        else:
            body = {"_class": "BillRequest", "from": 0, "to": int(timing.time()), "account": users[0], "linked": users}
        headers = {"Content-Type": "text/plain"}
        self.print_start()
        print datetime.datetime.utcnow().isoformat()
        requests.post(endpoint + "/command", json.dumps(body), headers=headers)
        self.print_end()
        print datetime.datetime.utcnow().isoformat()

    def monitor_database(self, database, from_time, records=1):
        cmd = ["journalctl", '-u', "influxdb.service", "-f", "--since", from_time]
        p = subprocess.Popen(cmd, stdout=subprocess.PIPE, bufsize=1)
        timetable = {}
        for line in iter(p.stdout.readline, b''):
            event = parse_line(line, ["query", "write"], database)
            if event:
                timetable.update(event)
                if "write" in timetable:
                    records -= 1
                    if records == 0:
                        self.print_end()
                        print datetime.datetime.utcnow().isoformat()
                        p.kill()
                        break

    def monitor_dropped_database(self, from_time):
        cmd = ["journalctl", '-u', "influxdb.service", "-f", "--since", from_time]
        p = subprocess.Popen(cmd, stdout=subprocess.PIPE, bufsize=1)
        for line in iter(p.stdout.readline, b''):
            if "DROP+DATABASE" in line:
                self.print_end()
                print datetime.datetime.utcnow().isoformat()
                p.kill()
                break


def evaluate_udr_generation(number_of_records, size_of_metadata, unique_metadata, unique_users):
    clean()
    evaluation = Evaluation(number_of_records, size_of_metadata, unique_metadata, unique_users)
    evaluation.publish_usages_over_data(udr_url)
    evaluation.generate_udr(udr_url)


def evaluate_billing_generation_for_one(number_of_records, size_of_metadata, unique_metadata, number_of_udrs):
    clean()
    evaluation = Evaluation(number_of_records, size_of_metadata, unique_metadata, 1)
    evaluation.publish_usages_over_data(udr_url)
    for i in range(number_of_udrs):
        evaluation.generate_udr(udr_url)
    evaluation.generate_bill(bill_url, "0")


def evaluate_billing_generation_federating(number_of_records, size_of_metadata, unique_metadata,
                                           number_of_users, number_of_udrs):
    clean()
    evaluation = Evaluation(number_of_records, size_of_metadata, unique_metadata, number_of_users)
    evaluation.publish_usages_over_data(udr_url)
    for i in range(number_of_udrs):
        evaluation.generate_udr(udr_url)
    evaluation.generate_bill(bill_url, range(number_of_users))


def evaluate_publishing_data(number_of_records, size_of_metadata, unique_metadata, number_of_users, batch_size):
    clean()
    evaluation = Evaluation(number_of_records, size_of_metadata, unique_metadata, number_of_users)
    evaluation.publish_usages_over_data(udr_url, batch_size)


def evaluate_publishing_queue_data(number_of_records, size_of_metadata, unique_metadata, number_of_users, batch_size):
    clean()
    evaluation = Evaluation(number_of_records, size_of_metadata, unique_metadata, number_of_users)
    evaluation.publish_usage_over_queue(batch_size)



'''
def generate_udrs_for_quering(number_records, users, number):
    clean_measurement(measurement, database, url_path)
    clean_measurement("OpenStackCeilometerDiskCapacity", database, url_path)
    clean_measurement("UDR", "cyclops.udr", url_path)
    clean_measurement("CDR", "cyclops.cdr", url_path)

    evlua = Evaluation(number_records, 0, 0, users)
    evlua.publish_usage_over_queue()

    for i in range(number):
        print "generating UDR"
        evlua.generate_udr(udr_url)


generate_udrs_for_quering(10000, 1000, 10)
'''


'''
target = open('data.txt', 'r')


my_file = target.read()
bloks = my_file.split("Generating Bill ...")
for block in bloks:
    array_of_beg = []
    array_of_end = []
    for line in block.split("\n"):
        if "started" in line:
            array_of_beg.append(float(line.split(" ")[1]))
        if "ended" in line:
            array_of_end.append(float(line.split(" ")[1]))

    if array_of_beg and array_of_end:
        print array_of_end[0] - array_of_beg[0]
'''
