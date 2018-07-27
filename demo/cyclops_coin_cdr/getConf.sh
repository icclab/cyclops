#!/bin/bash

echo -e "ServerHTTPPort=$ServerHTTPPort\n" \
"ServerHealthCheck=$ServerHealthCheck\n" \
"ServerHealthShutdown=$ServerHealthShutdown\n" \
"HibernateURL=$HibernateURL\n" \
"HibernateUsername=$HibernateUsername\n" \
"HibernatePassword=$HibernatePassword\n" \
"HibernateDriver=$HibernateDriver\n" \
"HibernateDialect=$HibernateDialect\n" \
"PublisherHost=$PublisherHost\n" \
"PublisherUsername=$PublisherUsername\n" \
"PublisherPassword=$PublisherPassword\n" \
"PublisherPort=$PublisherPort\n" \
"PublisherVirtualHost=$PublisherVirtualHost\n" \
"PublisherDispatchExchange=$PublisherDispatchExchange\n" \
"PublisherBroadcastExchange=$PublisherBroadcastExchange\n" \
"ConsumerHost=$ConsumerHost\n" \
"ConsumerUsername=$ConsumerUsername\n" \
"ConsumerPassword=$ConsumerPassword\n" \
"ConsumerPort=$ConsumerPort\n" \
"ConsumerVirtualHost=$ConsumerVirtualHost\n" \
"ConsumeFromQueue=$ConsumeFromQueue\n" >> coin_cdr.conf