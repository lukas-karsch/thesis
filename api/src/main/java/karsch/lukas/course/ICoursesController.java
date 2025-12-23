package karsch.lukas.course;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import karsch.lukas.response.ApiResponse;
import karsch.lukas.swagger.OpenApiConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RequestMapping("courses")
@Tag(name = "Courses", description = "Endpoints for managing courses")
public interface ICoursesController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all courses", description = "Retrieves a list of all available courses.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved courses")
    })
    ResponseEntity<ApiResponse<Set<CourseDTO>>> getCourses();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new course",
            description = "Creates a new course. Requires professor privileges."
    )
    @SecurityRequirement(name = OpenApiConfig.PROFESSOR_AUTH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Course created successfully",
                    content = @Content(schema = @Schema(implementation = UUID.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user is not a professor"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Prerequisite course not found")
    })
    ResponseEntity<ApiResponse<UUID>> createCourse(@RequestBody @Valid CreateCourseRequest createCourseRequest);
}
