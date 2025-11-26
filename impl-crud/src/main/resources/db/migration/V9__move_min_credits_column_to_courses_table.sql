ALTER TABLE courses
    ADD minimum_credits_required INTEGER;

ALTER TABLE courses
    ALTER COLUMN minimum_credits_required SET NOT NULL;

ALTER TABLE lectures
    DROP COLUMN minimum_credits_required;