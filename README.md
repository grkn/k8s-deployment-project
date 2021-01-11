# K8S Challenge Project

Application INFO : Spring boot, Angular 2+, Canvas Template

Application can run from ide according to some changes in application.properties and application-test.properties.

application.properties under main folder:

k8s.path=https://IP:PORT

k8s.clientCrt=/path/to/client.crt

k8s.clientKey=/path/to/client.key

k8s.caCrt=/path/to/ca.crt

application-test.properties under test folder

k8s.path=https://IP:PORT

k8s.clientCrt=/path/to/client.crt

k8s.clientKey=/path/to/client.key

k8s.caCrt=/path/to/ca.crt

Also you can run by replacing client.crt, client.key.

## Run as jar

java -jar k8s-code-challenge-1.0.jar --k8s.clientCrt=/path/to/client.crt --k8s.clientKey=/path/to/client.key --k8s.path=https://ip:port --k8s.caCrt=/path/to/caCrt

## Run mvn clean install

mvn "-DK8S_CLIENTCRT=/path/to/client.crt" "-DK8S_CLIENTKEY=/path/to/client.key" "-DK8S_PATH=https://ip:port" "-Djdk.tls.client.protocols=TLSv1.2" clean install

## Access UI

For accessing UI, you can open http://localhost:8100/ from browser.

## Endpoints

Deployment:

POST http://localhost:8100/api/v1/user/{userName}/deployment : creates deployment.

{
    "apiVersion": "apps/v1",
    "kind": "Deployment",
    "metaDataName": "deploy6",
    "replicas": 2,
    "appName": "nginx",
    "image": "nginx",
    "imagePullPolicy": "ALWAYS",
    "containerPorts": [
        80
    ],
    "pretty": true,
    "namespace": "default"
}

GET http://localhost:8100/api/v1/user/{userName}/deployment : lists deployments


Authentication:

POST http://localhost:8100/api/v1/authorize

{
    "name": "Gurkan",
    "userName": "grkn",
    "password": "123456"
}

POST http://localhost:8100/api/v1/token

{
    "userName": "grkn",
    "password": "123456"
}


