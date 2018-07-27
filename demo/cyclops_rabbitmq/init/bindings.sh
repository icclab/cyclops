#!/bin/bash

while : ; do
  curl -u "guest:guest" http://localhost:15672/api/vhosts >/dev/null 2>&1
  
  if [ $? -eq 0 ]
  then
    echo "Declaring cyclops.udr.consume queue on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.udr.consume >/dev/null 2>&1
    
    echo "Declaring cyclops.udr.commands queue on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.udr.commands >/dev/null 2>&1
    break
  else
    echo "Waiting for RabbitMQ (consumer host localhost:15672) service to become active"
    sleep 5
  fi
done

while : ; do
  curl -u "guest:guest" http://localhost:15672/api/vhosts >/dev/null 2>&1
  
  if [ $? -eq 0 ]
  then
    echo "Declaring cyclops.udr.broadcast exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.udr.broadcast >/dev/null 2>&1
    
    echo "Declaring cyclops.udr.dispatch exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"direct", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.udr.dispatch >/dev/null 2>&1
    break
  else
    echo "Waiting for RabbitMQ (publisher host localhost:15672) service to become active"
    sleep 5
  fi
done

while : ; do
  curl -u "guest:guest" http://localhost:15672/api/vhosts >/dev/null 2>&1
  
  if [ $? -eq 0 ]
  then
    echo "Declaring cyclops.cdr.consume queue on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.cdr.consume >/dev/null 2>&1
    
    echo "Declaring cyclops.cdr.commands queue on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.cdr.commands >/dev/null 2>&1
    
    echo "Declaring cyclops.coincdr.broadcast exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.coincdr.broadcast >/dev/null 2>&1
    
    echo "Declaring binding between cyclops.coincdr.broadcast and cyclops.cdr.consume on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPOST -d '{}' http://localhost:15672/api/bindings/%2F/e/cyclops.coincdr.broadcast/q/cyclops.cdr.consume	>/dev/null 2>&1
        
    break
  else
    echo "Waiting for RabbitMQ (consumer host localhost:15672) service to become active"
    sleep 5
  fi
done

while : ; do
  curl -u "guest:guest" http://localhost:15672/api/vhosts >/dev/null 2>&1
  
  if [ $? -eq 0 ]
  then
    echo "Declaring cyclops.cdr.broadcast exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.cdr.broadcast >/dev/null 2>&1
    
    echo "Declaring cyclops.cdr.dispatch exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"direct", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.cdr.dispatch >/dev/null 2>&1
    break
  else
    echo "Waiting for RabbitMQ (publisher host localhost:15672) service to become active"
    sleep 5
  fi
done

while : ; do
  curl -u "guest:guest" http://localhost:15672/api/vhosts >/dev/null 2>&1
  
  if [ $? -eq 0 ]
  then
    echo "Declaring cyclops.billing.consume queue on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.billing.consume >/dev/null 2>&1
    
    echo "Declaring cyclops.billing.commands queue on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.billing.commands >/dev/null 2>&1
    
    echo "Declaring cyclops.coinbill.broadcast exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.coinbill.broadcast >/dev/null 2>&1

    echo "Declaring binding between cyclops.coinbill.broadcast and cyclops.billing.consume on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPOST -d '{}' http://localhost:15672/api/bindings/%2F/e/cyclops.coinbill.broadcast/q/cyclops.billing.consume >/dev/null 2>&1
    break
  else
    echo "Waiting for RabbitMQ (consumer host localhost:15672) service to become active"
    sleep 5
  fi
done

