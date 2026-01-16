package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.Optional;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        // have to be a bit manual here... but you can also not do this and uncomment the spring.factories line
        new SpringApplicationBuilder(Main.class)
                .initializers(ctx -> {
                    var propertySources = ctx.getEnvironment().getPropertySources();
                    var customLoader = new TokenizingYamlPropertSourceLoader();
                    try {
                        var customProperySources = customLoader.load("whateverIThink", ctx.getResource("application.yml"));
                        customProperySources.forEach(propertySources::addLast);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).run(args);
    }

    @Bean
    Void checkBean(SomeConfig config) {
        if (config.config() != null) {
            if (config.config().emptyMap() instanceof SomeConfig.Token)
                LOGGER.info("emptyMap-token was set");
            else
                LOGGER.error("emptyMap-token was not detected");
            LOGGER.info("sibling contained: "+ Optional.ofNullable(config.config()).map(SomeConfig.Inner::sibling).orElse("NOT LOADED"));
        } else
            LOGGER.info("config not loaded at all");
        return null;
    }
}