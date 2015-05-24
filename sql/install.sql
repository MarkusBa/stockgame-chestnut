--createdb stock
--psql -d stock

CREATE TABLE player (
  id SERIAL primary key,
  name varchar(255)  NOT NULL,
  email varchar(255)  NOT NULL
);

CREATE TABLE item (
  id SERIAL primary key,
  symbol varchar(255)  NOT NULL,
  amount NUMERIC,
  price NUMERIC,
  idplayer SERIAL references player (id) ON DELETE CASCADE,
  ts timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ts_idx ON item (ts);

-- Inserts
insert into player (name, email) values ('test', 'test@test.com');
insert into item (symbol, amount, price, idplayer, ts) values ('CASH', 10000, 1, 1, now());
insert into item (symbol, amount, price, idplayer, ts) values ('PAH3.DE', 0, 0, 1, now());

