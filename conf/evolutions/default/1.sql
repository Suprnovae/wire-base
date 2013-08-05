# Transactions schema

# --- !Ups

CREATE TABLE transactions (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  amount   decimal NOT NULL,
  receiver_name varchar(100) NOT NULL,
  receiver_phonenumber varchar(20) NOT NULL,
  receiver_country char(2) NOT NULL,
  sender_name varchar(100) NOT NULL,
  sender_address varchar(100) NOT NULL,
  sender_postalcode varchar(20),
  sender_phonenumber varchar(30) NOT NULL,
  sender_email varchar(50) NOT NULL,
  sender_city varchar(20) NOT NULL,
  sender_country char(2) NOT NULL,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

# --- !Downs

DROP TABLE transactions;
