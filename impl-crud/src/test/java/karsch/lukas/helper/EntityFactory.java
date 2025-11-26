package karsch.lukas.helper;

import jakarta.annotation.Nullable;
import karsch.lukas.courses.CourseEntity;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lectures.LectureEntity;
import karsch.lukas.time.TimeSlotValueObject;
import karsch.lukas.users.ProfessorEntity;
import karsch.lukas.users.StudentEntity;

import java.util.Collections;
import java.util.Set;

public class EntityFactory {
    public static CourseEntity createCourseEntity(int credits) {
        return createCourseEntity(credits, null, null, null);
    }

    public static CourseEntity createCourseEntity(int credits, @Nullable String name, @Nullable String description, @Nullable Integer minimumCreditsRequired) {
        var course = new CourseEntity();
        course.setName(name == null ? "Mathematics" : name);
        course.setDescription(description);
        course.setCredits(credits);
        course.setMinimumCreditsRequired(minimumCreditsRequired != null ? minimumCreditsRequired : 0);

        return course;
    }

    public static ProfessorEntity createProfessorEntity() {
        return createProfessorEntity(null, null);
    }

    public static ProfessorEntity createProfessorEntity(@Nullable String firstName, @Nullable String lastName) {
        var professor = new ProfessorEntity();
        professor.setFirstName(firstName == null ? "John" : firstName);
        professor.setLastName(lastName == null ? "Pork" : lastName);

        return professor;
    }

    /**
     * Applies defaults:
     * <ul>
     *     <li>maximumStudents: 1</li>
     *     <li>timeSlots: empty set</li>
     * </ul>
     */
    public static LectureEntity createLectureEntity(ProfessorEntity professor, CourseEntity course) {
        return createLectureEntity(professor, course, null, Collections.emptySet(), null);
    }

    public static LectureEntity createLectureEntity(ProfessorEntity professor, CourseEntity course, @Nullable Integer maximumStudents, Set<TimeSlotValueObject> timeSlots, @Nullable LectureStatus lectureStatus) {
        var lecture = new LectureEntity();
        lecture.setCourse(course);
        lecture.setProfessor(professor);
        lecture.setMaximumStudents(maximumStudents == null ? 1 : maximumStudents);
        lecture.getTimeSlots().addAll(timeSlots);

        if (lectureStatus != null) {
            lecture.setLectureStatus(lectureStatus);
        }

        return lecture;
    }

    public static StudentEntity createStudentEntity() {
        return createStudentEntity(null, null, null);
    }

    public static StudentEntity createStudentEntity(@Nullable String firstName, @Nullable String lastName, @Nullable Integer semester) {
        var student = new StudentEntity();
        student.setFirstName(firstName == null ? "Default" : firstName);
        student.setLastName(lastName == null ? "Name" : lastName);
        student.setSemester(semester == null ? 1 : semester);
        return student;
    }
}
