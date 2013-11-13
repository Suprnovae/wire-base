# --- !Ups

CREATE TABLE withdrawals(
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  transaction_id UUID REFERENCES transactions(id),
  cash_point_id UUID REFERENCES cash_points(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX single_withdrawal_per_transaction ON withdrawals (transaction_id);

# --- !Downs

DROP TABLE withdrawals;
DROP INDEX IF EXISTS single_withdrawal_per_transaction;
