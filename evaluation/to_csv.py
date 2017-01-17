import os
import statistics

file_array = ['2016-11-21T19:37:32.478141', '2016-11-22T06:49:32.598813',
              '2016-11-22T18:22:03.628782', '2016-11-23T08:31:42.522369', '2016-11-24T16:55:18.180465']
summery_array = []
criteria = "complexity 10"
for folder in file_array:
    file = open('output/billing/' + folder + "/parsed_output.txt")
    result_array = []
    for batch in file.read().split("number of records "):
        if batch:
            final_dict = {}
            splited_batch = batch.split('\n')
            if criteria in batch:
                result_dict = {'name': splited_batch[0], 'billing': {'time': splited_batch[1]}}
                type = 'billing'
                for line in splited_batch[2:]:
                    if "ubuntu" in line:
                        missing_memory = True
                        missing_cpu = True
                        name = ""
                        for value in line.split(" "):
                            if ".jar" in value:
                                name = value
                                if value not in result_dict[type]:
                                    result_dict[type][value] = {'cpu': [], 'memory': []}
                            if "m" in value and missing_memory:
                                result_dict[type][name]["memory"].append(value)
                                missing_memory = False
                            if "%" in value and missing_cpu:
                                result_dict[type][name]["cpu"].append(value)
                                missing_cpu = False
                for type in result_dict:
                    for key in result_dict[type]:
                        if ".jar" in key:
                            for service in result_dict[type][key]:
                                clean_array = []
                                for point in result_dict[type][key][service]:
                                    clean_array.append(float(point[:-1]))
                                result_dict[type][key][service] = {'min': min(clean_array), 'max': max(clean_array), 'mean': sum(clean_array)/len(clean_array)}

                result_array.append(result_dict)
    summery_array.append(result_array)
print summery_array
#/data data
type = 'billing'
combined_dict = {}
for experiment in summery_array:
    for one_run in experiment:
        if one_run['name'] not in combined_dict:
            combined_dict[one_run['name']] = {'time': []}
        combined_dict[one_run['name']]['time'].append(float(one_run[type]['time']))
        for key in one_run[type]:
            if ".jar" in key:
                if key not in combined_dict[one_run['name']]:
                    combined_dict[one_run['name']][key] = {'cpu': {'max': [], 'min': [], 'mean': []},
                                                           'memory': {'max': [], 'min': [], 'mean': []}}
                for measure in one_run[type][key]:
                    for value in one_run[type][key][measure]:
                        combined_dict[one_run['name']][key][measure][value].append(one_run[type][key][measure][value])


def print_csv(array):
    return "," + str(min(array)) + "," + str(max(array)) + "," + str(statistics.median(array)) + "," + str(statistics.stdev(array))


def print_measure_csv(dict):
    return print_csv(dict['min']) + print_csv(dict['max']) + print_csv(dict['mean'])


def sort_x(x):
    try:
        users = x.split(" ")[0]
    except:
        users = "01"
    return float(x.split("udrs ")[1].split(" ")[0] + users)

normal_values="min,,,,max,,,,ave,,,,"
error_values="min,max,ave,err,"
normal_space = ",,,,"

print "," + normal_space + "CDR" + 6 * normal_space + "COIN BILL" + 6 * normal_space + "BILLING"
print "," + normal_space + ("memory" + normal_space*3 + "CPU" + normal_space*3)*3
print ",t" + normal_space + (normal_values*2) * 3
print "," + error_values + (error_values*6)*3
for key in  sorted(combined_dict, key=lambda x: sort_x(x)):
    time_array = combined_dict[key]['time']
    print key, print_csv(time_array), print_measure_csv(combined_dict[key]['cdr.jar']['memory']), print_measure_csv(combined_dict[key]['cdr.jar']['cpu']), \
        print_measure_csv(combined_dict[key]['coin_bill.jar']['memory']), print_measure_csv(combined_dict[key]['coin_bill.jar']['cpu']),\
        print_measure_csv(combined_dict[key]['billing.jar']['memory']), print_measure_csv(combined_dict[key]['billing.jar']['cpu'])








