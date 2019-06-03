FROM golang:1.12.2-alpine3.9

RUN apk update && apk upgrade && \
    apk add --no-cache bash git openssh

WORKDIR /go/src/k8c
COPY . .

ENV login cyclops
ENV pass 6tfGUUG3GKitdj7AFN4FiMWFd9uvJfrXL6utTXaj7VFABHCV6EK27aVUEnWvihDi
ENV host 185.72.22.112

RUN go get -d -v ./...
RUN go install -v ./...


CMD ["k8c"]
