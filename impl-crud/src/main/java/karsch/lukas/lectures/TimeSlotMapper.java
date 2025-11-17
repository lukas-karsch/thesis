package karsch.lukas.lectures;

import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.mappers.Mapper;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotMapper implements Mapper<TimeSlotValueObject, TimeSlot> {

    @Override
    public TimeSlot map(TimeSlotValueObject valueObject) {
        return new TimeSlot(
                valueObject.date(),
                valueObject.startTime(),
                valueObject.endTime()
        );
    }
}
