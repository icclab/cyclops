import numpy as np
import pylab as P
import matplotlib.pyplot as plt
from matplotlib import cm
from mpl_toolkits.mplot3d import axes3d




file_data = open('output/ingestion/data_ingestion.csv')
file_rabbit = open('output/ingestion/rabbitmq_ingestion.csv')

size = "big"
dataMeans = []
dataStd = []
my_range = []
for line in file_data.read().split('\n'):
    if size in line:
        output_line = line.split(",")
        my_range.append(output_line[0].split(" ")[1])
        dataMeans.append(float(output_line[27]))
        dataStd.append(float(output_line[28]))


N = len(dataMeans)

ind = np.arange(N)  # the x locations for the groups
width = 0.35       # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind, dataMeans, width, color='orange', yerr=dataStd)

rabbitMeans = []
rabbitStd = []
for line in file_rabbit.read().split('\n'):
    if size in line:
        output_line = line.split(",")
        rabbitMeans.append(float(output_line[27]))
        rabbitStd.append(float(output_line[28]))

print rabbitMeans
rects2 = ax.bar(ind + width, rabbitMeans, width, color='b', yerr=rabbitStd)



# add some text for labels, title and axes ticks
ax.set_ylabel('percentage of loading, %')
ax.set_title('UDR CPU utilization for Ingestion of big records')
ax.set_xticks(ind + width)
ax.set_xticklabels(my_range)
ax.set_xlabel('batch size')
ax.set_ylim(0, 50)

ax.legend((rects1[0], rects2[0]), ('/data', 'rabbitmq'))


def autolabel(rects):
    # attach some text labels
    for rect in rects:
        height = rect.get_height()
        ax.text(rect.get_x() + rect.get_width()/2., 1.05*height,
                '%d' % int(height),
                ha='center', va='bottom')

plt.show()

'''
fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')
my_file = open('output/billing/bill_generation40.csv')

#data
fx = []
fy = []
fz = []

#error data
zerror = []

for line in my_file.read().split('\n'):
    if "number" in line:
        output_line = line.split(",")
        fx.append(float(output_line[0].split(" ")[0]))
        fy.append(float(output_line[0].split("udrs ")[1].split(" ")[0]))
        fz.append(float(output_line[3]))
        zerror.append(float(output_line[4]))


#plot points
ax.plot_trisurf(fx, fy, fz, cmap=cm.coolwarm, linewidth=0)

#plot errorbars
for i in np.arange(0, len(fx)):
    ax.plot([fx[i], fx[i]], [fy[i], fy[i]], [fz[i]+zerror[i], fz[i]-zerror[i]], marker="_")

#configure axes
ax.set_xlim3d(100, 800)
ax.set_ylim3d(10, 60)
ax.set_zlim3d(0, 40)

ax.set_xlabel('number of records')
ax.set_ylabel('number of udrs')
ax.set_zlabel('time, sec')

plt.show()


'''
