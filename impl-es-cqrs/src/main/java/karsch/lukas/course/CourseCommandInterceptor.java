package karsch.lukas.course;

import karsch.lukas.course.commands.CreateCourseCommand;
import karsch.lukas.course.queries.FindCoursesByIdsQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseCommandInterceptor implements MessageHandlerInterceptor<CommandMessage<CreateCourseCommand>> {

    private final QueryGateway queryGateway;

    @Override
    public Object handle(@Nonnull UnitOfWork<? extends CommandMessage<CreateCourseCommand>> unitOfWork, @Nonnull InterceptorChain interceptorChain) throws Exception {
        CreateCourseCommand payload = unitOfWork.getMessage().getPayload();
        Set<Long> prerequisiteCourseIds = payload.prerequisiteCourseIds();

        if (prerequisiteCourseIds != null && !prerequisiteCourseIds.isEmpty()) {
            CompletableFuture<List<CourseDTO>> queryResult = queryGateway.query(
                    new FindCoursesByIdsQuery(prerequisiteCourseIds),
                    ResponseTypes.multipleInstancesOf(CourseDTO.class)
            );

            // Using .join() for simplicity here. In a real application, consider proper async handling.
            List<CourseDTO> existingCourses = queryResult.join();

            if (existingCourses.size() != prerequisiteCourseIds.size()) {
                Set<Long> foundIds = existingCourses.stream().map(CourseDTO::id).collect(Collectors.toSet());
                prerequisiteCourseIds.removeAll(foundIds);
                throw new IllegalArgumentException("Invalid prerequisite course IDs: " + prerequisiteCourseIds);
            }
        }

        return interceptorChain.proceed();
    }
}
