# CashPoints schema

# --- !Ups

CREATE TABLE cash_points (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  location POINT NOT NULL,
  address VARCHAR(100) NOT NULL,
  city VARCHAR(20) NOT NULL,
  country CHAR(2) NOT NULL,
  serial VARCHAR(128) NOT NULL,
  token VARCHAR(128) NOT NULL,
  note VARCHAR(140),
  active BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

# --- !Downs

DROP TABLE cash_points;
