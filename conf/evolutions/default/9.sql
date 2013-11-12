# --- !Ups

ALTER TABLE users
  ADD COLUMN class INT NOT NULL DEFAULT 0;

# --- !Downs

ALTER TABLE users 
  DROP COLUMN IF EXISTS class;