while : ; do
  curl -u "guest:guest" http://localhost:15672/api/vhosts >/dev/null 2>&1
  
  if [ $? -eq 0 ]
  then
    echo "Declaring cyclops.billing.broadcast exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.billing.broadcast >/dev/null 2>&1
    
    echo "Declaring cyclops.billing.dispatch exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"direct", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.billing.dispatch >/dev/null 2>&1
    
    echo "Declaring cyclops.cdr.commands queue on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.cdr.commands >/dev/null 2>&1

    echo "Declaring cyclops.coinbill.consume queue on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.coinbill.consume >/dev/null 2>&1

    echo "Declaring binding between cyclops.billing.dispatch and cyclops.cdr.commands with routing key CDR on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPOST -d "{\"routing_key\":\"CDR\"}" http://localhost:15672/api/bindings/%2F/e/cyclops.billing.dispatch/q/cyclops.cdr.commands >/dev/null 2>&1

    echo "Declaring binding between cyclops.billing.dispatch and cyclops.coinbill.consume with routing key CoinBill on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPOST -d "{\"routing_key\":\"CoinBill\"}" http://localhost:15672/api/bindings/%2F/e/cyclops.billing.dispatch/q/cyclops.coinbill.consume >/dev/null 2>&1
    
    # Note: this self-publishing requires Consumer and Publisher to be on the same RabbitMQ server
    echo "Declaring binding between cyclops.billing.dispatch and cyclops.billing.commands with routing key SelfPublish on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPOST -d "{\"routing_key\":\"SelfPublish\"}" http://localhost:15672/api/bindings/%2F/e/cyclops.billing.dispatch/q/cyclops.billing.commands >/dev/null 2>&1
    break
  else
    echo "Waiting for RabbitMQ (publisher host localhost:15672) service to become active"
    sleep 5
  fi
done

while : ; do
  curl -u "guest:guest" http://localhost:15672/api/vhosts >/dev/null 2>&1
  
  if [ $? -eq 0 ]
  then
    echo "Declaring cyclops.coincdr.consume queue on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.coincdr.consume >/dev/null 2>&1
    
    echo "Declaring cyclops.udr.broadcast exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.udr.broadcast >/dev/null 2>&1

    echo "Declaring binding between cyclops.udr.broadcast and cyclops.coincdr.consume on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPOST -d '{}' http://localhost:15672/api/bindings/%2F/e/cyclops.udr.broadcast/q/cyclops.coincdr.consume	>/dev/null 2>&1
    break
  else
    echo "Waiting for RabbitMQ (consumer host localhost:15672) service to become active"
    sleep 5
  fi
done

while : ; do
  curl -u "guest:guest" http://localhost:15672/api/vhosts >/dev/null 2>&1
  
  if [ $? -eq 0 ]
  then
    echo "Declaring cyclops.coincdr.broadcast exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.coincdr.broadcast >/dev/null 2>&1
    
    echo "Declaring cyclops.coincdr.dispatch exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"direct", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.coincdr.dispatch >/dev/null 2>&1
    break
  else
    echo "Waiting for RabbitMQ (publisher host localhost:15672) service to become active"
    sleep 5
  fi
done

while : ; do
  curl -u "guest:guest" http://localhost:15672/api/vhosts >/dev/null 2>&1
  
  if [ $? -eq 0 ]
  then
    echo "Declaring cyclops.coinbill.consume queue on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"durable":true}' http://localhost:15672/api/queues/%2F/cyclops.coinbill.consume >/dev/null 2>&1
    
    echo "Declaring cyclops.cdr.broadcast exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.cdr.broadcast >/dev/null 2>&1

    echo "Declaring binding between cyclops.cdr.broadcast and cyclops.coinbill.consume on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPOST -d '{}' http://localhost:15672/api/bindings/%2F/e/cyclops.cdr.broadcast/q/cyclops.coinbill.consume	>/dev/null 2>&1
    break
  else
    echo "Waiting for RabbitMQ (consumer host localhost:15672) service to become active"
    sleep 5
  fi
done

while : ; do
  curl -u "guest:guest" http://localhost:15672/api/vhosts >/dev/null 2>&1
  
  if [ $? -eq 0 ]
  then
    echo "Declaring cyclops.coinbill.broadcast exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"fanout", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.coinbill.broadcast >/dev/null 2>&1
    
    echo "Declaring cyclops.coinbill.dispatch exchange on localhost:15672 ... "
    curl -u "guest:guest" -H "content-type:application/json" -XPUT -d '{"type":"direct", "durable":true}' http://localhost:15672/api/exchanges/%2F/cyclops.coinbill.dispatch >/dev/null 2>&1
    break
  else
    echo "Waiting for RabbitMQ (publisher host localhost:15672) service to become active"
    sleep 5
  fi
done
