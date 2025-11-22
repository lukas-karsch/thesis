package karsch.lukas.lectures;


import java.util.Comparator;

public class LectureWaitlistEntryComparator implements Comparator<LectureWaitlistEntryEntity> {

    @Override
    public int compare(LectureWaitlistEntryEntity e1, LectureWaitlistEntryEntity e2) {
        var semester1 = e1.getStudent().getSemester();
        var semester2 = e2.getStudent().getSemester();

        var semesterCompare = Integer.compare(semester1, semester2);
        if (semesterCompare != 0) {
            return semesterCompare;
        }

        // the "smaller" (earlier) date should win the comparison
        return e2.getCreatedDate().compareTo(e1.getCreatedDate());
    }
}
