import os
import sys
import datetime
import subprocess
from evaluation import evaluate_billing_generation_for_one, evaluate_billing_generation_federating
from parsing import parse_output

time_file = datetime.datetime.utcnow().isoformat()
path = 'output/billing/' + time_file
original_output = sys.stdout


def billing_evaluation(number_of_udrs, number_users):
    print "Start billing generation evaluation"
    os.makedirs(path)
    sys.stdout = open(path + '/full_output.txt', 'w+')
    process = subprocess.Popen(["jvmtop/jvmtop.sh"], stdout=open(path + '/data.txt', 'w+'), shell=True)
    for i in range(10, number_of_udrs + 1, 10):
        line = "with parameters: number of UDRs " + str(i) + " for one"
        original_output.write(line + '\n')
        print line
        evaluate_billing_generation_for_one(1000, 20, 10, i)
        for user in range(100, number_users + 1, 100):
            line = "with parameters: number of UDRs " + str(i) + " number of users " + str(user) + " for federation "
            original_output.write(line + '\n')
            print line
            evaluate_billing_generation_federating(1000, 20, 10, user, i)
    sys.stdout = original_output
    data_map = [{'position': -1, 'source': ['billing']}]
    parse_output(path, data_map)
    print "Billing generation evaluation is finished"
    process.terminate()


def billing_complexity_evaluation(number_of_udrs, number_of_records):
    print "Start billing generation evaluation"
    os.makedirs(path)
    sys.stdout = open(path + '/full_output.txt', 'w+')
    process = subprocess.Popen(["jvmtop/jvmtop.sh"], stdout=open(path + '/data.txt', 'w+'), shell=True)
    complexity = 10
    while complexity <= 40:
        for records in range(500, number_of_records + 1, 500):
            for udrs in range(10, number_of_udrs + 1, 10):
                line = "with parameters: number of records " + str(records) + " number of udrs " + str(udrs) \
                       + " with complexity " + str(complexity)
                original_output.write(line + '\n')
                print line
                evaluate_billing_generation_for_one(number_of_records, 20, complexity, number_of_udrs)
        complexity *= 2
    sys.stdout = original_output
    data_map = [{'position': -1, 'source': ['billing']}]
    parse_output(path, data_map)
    print "Billing generation evaluation is finished"
    process.terminate()
