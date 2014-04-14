package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.AdHocRailwayException;

public class BrainException extends AdHocRailwayException {

    public BrainException() {
        super();
    }

    public BrainException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BrainException(final String message) {
        super(message);
    }

    public BrainException(final Throwable cause) {
        super(cause);
    }

}
