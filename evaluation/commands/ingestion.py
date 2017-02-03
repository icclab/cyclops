import datetime
import os
import subprocess
import sys
import time as timing

from evaluation import evaluate_publishing_queue_data, evaluate_publishing_data
from parsing import parse_output

time_file = datetime.datetime.utcnow().isoformat()
path = 'output/ingestion/' + time_file
original_output = sys.stdout


def data_ingestion_evaluation(records):
    print "Start ingestion evaluation"
    os.makedirs(path)
    sys.stdout = open(path + '/full_output.txt', 'w+')
    process = subprocess.Popen(["jvmtop/jvmtop.sh"], stdout=open(path + '/data.txt', 'w+'), shell=True)
    i = 1
    while i < records:
        timing.sleep(2)
        line = "with parameters: batch_size " + str(i) + " big records"
        original_output.write(line+'\n')
        print line
        evaluate_publishing_data(records, 10, 25, 10, i)
        timing.sleep(2)
        evaluate_publishing_queue_data(records, 10, 25, 10, i)
        i *= 2

    i = 1
    while i < records:
        timing.sleep(2)
        line = "with parameters: batch_size " + str(i) + " small records"
        original_output.write(line+'\n')
        print line
        evaluate_publishing_data(records, 0, 0, 10, i)
        timing.sleep(2)
        evaluate_publishing_queue_data(records, 0, 0, 10, i)
        i *= 2
    sys.stdout = original_output
    data_map = [{'position': 0, 'source': ['udr']}, {'position': 1, 'source': ['udr']}]
    parse_output(path, data_map)
    print "Ingestion evaluation is finished"
    process.terminate()
