package karsch.lukas.lecture;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import karsch.lukas.response.ApiResponse;
import karsch.lukas.swagger.OpenApiConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("lectures")
@Tag(name = "Lectures", description = "Endpoints for managing lectures and student enrollments")
public interface ILecturesController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get lectures for a student", description = "Retrieves enrolled and waitlisted lectures for a given student.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved lectures")
    })
    ResponseEntity<ApiResponse<GetLecturesForStudentResponse>> getLecturesForStudent(@RequestParam UUID studentId);

    @PostMapping("{lectureId}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enroll a student to a lecture", description = "Enrolls the authenticated student to a lecture. If the lecture is full, the student is added to the waiting list.")
    @SecurityRequirement(name = OpenApiConfig.STUDENT_AUTH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Successfully enrolled or waitlisted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - e.g., not open for enrollment, already enrolled, timeslot overlap, prerequisites not met"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user is not a student")
    })
    ResponseEntity<ApiResponse<EnrollStudentResponse>> enrollToLecture(@PathVariable UUID lectureId);

    @DeleteMapping("{lectureId}/enroll")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Disenroll a student from a lecture", description = "Disenrolls the authenticated student from a lecture. If there are students on the waiting list, the next eligible one is enrolled.")
    @SecurityRequirement(name = OpenApiConfig.STUDENT_AUTH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully disenrolled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user is not a student")
    })
    ResponseEntity<ApiResponse<Void>> disenrollFromLecture(@PathVariable UUID lectureId);

    @PostMapping("create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a lecture from a course", description = "Creates a new lecture for a course. Requires professor privileges.")
    @SecurityRequirement(name = OpenApiConfig.PROFESSOR_AUTH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Lecture created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - e.g., overlapping timeslots"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user is not a professor"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found")
    })
    ResponseEntity<ApiResponse<UUID>> createLectureFromCourse(@RequestBody @Valid CreateLectureRequest createLectureRequest);

    @GetMapping("{lectureId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get lecture details", description = "Retrieves detailed information about a specific lecture.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved lecture details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lecture not found")
    })
    ResponseEntity<ApiResponse<LectureDetailDTO>> getLectureDetails(@PathVariable UUID lectureId);

    @GetMapping("all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get details for all lectures", description = "Retrieves detailed all lectures.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved lecture details")
    })
    ResponseEntity<ApiResponse<List<LectureDetailDTO>>> getAllLectureDetails();

    @PostMapping("{lectureId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign a grade to a student", description = "Assigns a grade to a student for a specific lecture assessment. Requires professor privileges.")
    @SecurityRequirement(name = OpenApiConfig.PROFESSOR_AUTH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Grade assigned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - e.g., assessment has not happened yet, lecture status not FINISHED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user is not the responsible professor"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found - e.g., student not enrolled, assessment does not exist")
    })
    ResponseEntity<ApiResponse<Void>> assignGrade(@PathVariable UUID lectureId, @RequestBody @Valid AssignGradeRequest assignGradeRequest);

    @PatchMapping("{lectureId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a student's grade", description = "Updates an already existing grade. Requires professor privileges.")
    @SecurityRequirement(name = OpenApiConfig.PROFESSOR_AUTH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Grade updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user is not the responsible professor"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Grade not found")
    })
    ResponseEntity<ApiResponse<Void>> updateGrade(@PathVariable UUID lectureId, @RequestBody @Valid AssignGradeRequest assignGradeRequest);

    @PostMapping("{lectureId}/dates")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add dates to a lecture", description = "Adds new time slots to a lecture. Requires professor privileges.")
    @SecurityRequirement(name = OpenApiConfig.PROFESSOR_AUTH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Dates added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - e.g., overlapping dates"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user is not the responsible professor")
    })
    ResponseEntity<ApiResponse<Void>> addDatesToLecture(@PathVariable UUID lectureId, @RequestBody @Valid AssignDatesToLectureRequest assignDatesToLectureRequest);

    @PostMapping("{lectureId}/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an assessment to a lecture", description = "Adds a new assessment to a lecture. Requires professor privileges.")
    @SecurityRequirement(name = OpenApiConfig.PROFESSOR_AUTH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Assessment added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - e.g., assessment date is in the past"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user is not the responsible professor"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lecture not found")
    })
    ResponseEntity<ApiResponse<UUID>> addAssessmentForLecture(@PathVariable UUID lectureId, @RequestBody @Valid CreateLectureAssessmentRequest createLectureAssessmentRequest);

    @GetMapping("{lectureId}/waitingList")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get waiting list for a lecture", description = "Retrieves the waiting list for a specific lecture.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved waiting list"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lecture not found")
    })
    ResponseEntity<ApiResponse<WaitlistDTO>> getWaitingListForLecture(@PathVariable UUID lectureId);

    @PostMapping("{lectureId}/lifecycle")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Advance lifecycle of a lecture", description = "Advances the lifecycle of a lecture to a new status (e.g., from 'OPEN_FOR_ENROLLMENT' to 'IN_PROGRESS'). Requires professor privileges.")
    @SecurityRequirement(name = OpenApiConfig.PROFESSOR_AUTH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Lifecycle advanced successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - invalid lifecycle transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user is not the responsible professor")
    })
    ResponseEntity<ApiResponse<Void>> advanceLifecycleOfLecture(@PathVariable UUID lectureId, @RequestParam LectureStatus newLectureStatus);
}
