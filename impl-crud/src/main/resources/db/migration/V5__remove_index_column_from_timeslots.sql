ALTER TABLE lecture_timeslots
    DROP CONSTRAINT pk_lecture_timeslots;

ALTER TABLE lecture_timeslots
    DROP COLUMN slot_index;