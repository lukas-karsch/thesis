CREATE INDEX IF NOT EXISTS idx_slp_enrolled_ids_gin
    ON student_lectures_projection
        USING GIN (enrolled_ids);

CREATE INDEX IF NOT EXISTS idx_slp_waitlisted_ids_gin
    ON student_lectures_projection
        USING GIN (waitlisted_ids);
