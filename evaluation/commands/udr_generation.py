import datetime
import os
import subprocess
import sys

from evaluation import evaluate_udr_generation
from parsing import parse_output

time_file = datetime.datetime.utcnow().isoformat()
path = 'output/udr_generation/' + time_file
original_output = sys.stdout


def udr_generation_evaluation(number_records, uniqueness):
    print "Start udr generation evaluation"
    os.makedirs(path)
    sys.stdout = open(path + '/full_output.txt', 'w+')
    process = subprocess.Popen(["jvmtop/jvmtop.sh"], stdout=open(path + '/data.txt', 'w+'), shell=True)
    for i in range(100, number_records + 1, 100):
        for unique in range(0, uniqueness + 1, 10):
            line = "with parameters: number = " + str(i) + " and uniqueness = " + str(unique)
            original_output.write(line + '\n')
            print line
            evaluate_udr_generation(i, 20, unique, 10)
    sys.stdout = original_output
    data_map = [{'position': 1, 'source': ['udr']}, {'position': 2, 'source': ['coin_cdr','cdr']}]
    parse_output(path, data_map)
    sys.stdout = original_output
    print "UDR generation evaluation is finished"
    process.terminate()


def udr_generation_evaluation_uniqueness(number_records, uniqueness, users):
    print "Start udr generation evaluation"
    os.makedirs(path)
    sys.stdout = open(path + '/full_output.txt', 'w+')
    process = subprocess.Popen(["jvmtop/jvmtop.sh"], stdout=open(path + '/data.txt', 'w+'), shell=True)
    for i in range(users, number_records + 1, users):
        for user in range(50, users + 1, 50):
            line = "with parameters: number = " + str(i) + " users = " + str(user) + "and uniqueness = " + str(uniqueness)
            original_output.write(line + '\n')
            print line
            evaluate_udr_generation(i, 20, uniqueness, user)
    sys.stdout = original_output
    data_map = [{'position': 1, 'source': ['udr']}, {'position': 2, 'source': ['cdr']}]
    parse_output(path, data_map)
    sys.stdout = original_output
    print "UDR generation evaluation is finished"
    process.terminate()
