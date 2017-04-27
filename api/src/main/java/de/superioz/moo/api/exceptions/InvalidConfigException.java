package de.superioz.moo.api.exceptions;

import de.superioz.moo.api.io.JsonConfig;
import lombok.Getter;

/**
 * Exception if something from the config couldn't be found
 *
 * @see JsonConfig
 */
public class InvalidConfigException extends RuntimeException {

    @Getter
    private JsonConfig config;

    public InvalidConfigException(String message, JsonConfig config) {
        super(message);
        this.config = config;
    }

}
