package org.example;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This config is a bit simple, but essentially show how it is not possible to detect an empty token was set or not
 * A configuration-pattern that is useful which uses this could be a migration of something like:
 * <pre>{@code
 *     my-application:
 *       throttling:
 *         type: fixed
 *         duration: 2s
 * }</pre>
 * using an enum and a sibling. But after a while throttling expands,
 * you get a configurable doubling with min and max, no throttling (ignoring sibling)
 * So the natural, better configuration is
 * <pre>{@code
 *     my-application:
 *       throttling:
 *         fixed:
 *           duration: 2s
 *         none: {}
 *         doubling:
 *           min: 2s
 *           max: 2m
 * }</pre>
 * where the code would then allow only one to be set at a time.
 * @param config
 */
@ConfigurationProperties("some")
public record SomeConfig(
        Inner config

) {
    public record Inner(
            String sibling,
            Token emptyMap
    ) {}
    public record Token() {}
}
