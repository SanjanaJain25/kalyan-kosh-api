-- Optional service type on the TAB1 school-education user form.
-- Existing users remain NULL; no backfill or NOT NULL constraint.
ALTER TABLE users
  ADD COLUMN service_type VARCHAR(24) NULL DEFAULT NULL;
