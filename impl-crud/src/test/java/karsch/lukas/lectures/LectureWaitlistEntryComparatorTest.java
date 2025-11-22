package karsch.lukas.lectures;

import karsch.lukas.users.StudentEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class LectureWaitlistEntryComparatorTest {

    @Test
    void testCompare_whenDifferentSemester() {
        var student1 = new StudentEntity();
        student1.setSemester(5);
        var student2 = new StudentEntity();
        student2.setSemester(1);

        var e1 = new LectureWaitlistEntryEntity();
        e1.setStudent(student1);
        e1.setCreatedDate(LocalDateTime.of(2025, 1, 1, 12, 0));

        var e2 = new LectureWaitlistEntryEntity();
        e2.setStudent(student2);
        e2.setCreatedDate(LocalDateTime.of(2025, 1, 1, 12, 0));

        var underTest = new LectureWaitlistEntryComparator();

        // first argument is GREATER than the second = result > 0
        assertThat(underTest.compare(e1, e2)).isEqualTo(1);
        // first argument is LESS than the second = result < 0
        assertThat(underTest.compare(e2, e1)).isEqualTo(-1);
    }

    @Test
    void testCompare_whenSameSemester() {
        var student1 = new StudentEntity();
        student1.setSemester(1);
        var student2 = new StudentEntity();
        student2.setSemester(1);

        var e1 = new LectureWaitlistEntryEntity(); // this one should win because it was created earlier
        e1.setStudent(student1);
        e1.setCreatedDate(LocalDateTime.of(2025, 1, 1, 12, 0));

        var e2 = new LectureWaitlistEntryEntity();
        e2.setStudent(student2);
        e2.setCreatedDate(LocalDateTime.of(2025, 2, 1, 12, 0));

        var underTest = new LectureWaitlistEntryComparator();

        // first argument is GREATER than the second = result > 0
        assertThat(underTest.compare(e1, e2)).isEqualTo(1);
        // first argument is LESS than the second = result < 0
        assertThat(underTest.compare(e2, e1)).isEqualTo(-1);
    }
}
