#!/bin/bash

rm -f *.key *.crt *.csr *.p12 *.jks

openssl genrsa -out ca.key 2048
openssl req -x509 -new -nodes -key ca.key -sha256 -days 3650 -out ca.crt -subj "/CN=ca.hyperpaint.local"

openssl genrsa -out zookeeper-secure.key 2048
openssl req -new -key zookeeper-secure.key -out zookeeper-secure.csr -subj "/CN=zookeeper.hyperpaint.local"
openssl x509 -req -in zookeeper-secure.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out zookeeper-secure.crt -days 3650 -sha256

openssl genrsa -out certificate.key 2048
openssl req -new -key certificate.key -out certificate.csr -subj "/CN=client.hyperpaint.local"
openssl x509 -req -in certificate.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out certificate.crt -days 3650 -sha256

keytool -import -trustcacerts -alias rootca -file ca.crt -keystore truststore.jks -storepass changeit -noprompt

openssl pkcs12 -export -in zookeeper-secure.crt -inkey zookeeper-secure.key -certfile ca.crt -out keystore.p12 -name zookeeper-secure -passout pass:changeit
keytool -importkeystore -srckeystore keystore.p12 -srcstoretype PKCS12 -destkeystore keystore.jks -deststoretype JKS -srcstorepass changeit -deststorepass changeit
