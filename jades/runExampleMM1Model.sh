#!/bin/bash
echo 'Running MM1 model with simulation time = 1000.0, inter-arrival time = 1.0, service time = 1.0 and 1 server'
java -cp target/jades-0.1-SNAPSHOT.jar:./lib/it/unisannio/ing/perflab/jades/custom-org-apache-commons-javaflow/2.0/custom-org-apache-commons-javaflow-2.0.jar  it.unisannio.ing.perflab.jades.examples.MM1 1000 1.0 0.5 1
