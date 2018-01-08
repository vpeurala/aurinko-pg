ALTER ROLE jaanmurtaja WITH PASSWORD 'argxBX4DxWJKC7st';

CREATE TABLE valtio (
  id BIGSERIAL PRIMARY KEY,
  nimi TEXT NOT NULL
);

CREATE TABLE laiva (
  id BIGSERIAL PRIMARY KEY,
  nimi TEXT NOT NULL,
  valmistumisvuosi DATE NOT NULL,
  akseliteho NUMERIC NOT NULL,
  vetoisuus INT NOT NULL,
  pituus NUMERIC NOT NULL,
  leveys NUMERIC NOT NULL,
  omistaja BIGINT REFERENCES valtio (id) NOT NULL
);

CREATE FUNCTION valtion_id(
  IN in_nimi TEXT)
RETURNS BIGINT
LANGUAGE SQL
STABLE
STRICT
AS $$
SELECT id
FROM valtio
WHERE nimi = in_nimi
$$;

INSERT INTO valtio (nimi) VALUES
  ('Suomi'),
  ('Venäjä');

-- https://fi.wikipedia.org/wiki/J%C3%A4%C3%A4nmurtaja#Suomen_j%C3%A4%C3%A4nmurtajat
INSERT INTO laiva (nimi, valmistumisvuosi, akseliteho, vetoisuus, pituus, leveys, omistaja) VALUES
  ('Voima', '1954-1-1', 10.2, 4159, 83.5, 19.4, valtion_id('Suomi')),
  ('Urho', '1975-1-1', 16.2, 7525, 106.6, 23.8, valtion_id('Suomi')),
  ('Sisu', '1976-1-1', 16.2, 7525, 106.6, 23.8, valtion_id('Suomi')),
  ('Otso', '1986-1-1', 15, 7066, 99, 24.2, valtion_id('Suomi')),
  ('Kontio', '1987-1-1', 15, 7066, 99, 24.2, valtion_id('Suomi')),
  ('Nordica', '1994-1-1', 15, 9088, 116, 26, valtion_id('Suomi')),
  ('Fennica', '1993-1-1', 15, 9088, 116, 26, valtion_id('Suomi')),
  ('Polaris', '2016-1-1', 19, 9300, 110, 24.4, valtion_id('Suomi')),
  ('Rossija', '1985-1-1', 52, 18172, 148, 30, valtion_id('Venäjä')),
  ('Sovjetski Sojuz', '1990-1-1', 52, 18172, 148, 30, valtion_id('Venäjä')),
  ('Jamal', '1992-1-1', 52, 18172, 148, 30, valtion_id('Venäjä')),
  ('Let Pobedy', '2007-1-1', 52, 18172, 148, 30, valtion_id('Venäjä'));

