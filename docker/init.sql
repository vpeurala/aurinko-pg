ALTER ROLE jaanmurtaja WITH PASSWORD 'argxBX4DxWJKC7st';

CREATE TABLE upseeri(
  id BIGSERIAL PRIMARY KEY,
  etunimi TEXT NOT NULL,
  sukunimi TEXT NOT NULL,
  sotilasarvo TEXT NOT NULL
);

CREATE TABLE laivasto(
  id BIGSERIAL PRIMARY KEY,
  nimi TEXT NOT NULL,
  komentaja BIGINT REFERENCES upseeri (id) NOT NULL
);

CREATE TABLE laiva (
  id BIGSERIAL PRIMARY KEY,
  nimi TEXT NOT NULL,
  laivasto BIGINT REFERENCES laivasto (id) NOT NULL,
  komentaja BIGINT REFERENCES upseeri (id) NOT NULL,
  pituus INT NOT NULL,
  vetoisuus INT NOT NULL,
  aseistus TEXT NOT NULL
);

CREATE TABLE merimies(
  id BIGSERIAL PRIMARY KEY,
  etunimi TEXT NOT NULL,
  sukunimi TEXT NOT NULL,
  sotilasarvo TEXT NOT NULL,
  esimies BIGINT REFERENCES upseeri (id) NOT NULL,
  palveluslaiva BIGINT REFERENCES laiva (id) NOT NULL
);

INSERT INTO upseeri (etunimi, sukunimi, sotilasarvo) VALUES
  ('Pentti', 'Koikkalainen', 'Amiraali'),
  ('Matti', 'Mursu', 'Kommodori');

INSERT INTO laivasto (nimi, komentaja) VALUES
  ('Suomenlahden laivasto', (SELECT id FROM upseeri WHERE etunimi = 'Pentti' AND sukunimi = 'Koikkalainen')),
  ('Saaristomeren laivasto', (SELECT id FROM upseeri WHERE etunimi = 'Matti' AND sukunimi = 'Mursu'));

INSERT INTO laiva (nimi, laivasto, komentaja, pituus, vetoisuus, aseistus) VALUES
  ('Koppelo', (SELECT id FROM LAIVASTO WHERE nimi = 'Suomenlahden laivasto'), (SELECT id FROM upseeri WHERE etunimi = 'Pentti' AND sukunimi = 'Koikkalainen'), 114, 1500, '3 kpl 180 mm tykkejä ja pirunmoiset haupitsit etukannella, sekä 4 22mm ilmatorjuntakonekivääriä ja 3 Urho-luokan ballistista ydinohjusta.');

INSERT INTO merimies (etunimi, sukunimi, sotilasarvo, esimies, palveluslaiva) VALUES
  ('Roope', 'Rosvo', 'Matruusi', (SELECT id FROM upseeri WHERE etunimi = 'Pentti' AND sukunimi = 'Koikkalainen'), (SELECT id FROM laiva WHERE nimi = 'Koppelo'));

