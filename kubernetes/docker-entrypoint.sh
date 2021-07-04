#!/bin/bash
set -e
/opt/jboss/wildfly/bin/standalone.sh -c standalone-full-ha.xml -b $MY_POD_IP -bprivate $MY_POD_IP -bmanagement 0.0.0.0 -Djboss.node.name=$HOSTNAME -Djboss.txn.uniqueid=$MY_POD_IP -Djboss.messaging.cluster.password=acme
