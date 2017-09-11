CREATE TABLE Contact (
  id INT,
  name VARCHAR(64),
  phone VARCHAR(64),
  email VARCHAR(64),
  PRIMARY KEY(id)
);

ALTER TABLE Contact OWNER TO flywaydemo;