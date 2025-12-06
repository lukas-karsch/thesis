package karsch.lukas.core.exceptions;

import org.axonframework.commandhandling.CommandExecutionException;

public abstract class MissingResourceException extends CommandExecutionException {
    public MissingResourceException(String message) {
        super(
                message,
                null,
                ErrorDetails.RESOURCE_NOT_FOUND
        );
    }
}
