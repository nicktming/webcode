#!/usr/bin/env bash

NAMESPACE=kubeflow
kubectl create namespace ${NAMESPACE}

APP_NAME=my-kubeflow
ks init ${APP_NAME}
cd ${APP_NAME}
ks env set default --namespace ${NAMESPACE}

ks registry add kubeflow github.com/katacoda/kubeflow-ksonnet/tree/master/kubeflow
ks pkg install kubeflow/argo
ks pkg install kubeflow/core
ks pkg install kubeflow/seldon
ks pkg install kubeflow/tf-serving

ks generate kubeflow-core kubeflow-core --namespace=${NAMESPACE}

ks apply default -c kubeflow-core

