#!/bin/bash

# Array of docker context directories 
declare -a srcImages=("cyclops_udr" 
					  "cyclops_cdr" 
					  "cyclops_billing" 
					  "cyclops_coin_cdr" 
					  "cyclops_coin_bill" 
					  "cyclops_rabbitmq_init"
					  "cyclops_rabbitmq"
					  "cyclops_timescaledb")

for i in "${srcImages[@]}"
do
	docker build -t "$i" "$i" --no-cache
done

docker rmi -f $(docker images --filter "dangling=true" -q --no-trunc)
 

