#!/bin/bash
set -e
/opt/jboss/wildfly/bin/standalone.sh -c standalone.xml -b $MY_POD_IP -bprivate $MY_POD_IP -bmanagement 0.0.0.0 -Djboss.node.name=$HOSTNAME -Djboss.messaging.cluster.password=acme
