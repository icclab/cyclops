#!/bin/bash

echo -e "ServerHTTPPort=$ServerHTTPPort\n" \
"ServerHealthCheck=$ServerHealthCheck\n" \
"ServerHealthShutdown=$ServerHealthShutdown\n" \
"DatabasePort=$DatabasePort\n" \
"DatabaseHost=$DatabaseHost\n" \
"DatabaseUsername=$DatabaseUsername\n" \
"DatabasePassword=$DatabasePassword\n" \
"DatabaseName=$DatabaseName\n" \
"DatabasePageLimit=$DatabasePageLimit\n" \
"DatabaseConnections=$DatabaseConnections\n" \
"PublisherHost=$PublisherHost\n" \
"PublisherUsername=$PublisherUsername\n" \
"PublisherPassword=$PublisherPassword\n" \
"PublisherPort=$PublisherPort\n" \
"PublisherVirtualHost=$PublisherVirtualHost\n" \
"PublisherDispatchExchange=$PublisherDispatchExchange\n" \
"PublisherBroadcastExchange=$PublisherBroadcastExchange\n" \
"RoutingKeyPublishUDRCommand=$RoutingKeyPublishUDRCommand\n" \
"ConsumerHost=$ConsumerHost\n" \
"ConsumerUsername=$ConsumerUsername\n" \
"ConsumerPassword=$ConsumerPassword\n" \
"ConsumerPort=$ConsumerPort\n" \
"ConsumerVirtualHost=$ConsumerVirtualHost\n" \
"ConsumerDataQueue=$ConsumerDataQueue\n" \
"ConsumerCommandsQueue=$ConsumerCommandsQueue\n" >> udr.conf 