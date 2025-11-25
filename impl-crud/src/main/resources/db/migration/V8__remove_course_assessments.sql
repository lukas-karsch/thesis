ALTER TABLE course_assessments
    DROP CONSTRAINT fk_course_assessments_on_course_entity;

DROP TABLE course_assessments CASCADE;