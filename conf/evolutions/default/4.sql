# --- !Ups

ALTER TABLE transactions
  ALTER COLUMN token TYPE VARCHAR(128);

# --- !Downs

ALTER TABLE transactions
  ALTER COLUMN token TYPE CHAR(128);
