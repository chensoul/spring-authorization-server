package com.chensoul.authserver.configuration;

import com.chensoul.authserver.authentication.CustomUser;
import com.chensoul.authserver.oauth2.client.CustomRegisteredClientRepository;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationPrinter implements ApplicationRunner {
    private static Logger logger = LoggerFactory.getLogger("spring-authorization-server");
    private Integer port;
    private String scheme = "http";
    private final Collection<CustomUser> users;
    private final CustomRegisteredClientRepository repository;
    private static BuildProperties buildProperties;

    public ConfigurationPrinter(ServerProperties serverProperties, CustomRegisteredClientRepository repository, BuildProperties buildProperties) {
        this.users = serverProperties.getUsers();
        this.repository = repository;
        this.buildProperties = buildProperties;
    }

    public static void printSampleConfiguration() {
        String sampleConfig = """
                server: # OPTIONAL
                  # The port on which Spring Authorization Server runs. Defaults to 9000.
                  port: 9000
                
                application:
                  spring-authorization-server:
                
                    # OPTIONAL: whether to use a hardcoded RSA key for JWT signing, or a randomly generated one.
                    # Hardcoded keys mean faster startup time.
                    jwk:
                      # Defaults to false
                      random: false
                
                
                    # OPTIONAL: custom users for logging in
                    users:
                      - username: my-user # REQUIRED
                        password: clear-text-password # REQUIRED
                        # Attributes are added to the id_token based on requested scopes.
                        # All attributes are optional.
                        attributes: # OPTIONAL
                          # standard OpenID Connect attributes:
                
                          # scope: profile
                          name: "Jane T. Spring"
                          given_name: "Jane"
                          family_name: "Spring"
                          middle_name: "Team"
                          nickname: "Spring"
                          preferred_username: "spring"
                          profile: "https://spring.io/team"
                          picture: "https://spring.io/img/spring-2.svg"
                          website: "https://spring.io"
                          gender: "unspecified"
                          birthdate: "1970-01-01"
                          zoneinfo: "Europe/Paris"
                          locale: "fr-FR"
                
                          # scope: email
                          email: "user@example.com"
                          email_verified: true
                
                          # scope: phone_number
                          phone_number: "+1 (555) 555-1234"
                          phone_number_verified: true
                
                          # scope: address
                          address:
                            formatted: "1, OpenID St., Openid.net City, 1234 Identity Realm, Internet"
                            street_address: "1, OpenID St."
                            locality: "Openid.net City"
                            region: "Identity Realm"
                            postal_code: "1234"
                            country: "Internet"
                
                          # all other attributes are custom ("user-defined"), and added to the id_token claims when
                          # the "profile" scope is requested
                          some-claim: "some-value"
                          custom-age: 42
                      - username: other-user
                        password: other-password
                
                
                    # OPTIONAL: custom client registrations, which must match the client application's
                    # spring.security.oauth2.client.registration.<id>.* properties
                    clients:
                      - client-id: "custom-client"
                        client-secret: "custom-secret"
                        # MUST be one or more of the following
                        client-authentication-methods:
                          - "client_secret_basic"
                          - "client_secret_post"
                          - "none"
                        # MUST be one or more of the following
                        authorization-grant-types:
                          - "authorization_code"
                          - "client_credentials"
                          - "refresh_token"
                        # OPTIONAL, can be anything
                        scope:
                          - "openid"
                          - "email"
                          - "profile"
                          - "address"
                          - "phone"
                          - "message.read"
                          - "message.write"
                        # REQUIRED when authorization-grant-type contains authorization_code, otherwise OPTIONAL
                        redirect-uris:
                          # This is default Spring Boot redirect URI for the spring-authorization-server provider
                          - "http://127.0.0.1:8080/login/oauth2/code/spring-authorization-server"
                          - "http://localhost:8080/login/oauth2/code/spring-authorization-server"
                          # Here are other examples:
                          - "http://127.0.0.1:8081/authorized"
                          - "http://127.0.0.1:8082/callback"
                        # OPTIONAL: only necessary if validate-redirect-uri is true
                        post_logout_redirect_uris:
                          - "http://127.0.0.1:8080/"
                        # OPTIONAL: show the "consent" screen on the /oauth2/authorize call. Defaults to false.
                        require-authorization-consent: false
                        # OPTIONAL: enable pkce for the client. Defaults to false.
                        require-proof-key: false
                        # OPTIONAL: enforce redirect_uri and post_logout_redirect_uri validation. When set to true,
                        # clients may only use one of the redirect_uris defined for this client. Defaults to false.
                        validate-redirect-uri: false
                        # OPTIONAL: enforce scope validation. When set to true, clients may only use one of
                        # the scopes defined for this client. Defaults to false.
                        validate-scope: false
                
                      - client-id: "other-client"
                        client-secret: "other-secret"
                        client-authentication-methods:
                          - "client_secret_basic"
                        authorization-grant-types:
                          - "client_credentials"
                """;
        System.out.println(sampleConfig);
    }

    public static void printHelp() {
        String helpMessage = getHelpString();
        System.out.println(helpMessage);
    }

    public static String getHelpString() {
        return """
                You are using Spring Authorization Server v%s.
                
                Usage:
                
                \tjava -jar spring-authorization-server-%s.jar [OPTIONS]
                
                
                Example with a custom configuration file:
                
                \tjava -jar spring-authorization-server-%s.jar --config=my-configuration.yml
                
                
                Options:
                
                \t--config=my-configuration.yml:
                \t\tRun Spring Authorization Server using the configuration file `my-configuration.yml`. To
                \t\tobtain a sample configuration file, see --print-sample-config
                
                \t--help:
                \t\tPrint this help message
                
                \t--print-sample-config:
                \t\tPrint a sample configuration file, which can be used with the `--config=` flag.
                """.formatted(buildProperties.getVersion(), buildProperties.getVersion(), buildProperties.getVersion());
    }

    public static void printUnknownFlag(String flag) {
        String unknownFlagMessage = """
                Unknown flag [%s]. To learn more about usage, re-run the program with the --help flag:
                
                \tjava -jar spring-authorization-server-%s.jar --help
                """.formatted(flag, buildProperties.getVersion());
        System.out.println(unknownFlagMessage);
    }

    @EventListener
    void onApplicationEvent(final ServletWebServerInitializedEvent event) {
        WebServer webServer = event.getWebServer();
        if (webServer instanceof TomcatWebServer tomcat) {
            this.scheme = tomcat.getTomcat().getConnector().getScheme();
        }

        this.port = event.getWebServer().getPort();
    }

    public void run(ApplicationArguments args) {
        String welcomeMessage = """
                You are using Spring Authorization Server.
                
                To learn how to configure the Spring Authorization Server, re-run with the --help flag.
                
                \ud83d\udea8 DO NOT USE IN PRODUCTION
                ---
                
                Spring Authorization Server is built for local development and testing. It is not, in any way, fit for production.
                """;
        String userConfig = this.getUserConfig();
        String clientConfig = this.repository.isDefault() ? this.getDefaultClientConfig():this.getClientConfig();
        logger.info("\n\n{}\n\n{}\n\n{}", welcomeMessage, userConfig, clientConfig);
    }

    private String getUserConfig() {
        String userHeader = """
                \ud83e\uddd1 You can log in with the following users:
                ---
                
                """;
        String userList = this.users.stream()
                .map((user) -> "- username: %s\n  password: %s\n".formatted(user.getUsername(), user.getPassword()))
                .collect(Collectors.joining(""));
        return userHeader + userList;
    }

    private String getClientConfig() {
        return """
                \ud83e\udd7e You can use the following Spring Boot configuration in your own application to target the Authorization Server:
                ---
                
                spring:
                  security:
                    oauth2:
                      client:
                        registration:
                          spring-authorization-server:
                            # ... your client properties ...
                        provider:
                          spring-authorization-server:
                            issuer-uri: %s://localhost:%s
                
                
                The client properties must match those defined in Spring Authorization Server's configuration, under [application.spring-authorization-server.clients].
                
                Make sure that the correct redirect URLs are registered in your Client configuration. For the default Spring Boot configuration, the redirect uri would be: http://127.0.0.1:8080/login/oauth2/code/spring-authorization-server
                
                For more information, refer to the reference documentation on how to configure OAuth2 clients:
                - Servlet: https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html
                - Reactive: https://docs.spring.io/spring-security/reference/reactive/oauth2/login/core.html
                """.formatted(this.scheme, this.port);
    }

    private String getDefaultClientConfig() {
        return """
                \ud83e\udd7e You can use the following Spring Boot configuration in your own application to target the Authorization Server:
                ---
                
                spring:
                  security:
                    oauth2:
                      client:
                        registration:
                          spring-authorization-server:
                            client-id: %s
                            client-secret: %s
                            client-name: Spring Authorization Server
                            scope:
                              - openid
                              - email
                              - profile
                        provider:
                          spring-authorization-server:
                            issuer-uri: %s://localhost:%s
                """.formatted(Defaults.CLIENT.getClientId(), Defaults.CLIENT.getClientSecret(), this.scheme, this.port);
    }
}
