kubectl delete configmap prometheus-config --ignore-not-found
kubectl delete deployment prometheus --ignore-not-found
kubectl delete service prometheus-svc --ignore-not-found
kubectl delete deployment devops-prometheus-app --ignore-not-found
kubectl delete service devops-prometheus-svc --ignore-not-found


cd minikube
kubectl apply -f 01_ServiceAccount.yml 
kubectl apply -f 02_ClusterRole.yml
kubectl apply -f 03_ClusterRoleBinding.yml

cd k8s
kubectl apply -f prometheus-configmap.yaml
kubectl apply -f prometheus-deployment.yaml
kubectl apply -f prometheus-service.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

使用工具
kubectl run tmp-box --rm -it --image=busybox --restart=Never -- wget -qO- http://10.244.0.5:8080/actuator/prometheus
minikube service prometheus-svc



192.168.49.2   spring.local
192.168.49.2   prometheus.local
192.168.49.2   grafana.local