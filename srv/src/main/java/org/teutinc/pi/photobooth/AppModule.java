package org.teutinc.pi.photobooth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Charsets;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.*;
import restx.jackson.FrontObjectMapperFactory;
import restx.security.*;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

@Module
public class AppModule {
    private static final Logger logger = getLogger(AppModule.class);

    @Provides
    public SignatureKey signatureKey() {
         return new SignatureKey("photobooth a4193742-3c1b-40e9-b58f-6195234045cf 6391424401974414353 photobooth".getBytes(Charsets.UTF_8));
    }

    @Provides
    @Named("restx.admin.password")
    public String restxAdminPassword() {
        return "admin";
    }

    @Provides
    public ConfigSupplier appConfigSupplier(ConfigLoader configLoader) {
        return configLoader.fromResource("camerabox");
    }

    @Provides
    public CredentialsStrategy credentialsStrategy() {
        return new BCryptCredentialsStrategy();
    }

    @Provides
    public BasicPrincipalAuthenticator basicPrincipalAuthenticator(
            SecuritySettings securitySettings, CredentialsStrategy credentialsStrategy,
            @Named("restx.admin.passwordHash") String defaultAdminPasswordHash, ObjectMapper mapper) {
        return new StdBasicPrincipalAuthenticator(new StdUserService<>(
                // use file based users repository.
                // Developer's note: prefer another storage mechanism for your users if you need real user management
                // and better perf
                new FileBasedUserRepository<>(
                        StdUser.class, // this is the class for the User objects, that you can get in your app code
                        // with RestxSession.current().getPrincipal().get()
                        // it can be a custom user class, it just need to be json deserializable
                        mapper,

                        // this is the default restx admin, useful to access the restx admin console.
                        // if one user with restx-admin role is defined in the repository, this default user won't be
                        // available anymore
                        new StdUser("admin", ImmutableSet.<String>of("*")),

                        // the path where users are stored
                        Paths.get("data/users.json"),

                        // the path where credentials are stored. isolating both is a good practice in terms of security
                        // it is strongly recommended to follow this approach even if you use your own repository
                        Paths.get("data/credentials.json"),

                        // tells that we want to reload the files dynamically if they are touched.
                        // this has a performance impact, if you know your users / credentials never change without a
                        // restart you can disable this to get better perfs
                        true),
                credentialsStrategy, defaultAdminPasswordHash),
                securitySettings);
    }

    @Provides
    public CORSAuthorizer corsAuthorizer() {
        return StdCORSAuthorizer.builder()
                .setOriginMatcher(Predicates.<CharSequence>alwaysTrue())
                .setPathMatcher(Predicates.<CharSequence>alwaysTrue())
                .setAllowedMethods(ImmutableList.of("GET", "PUT", "POST", "DELETE"))
                .setAllowedHeaders(ImmutableList.of("accept", "content-type"))
                .build();
    }

    @Provides
    public ComponentCustomizerEngine customizeObjectMapper() {
        return new SingleComponentNameCustomizerEngine<ObjectMapper>(-10000, FrontObjectMapperFactory.NAME) {
            @Override
            public NamedComponent<ObjectMapper> customize(NamedComponent<ObjectMapper> namedComponent) {
                namedComponent.getComponent()
                              .registerModule(new Jdk8Module())
                              .registerModule(new GuavaModule());
                return namedComponent;
            }
        };
    }

    @Provides(priority = -10000)
    public AutoStartable cleanTempPath(AppSettings settings) {
        return () -> {
            final Path tempPath = Paths.get(settings.uploadTempPath());
            if (Files.isDirectory(tempPath)) {
                try (Stream<Path> list = Files.list(tempPath)) {
                    list.map(Path::toFile)
                        .forEach((file) -> {
                            try {
                                FileUtils.forceDelete(file);
                            } catch (IOException e) {
                                logger.warn("unable to delete: " + file, e);
                            }
                        });
                } catch (IOException e) {
                    logger.warn("unable to delete temporary upload directory contents: " + tempPath, e);
                }
            }
        };
    }
}
