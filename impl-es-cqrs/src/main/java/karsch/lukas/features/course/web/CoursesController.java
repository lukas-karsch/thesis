package karsch.lukas.features.course.web;

import karsch.lukas.context.RequestContext;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.CreateCourseRequest;
import karsch.lukas.course.ICoursesController;
import karsch.lukas.features.course.api.CreateCourseCommand;
import karsch.lukas.features.course.api.FindAllCoursesQuery;
import karsch.lukas.response.ApiResponse;
import karsch.lukas.uuid.UuidUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CoursesController implements ICoursesController {

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<Set<CourseDTO>>> getCourses() {
        Future<List<CourseDTO>> future = queryGateway.query(new FindAllCoursesQuery(), ResponseTypes.multipleInstancesOf(CourseDTO.class));
        try {
            List<CourseDTO> courses = future.get();
            var response = new ApiResponse<>(HttpStatus.OK, Set.copyOf(courses));
            return new ResponseEntity<>(response, response.getHttpStatus());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // Handle exceptions, e.g., InterruptedException or ExecutionException
            var response = new ApiResponse<Set<CourseDTO>>(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching courses");
            return new ResponseEntity<>(response, response.getHttpStatus());
        }
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> createCourse(CreateCourseRequest createCourseRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for CoursesController.createCourse", requestContext.getUserType());
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Must be authenticated as professor to create courses"), HttpStatus.FORBIDDEN
            );
        }

        var uuid = UuidUtils.randomV7();
        UUID created = commandGateway.sendAndWait(new CreateCourseCommand(
                uuid,
                createCourseRequest.name(),
                createCourseRequest.description(),
                createCourseRequest.credits(),
                createCourseRequest.prerequisiteCourseIds(),
                createCourseRequest.minimumCreditsRequired()
        ));

        // sanity check
        Assert.isTrue(uuid.equals(created), "Returned UUID is not equal. uuid=" + uuid + ", created=" + created);

        var response = new ApiResponse<>(HttpStatus.CREATED, "Course creation initiated", created);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}
