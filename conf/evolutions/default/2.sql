# --- !Ups

ALTER TABLE transactions RENAME COLUMN created_at TO deposited_at;

ALTER TABLE transactions
  ADD COLUMN withdrawn_at TIMESTAMP,
  ADD COLUMN token CHAR(128) NOT NULL,
  ADD COLUMN code CHAR(12) NOT NULL;

# --- !Downs

ALTER TABLE transactions RENAME COLUMN deposited_at TO created_at;

ALTER TABLE transactions
  DROP COLUMN IF EXISTS withdrawn_at,
  DROP COLUMN IF EXISTS token,
  DROP COLUMN IF EXISTS code;
