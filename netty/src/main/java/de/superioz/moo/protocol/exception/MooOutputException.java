package de.superioz.moo.protocol.exception;

import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;

/**
 * This exception is being called when:<br>
 * - The client tries to send a packets not inside the moo-pool executor service<br>
 * - The client tries to send a packets without cloud-connection
 */
public class MooOutputException extends RuntimeException {

    @Getter
    private Type type;

    public MooOutputException(MooOutputException.Type type, Object... replacements) {
        super(StringUtil.format(type.getMessage(), replacements));
        this.type = type;
    }

    /**
     * The type of the exception earlier mentioned before
     */
    public enum Type {

        CONNECTION_FAILED,
        WRONG_THREAD("You have to execute packet operations asynchronous! ({0})");

        @Getter
        private String message;

        Type(String message) {
            this.message = message;
        }

        Type() {
        }

    }

}
