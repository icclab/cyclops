# How to:
- Modify ENV in dockerfile to match RabbitMQ credentials
- Build and push image `docker build <image-name>` and `docker push <image-name>`
- Apply rbac policy to Cluster `kubectl apply -f rbac.yaml`
- Run the container in the cluster with `kubectl run <pod-name> --image=<image-name>`
