# spring-authorization-server


## Enable AOT

Disable the refresh scope in the application.yaml file:

```yaml
spring:
  cloud:
    refresh:
      enabled: false
```

Install GraalVM JDK:

```bash
sdk install java 21.0.5-graal 
```

Run maven command to compile the native image:

```bash
./mvnw -Pnative native:compile
```

## Run with Docker

Create an image with [buildpack](https://buildpacks.io/).

```bash
brew install buildpacks/tap/pack

pack build spring-authorization-server:0.0.1 \
  --path ./spring-authorization-server-0.0.1-SNAPSHOT.jar \
  --builder paketobuildpacks/builder:tiny
```

> If you will be running the image on an ARM host (such as an Apple machine with an Apple chipset), you must use a
> different builder:
>
> ```bash
> pack build spring-authorization-server:0.0.1 \
> --path target/spring-authorization-server-0.0.1-SNAPSHOT.jar \
> --builder dashaun/builder:tiny
> ```

Or you can create an image using docker build.

```bash
docker build -t chensoul/spring-authorization-server:0.0.1 .
```

Start the container by running:

```bash
docker run -d \
  -p 8888:8888 \
  --mount type=bind,source="$(pwd)"/samples,target=/app/samples \
  -e SPRING_CONFIG_IMPORT='file:samples/config-repo-tls.yml' \
  chensoul/spring-authorization-server:0.0.1
```

Alternatively, you can push the image to docker hub:

```bash
docker login
docker tag chensoul/spring-authorization-server:0.0.1 chensoul/spring-authorization-server:latest
docker push chensoul/spring-authorization-server:0.0.1
```