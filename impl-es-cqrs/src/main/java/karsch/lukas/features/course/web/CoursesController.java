package karsch.lukas.features.course.web;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.CreateCourseRequest;
import karsch.lukas.course.ICoursesController;
import karsch.lukas.features.course.api.CreateCourseCommand;
import karsch.lukas.features.course.api.FindAllCoursesQuery;
import karsch.lukas.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CoursesController implements ICoursesController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

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
        UUID courseId = UUID.randomUUID();
        CreateCourseCommand command = new CreateCourseCommand(
                courseId,
                createCourseRequest.name(),
                createCourseRequest.description(),
                createCourseRequest.credits(),
                createCourseRequest.prerequisiteCourseIds(),
                createCourseRequest.minimumCreditsRequired()
        );

        UUID created = commandGateway.sendAndWait(command); // axon by default returns the ID of the created aggregate

        var response = new ApiResponse<>(HttpStatus.CREATED, "Course creation initiated", created);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}
