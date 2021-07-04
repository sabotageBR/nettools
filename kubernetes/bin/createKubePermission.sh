#!/bin/bash
#https://stackoverflow.com/questions/48311683/how-can-i-use-curl-to-access-the-kubernetes-api-from-within-a-pod

kubectl create rolebinding default-viewer \
  --clusterrole=view \
  --serviceaccount=default:default \
  --namespace=default