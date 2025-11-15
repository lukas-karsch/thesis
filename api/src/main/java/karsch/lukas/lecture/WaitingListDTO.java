package karsch.lukas.lecture;

import karsch.lukas.student.StudentDTO;

import java.util.List;

public record WaitingListDTO(List<StudentDTO> students) {
}
