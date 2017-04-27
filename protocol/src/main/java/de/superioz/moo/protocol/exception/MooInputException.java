package de.superioz.moo.protocol.exception;

import de.superioz.moo.protocol.common.Response;
import lombok.Getter;

/**
 * This exception is being called when the client receives a packets and the response is not OK
 */
@Getter
public class MooInputException extends Exception {

    private Response response;

    public MooInputException(Response response) {
        this.response = response;
    }

}
