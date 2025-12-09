package karsch.lukas.config.commandInterceptors;

import karsch.lukas.core.exceptions.ErrorDetails;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class AggregateNotFoundInterceptor implements MessageHandlerInterceptor<CommandMessage<?>> {

    @Override
    public Object handle(@Nonnull UnitOfWork<? extends CommandMessage<?>> unitOfWork,
                         @Nonnull InterceptorChain interceptorChain) throws Exception {
        try {
            return interceptorChain.proceed();
        } catch (AggregateNotFoundException e) {
            throw new CommandExecutionException(
                    "Resource not found",
                    e,
                    ErrorDetails.RESOURCE_NOT_FOUND
            );
        }
    }
}
