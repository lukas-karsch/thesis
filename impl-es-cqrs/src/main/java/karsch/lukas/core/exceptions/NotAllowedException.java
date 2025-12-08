package karsch.lukas.core.exceptions;

import org.axonframework.commandhandling.CommandExecutionException;

public class NotAllowedException extends CommandExecutionException {
    public NotAllowedException(String message) {
        super(message, null, ErrorDetails.NOT_ALLOWED);
    }
}
