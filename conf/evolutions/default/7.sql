# --- !Ups

CREATE UNIQUE INDEX user_handles ON users (handle);
CREATE UNIQUE INDEX machine_serials ON cash_points (serial);

# --- !Downs

DROP INDEX IF EXISTS user_handles;
DROP INDEX IF EXISTS machine_serials;
