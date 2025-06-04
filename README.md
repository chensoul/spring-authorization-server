# spring-authorization-server

## How to use from source code

Package the application using the maven command:

```bash
./mvnw clean package
```

Run the application with `--config` argument for the sample file `samples/config.yml`:

```yaml
application:
  spring-authorization-server:
    users:
      - username: alice
        password: alice
        attributes:
          email: alice@example.com
          roles:
            - viewer
            - editor
            - admin
      - username: bob
        password: bob
        attributes:
          email: bob@example.com
          roles:
            - viewer
            - editor
```

Then run the application:

```bash
java -jar target/spring-authorization-server-0.0.1-SNAPSHOT.jar --config=samples/config.yml
```

## How to use from maven

You can download it directly using Maven:

```bash
mvn dependency:copy \
 -Dartifact=com.chensoul:spring-authorization-server:<VERSION> \
 -DoutputDirectory=.
```

To run Spring Authorization Server, Ensure that Java 17+ is installed, and then run:

```bash
java -jar spring-authorization-server-<VERSION>.jar
```

## Using in a client application

### Config the client application

Copy the sample configuration that the authorization server prints out in the console, and use it in your client
application.Make sure the `openid` scope is included in the client configuration.

Finally, configure your client application to extract authorities from the custom `roles` claim, by providing an `OidcUserService` bean:

```java

@Bean
OidcUserService oidcUserService() {
    var oidcUserService = new OidcUserService();
    oidcUserService.setOidcUserMapper((oidcUserRequest, oidcUserInfo) -> {
        // Will map the "roles" claim from the `id_token` into user authorities (roles)
        var roles = oidcUserRequest.getIdToken().getClaimAsStringList("roles");
        var authorities = AuthorityUtils.createAuthorityList();
        if (roles!=null) {
            roles.stream()
                    .map(r -> "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        return new DefaultOidcUser(authorities, oidcUserRequest.getIdToken(), oidcUserInfo);
    });
    return oidcUserService;
}
```

Roles can then be checked in request or method security:

```java

@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/public/**").permitAll();
                auth.requestMatchers("/document/**").hasAnyRole("viewer", "editor", "admin");
                auth.requestMatchers("/admin/**").hasRole("admin");
                auth.anyRequest().authenticated();
            })
            .oauth2Login(Customizer.withDefaults())
            .build();
}
```

### Using in tests with Testcontainers

First add the `testcontainers` dependency to your project, for example pom.xml in a maven project:

```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
    </dependency>
    <dependency>
     <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-testcontainers</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
    </dependency>
```

Then, configure `@SpringBootTests` to use Spring Authorization Server + Testcontainers:

```java

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class TestcontainersTests {

    @Container
    static GenericContainer<?> authServer = new GenericContainer<>("chensoul/spring-authorization-server:0.0.1")
            .withExposedPorts(9000);

    @DynamicPropertySource
    static void clientRegistrationProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.client.provider.spring-authorization-server.issuer-uri",
                () -> "http://localhost:" + authServer.getExposedPorts().get(0));
    }

    @Test
    void contextLoads() {
    }
}
```

## Enable AOT

Install GraalVM JDK:

```bash
sdk env install
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
docker run -d -p 9000:9000 chensoul/spring-authorization-server:0.0.1
```

Alternatively, you can push the image to docker hub:

```bash
docker login
docker tag chensoul/spring-authorization-server:0.0.1 chensoul/spring-authorization-server:latest
docker push chensoul/spring-authorization-server:0.0.1
```