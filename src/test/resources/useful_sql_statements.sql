--LIST DATABASES:
SELECT * FROM pg_database;
--KILL ALL CONNECTIONS:
SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = 'jaanmurtaja' AND pid <> pg_backend_pid();
-- SELECT DATASET:
SELECT laiva.id AS laiva_id, laiva.nimi AS laiva_nimi, CAST(EXTRACT(YEAR FROM laiva.valmistumisvuosi) AS INT) AS laiva_valmistumisvuosi, laiva.akseliteho AS laiva_akseliteho, laiva.vetoisuus AS laiva_vetoisuus, laiva.pituus AS laiva_pituus, laiva.leveys AS laiva_leveys ,valtio.id AS valtio_id, valtio.nimi AS valtio_nimi FROM laiva INNER JOIN valtio ON laiva.omistaja = valtio.id ORDER BY laiva_id;
