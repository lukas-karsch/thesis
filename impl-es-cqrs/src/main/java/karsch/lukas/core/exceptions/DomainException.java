package karsch.lukas.core.exceptions;

import org.axonframework.commandhandling.CommandExecutionException;

public class DomainException extends CommandExecutionException {
    public DomainException(String message) {
        super(message, null, ErrorDetails.ILLEGAL_DOMAIN_STATE);
    }
}
