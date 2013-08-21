# Transactions schema

# --- !Ups

CREATE TABLE users (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  handle varchar(50) NOT NULL,
  secret varchar(128) NOT NULL,
  status varchar(20) NOT NULL DEFAULT 'CREATED',
  created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

# --- !Downs

DROP TABLE users;
