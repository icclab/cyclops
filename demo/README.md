# Cyclops Demo

Use the scripts to quickly create relevant Docker images for Cyclops micro services and other required components. The images are always built from source code fetched from the master branch.

## Building the images

```bash
$ bash buildImages.sh
```

This script has been tested on Ubuntu and MacOS. The following containers should be listed in the docker image list output.

```bash
$ docker images
```

* cyclops_timescaledb
* cyclops_rabbitmq
* cyclops_rabbitmq_init
* cyclops_coin_bill
* cyclops_coin_cdr
* cyclops_billing
* cyclops_cdr
* cyclops_udr

## Starting Cyclops

The included ```docker-compose.yml``` file is self sufficient to run the framework, once the images have been created, you can start the whole framework using the following command.

```bash
$ docker-compose up -d
```

The containers are created and started in correct order. Necessary RabbitMQ bindings between exchanges and queues are setup appropriately by the ```cyclops_rabbitmq_init``` container. Once the bindings are setup, the container process should exit with status code 0.