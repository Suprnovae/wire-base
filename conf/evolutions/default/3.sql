# --- !Ups

ALTER TABLE transactions
  ALTER COLUMN code TYPE VARCHAR(12);

# --- !Downs

ALTER TABLE transactions
  ALTER COLUMN code TYPE CHAR(12);
