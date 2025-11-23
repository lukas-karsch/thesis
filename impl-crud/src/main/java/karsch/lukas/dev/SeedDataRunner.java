package karsch.lukas.dev;

import karsch.lukas.courses.CourseAssessmentValueObject;
import karsch.lukas.courses.CourseEntity;
import karsch.lukas.courses.CoursesRepository;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lectures.LectureEntity;
import karsch.lukas.lectures.LecturesRepository;
import karsch.lukas.stats.AssessmentType;
import karsch.lukas.time.TimeSlotValueObject;
import karsch.lukas.users.ProfessorEntity;
import karsch.lukas.users.ProfessorRepository;
import karsch.lukas.users.StudentEntity;
import karsch.lukas.users.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Profile("dev")
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedDataRunner implements CommandLineRunner {

    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final CoursesRepository coursesRepository;
    private final LecturesRepository lecturesRepository;

    @Override
    public void run(String... args) {
        log.info("Seeding data...");

        var students = createStudents();
        studentRepository.saveAll(students);
        log.info("Saved {} students", students.size());

        var professors = createProfessors();
        professorRepository.saveAll(professors);
        log.info("Saved {} professors", professors.size());

        var courses = createCourses();
        coursesRepository.saveAll(courses);
        log.info("Saved {} courses", courses.size());

        var lectures = createLectures(courses, professors);
        lecturesRepository.saveAll(lectures);
        log.info("Saved {} lectures", lectures.size());

        log.info("Data seeding complete.");
    }

    private List<StudentEntity> createStudents() {
        var student1 = new StudentEntity();
        student1.setFirstName("John");
        student1.setLastName("Doe");

        var student2 = new StudentEntity();
        student2.setFirstName("Anna");
        student2.setLastName("Foo");

        return List.of(
                student1, student2
        );
    }

    private List<ProfessorEntity> createProfessors() {
        var professor1 = new ProfessorEntity();
        professor1.setFirstName("Mr");
        professor1.setLastName("Bean");

        var professor2 = new ProfessorEntity();
        professor2.setFirstName("Jim");
        professor2.setLastName("Knopf");

        return List.of(professor1, professor2);
    }

    private List<CourseEntity> createCourses() {
        var course1 = new CourseEntity();
        course1.setName("Computer Science 1");
        course1.setDescription("Basics and history of computer science");
        course1.setCredits(4);
        course1.setCourseAssessments(
                Set.of(
                        new CourseAssessmentValueObject(AssessmentType.EXAM, 1)
                )
        );

        var course2 = new CourseEntity();
        course2.setName("Computer Science 2");
        course2.setDescription("Advanced topics of computer science");
        course2.setCredits(5);
        course2.setCourseAssessments(
                Set.of(
                        new CourseAssessmentValueObject(AssessmentType.PROJECT, 0.5f),
                        new CourseAssessmentValueObject(AssessmentType.EXAM, 0.5f)
                )
        );

        return List.of(course1, course2);
    }

    private List<LectureEntity> createLectures(List<CourseEntity> courses, List<ProfessorEntity> professors) {
        var lectures = new ArrayList<LectureEntity>();
        int hourOffset = 0;
        for (var course : courses) {
            var lecture = new LectureEntity();
            lecture.setCourse(course);
            lecture.setMaximumStudents(10);

            int hour = 10 + hourOffset;
            hourOffset += 1;

            lecture.getTimeSlots().addAll(Set.of(
                    new TimeSlotValueObject(LocalDate.of(2025, 11, 17), LocalTime.of(hour, 0), LocalTime.of(hour, 59)),
                    new TimeSlotValueObject(LocalDate.of(2025, 11, 24), LocalTime.of(hour, 0), LocalTime.of(hour, 59)),
                    new TimeSlotValueObject(LocalDate.of(2025, 12, 1), LocalTime.of(hour, 0), LocalTime.of(hour, 59))
            ));
            lecture.setLectureStatus(LectureStatus.OPEN_FOR_ENROLLMENT);
            lecture.setProfessor(
                    professors.get(lectures.size() % professors.size())
            );

            lectures.add(lecture);
        }

        return lectures;
    }
}
