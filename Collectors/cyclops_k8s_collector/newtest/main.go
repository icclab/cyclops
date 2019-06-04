

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
	"flag"
	"fmt"
	"github.com/streadway/amqp"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/clientcmd"

	metrics "k8s.io/metrics/pkg/client/clientset/versioned"
	"log"
	"os"

	"time"
)



type Configuration struct {
	K8s struct {
		ApiToken              string
		ClusterName           string
		ClusterServer         string
		ClusterCA             string
		ContextName           string
		ContextCluster        string
		ContextUser           string
		CurrentContext        string
		UserName              string
		UserClientCertificate string
		UserClientKey         string
		ConfigPath            string
	}
}

type MemoryUnit struct {
	Metric  string `json:"metric"`
	Account string `json:"account"`
	Time    int64 `json:"time"`
	Usage   int64 `json:"usage"`
	Unit   string `json:"unit"`
	Data    `json:"data"`
}
type Data struct {
		PodID     string `json:"podId"`
		Container string `json:"container"`
	}

type k8sdata struct {
	Agent          string `json:"agent"`
	Node           string `json:"node"`
	Podcount       int    `json:"podcount"`
	Servicecount   int    `json:"servicecount"`
	Namespacecount int    `json:"namespacecount"`
	Cpu            string `json:"cpu"`
	Memory         string `json:"ram"`
}

type kubeconfig struct {
	ApiVersion     string      `yaml:"apiVersion"`
	Clusters       []Cluster   `yaml:"clusters"`
	Contexts       []K8Context `yaml:"contexts"`
	CurrentContext string      `yaml:"current-context"`
	Kind           string      `yaml:"kind"`
	Preferences    PrfObj      `yaml:"preferences"`
	Users          []User      `yaml:"users"`
}

type PrfObj struct {
}

type User struct {
	Name      string  `yaml:"name"`
	UserInner UserObj `yaml:"user"`
}

type UserObj struct {
	ClientCert string `yaml:"client-certificate"`
	ClientKey  string `yaml:"client-key"`
}

type Cluster struct {
	Name         string     `yaml:"name"`
	ClusterInner ClusterObj `yaml:"cluster"`
}

type ClusterObj struct {
	CertificateAuth string `yaml:"certificate-authority"`
	Server          string `yaml:"server"`
}

type K8Context struct {
	Name        string     `yaml:"name"`
	ConextInner ContextObj `yaml:"context"`
}

type ContextObj struct {
	ClusterName string `yaml:"cluster"`
	User        string `yaml:"user"`
}

type NodesStat struct {
	Items []NodeData `json:"items"`
}

type NodeData struct {
	Metadata NodeMetadata `json:"metadata"`
	Usage    NodeUsage    `json:"usage"`
}

type NodeMetadata struct {
	Name     string `json:"name"`
	SelfLink string `json:"selfLink"`
}

type NodeUsage struct {
	CPU    string `json:"cpu"`
	Memory string `json:"memory"`
}

func main() {

	///////////////////////////////////////////////////////////////

	//reading from Kube config file
	//reading from Kube config file
	var kubeconfig *string

		//kubeconfig = flag.String("kubeconfig", filepath.Join(home, ".kube", "config"), "(optional) absolute path to the kubeconfig file")
		///go/src/k8c/kube/config
		//kubeconfig = flag.String("kubeconfig", "/home/panos/software/cyclops_k8s_collector/newtest/kube/config", "absolute path to the kubeconfig file")
		kubeconfig = flag.String("kubeconfig", "/go/src/k8c/kube/config", "absolute path to the kubeconfig file")
		//kubeconfig = flag.String("kubeconfig", "/home/panos/.kube/config", "absolute path to the kubeconfig file")

	flag.Parse()

	fmt.Printf("Using the Kube config file located at: %s\n", *kubeconfig)

	// use the current context in kubeconfig
	config, err := clientcmd.BuildConfigFromFlags("", *kubeconfig)


	// create the clientset
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
	//rabbitUrl := fmt.Sprintf("amqp://%s:%s@%s/%s", os.Getenv("login"), os.Getenv("pass"), os.Getenv("host"), os.Getenv("vhost"))
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



		//time.Sleep(60 * time.Second)
		fmt.Println("=========================================================================================")

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
