package main
/*
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
 */
import (
	"encoding/json"
	"fmt"
	"github.com/streadway/amqp"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	metrics "k8s.io/metrics/pkg/client/clientset/versioned"
	"log"
	"os"
	"time"
)


func main() {
	// creates the in-cluster config
	config, err := rest.InClusterConfig()
	if err != nil {
		panic(err.Error())
	}
	// creates the clientset
	clientset, err := kubernetes.NewForConfig(config)
	if err != nil {
		panic(err.Error())
	}

	mc, err := metrics.NewForConfig(config)
	if err != nil {
		panic(err)
	}
	fmt.Printf("\n---\nConnecting to que\n---\n")
	// read credentials from environment variables to initialize rabbitmq connection
	rabbitUrl := fmt.Sprintf("amqp://%s:%s@%s/", os.Getenv("login"), os.Getenv("pass"), os.Getenv("host"))
	conn, err := amqp.Dial(rabbitUrl)
	failOnError(err, "Failed to connect to RabbitMQ")
	defer conn.Close()
	fmt.Printf("\n---\nConnected to que\n---\n")

	ch, err := conn.Channel()
	failOnError(err, "Failed to open a channel")
	defer ch.Close()

	q, err := ch.QueueDeclare(
		"cyclops.udr.consume", // name
		true,         // durable
		false,         // delete when unused
		false,         // exclusive
		false,         // no-wait
		nil,           // arguments
	)
	failOnError(err, "Failed to declare a queue")
	fmt.Printf("\n---\nRabbitMQ was initialized!!!\n---\n")
	for {
		ns, err := clientset.CoreV1().Namespaces().List(metav1.ListOptions{})
		if err != nil {
			panic(err.Error())
		}

		for _, memoryUnit := range getMemoryUsages("default", mc) {
			body, err := json.Marshal(memoryUnit)
			if err != nil {
				panic(err.Error())
			}
			err = ch.Publish(
				"",     // exchange
				q.Name, // routing key
				false,  // mandatory
				false,  // immediate
				amqp.Publishing{
					ContentType: "application/json",
					Body:        body,
				})
		}
		for _, cpuUnit := range getCPUUsages("default", mc) {
			body, err := json.Marshal(cpuUnit)
			if err != nil {
				panic(err.Error())
			}
			err = ch.Publish(
				"",     // exchange
				q.Name, // routing key
				false,  // mandatory
				false,  // immediate
				amqp.Publishing{
					ContentType: "application/json",
					Body:        body,
				})
		}
		for _, ipUnit := range getIPUsages("default", clientset) {
			body, err := json.Marshal(ipUnit)
			if err != nil {
				panic(err.Error())
			}
			err = ch.Publish(
				"",     // exchange
				q.Name, // routing key
				false,  // mandatory
				false,  // immediate
				amqp.Publishing{
					ContentType: "application/json",
					Body:        body,
				})
		}

		fmt.Printf("There are %d Namespaces in the cluster\n", len(ns.Items))


		//for i := 0; i < len(ns.Items); i++ {
		//	pods, _ := clientset.CoreV1().Pods(ns.Items[i].Name).List(metav1.ListOptions{})
		//
		//	fmt.Printf("Namespace: `%v` has following number of pods: %d\n", ns.Items[i].Name, len(pods.Items))
		//
		//	pv, _ := clientset.CoreV1().PersistentVolumeClaims(ns.Items[i].Name).List(metav1.ListOptions{})
		//	fmt.Printf("Persistent volume claims number: %v", len(pv.Items))
		//	for _, volume := range pv.Items {
		//		fmt.Printf("Storache ephemeral: %v", volume.Status.Capacity.StorageEphemeral())
		//		fmt.Printf("Memory: %v", volume.Status.Capacity.Memory())
		//
		//	}
		//
		//
		//	//var pod v1.Pod
		//	//for _, pod = range pods.Items {
		//	//	for _, container := range pod.Spec.Containers {
		//	//
		//	//		name := container.Name
		//	//		cpu := container.Resources.Requests.Cpu()
		//	//		memory := container.Resources.Requests.Memory()
		//	//		ephemeral := container.Resources.Requests.StorageEphemeral()
		//	//
		//	//		fmt.Printf("Resources of container `%v`:\ncpu: %v\nmemory: %v\nEphemeral storage: %v\n",
		//	//			name, cpu, memory, ephemeral)
		//	//
		//	//	}
		//	//}
		//
		//
		//	services, _ := clientset.CoreV1().Services(ns.Items[i].Name).List(metav1.ListOptions{})
		//	fmt.Printf("Services number of NS #%d \n", len(services.Items))
		//	fmt.Println("--------------------------------------------------------------------------------------")
		//	for j := 0; j < len(services.Items); j++  {
		//		fmt.Printf("Service #%d: '%v' \n", j, services.Items[j].Name)
		//		fmt.Printf("Cluster IP: %v \n", services.Items[j].Spec.ClusterIP)
		//		fmt.Printf("External IPs: %v \n", services.Items[j].Spec.ExternalIPs)
		//		fmt.Printf("Loadbalancer IP: %v \n", services.Items[j].Spec.LoadBalancerIP)
		//		fmt.Printf("Srvice type: %v \n", services.Items[j].Spec.Type)
		//		fmt.Println("--------------------------------------------------------------------------------------")
		//
		//	}
		//}


		time.Sleep(60 * time.Second)
		fmt.Println("=========================================================================================")

	}
}
// Returns memory usages in array of objects per pod of a certain namespace
func getMemoryUsages(namespace string, mc *metrics.Clientset) (memoryUnit []MemoryUnit){
	podMetricsList, err := mc.MetricsV1beta1().PodMetricses(namespace).List(metav1.ListOptions{})
	if err != nil {
		panic(err.Error())
	}
	for _, podMetric := range podMetricsList.Items {
		for _, containerMetrics := range podMetric.Containers {
			mu := MemoryUnit{
				"memory",
				"dord",
				makeTimestamp(),
				containerMetrics.Usage.Memory().Value(),
				"KiB",
				Data{
					podMetric.Name,
					containerMetrics.Name,
				},
			}
			memoryUnit = append(memoryUnit, mu)
		}
	}
	return
}
func getCPUUsages(namespace string, mc *metrics.Clientset) (memoryUnit []MemoryUnit){
	podMetricsList, err := mc.MetricsV1beta1().PodMetricses(namespace).List(metav1.ListOptions{})
	if err != nil {
		panic(err.Error())
	}
	for _, podMetric := range podMetricsList.Items {
		for _, containerMetrics := range podMetric.Containers {
			mu := MemoryUnit{
				"cpu",
				"dord",
				makeTimestamp(),
				containerMetrics.Usage.Cpu().Value(),
				"clock",
				Data{
					podMetric.Name,
					containerMetrics.Name,
				},
			}
			memoryUnit = append(memoryUnit, mu)
		}
	}
	return
}
func getIPUsages(namespace string, clientset *kubernetes.Clientset) (memoryUnit []MemoryUnit){
	services, err := clientset.CoreV1().Services("default").List(metav1.ListOptions{})
	if err != nil {
		panic(err.Error())
	}
	for _, service := range services.Items {
		if service.Spec.Type ==  "loadbalancer"{

		}
		mu := MemoryUnit{
			"IP",
			"dord",
			makeTimestamp(),
			1,
			"",
			Data{
				"",
				"",
			},
		}
		memoryUnit = append(memoryUnit, mu)
	}
	return
}

func failOnError(err error, msg string) {
	if err != nil {
		log.Fatalf("%s: %s", msg, err)
		panic(fmt.Sprintf("%s: %s", msg, err))
	}
}
func makeTimestamp() int64 {
	return time.Now().UnixNano() / int64(time.Millisecond)
}

