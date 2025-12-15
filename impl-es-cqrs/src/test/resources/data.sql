CREATE INDEX IF NOT EXISTS idx_slp_enrolled_ids_gin
    ON student_lectures_projection
        USING GIN (enrolled_ids);

CREATE INDEX IF NOT EXISTS idx_slp_waitlisted_ids_gin
    ON student_lectures_projection
        USING GIN (waitlisted_ids);

CREATE INDEX IF NOT EXISTS idx_slp_passed_courses_ids_gin
    ON student_credits_lookup
        USING GIN (passed_courses);

CREATE INDEX IF NOT EXISTS idx_slp_passed_courses_ids_gin
    ON courses_lookup
        USING GIN (prerequisite_courses);
