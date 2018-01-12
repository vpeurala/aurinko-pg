-- LIST DATABASES:
SELECT * FROM pg_database;

-- KILL ALL CONNECTIONS:
SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = 'jaanmurtaja' AND pid <> pg_backend_pid();

-- CANCEL ALL CONNECTIONS:
SELECT pg_cancel_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = 'jaanmurtaja' AND pid <> pg_backend_pid();

-- SELECT DATASET:
SELECT laiva.id AS laiva_id, laiva.nimi AS laiva_nimi, CAST(EXTRACT(YEAR FROM laiva.valmistumisvuosi) AS INT) AS laiva_valmistumisvuosi, laiva.akseliteho AS laiva_akseliteho, laiva.vetoisuus AS laiva_vetoisuus, laiva.pituus AS laiva_pituus, laiva.leveys AS laiva_leveys ,valtio.id AS valtio_id, valtio.nimi AS valtio_nimi FROM laiva INNER JOIN valtio ON laiva.omistaja = valtio.id ORDER BY laiva_id;

-- DOES THE CURRENT USER HAVE SUPERUSER PRIVILEGES:
SELECT usesuper FROM pg_catalog.pg_user WHERE usename = current_user;

-- System functions, pg_catalog tables and views, and other system-level stuff:
SELECT * FROM pg_catalog.pg_database;

SELECT * FROM pg_catalog.pg_db_role_setting;

SELECT * FROM pg_catalog.pg_default_acl;

SELECT * FROM pg_catalog.pg_operator;

SELECT * FROM pg_catalog.pg_policy;

SELECT * FROM pg_catalog.pg_settings;

SELECT * FROM pg_catalog.pg_stat_activity;

SELECT * FROM pg_catalog.pg_stat_database;

SELECT * FROM pg_catalog.pg_stat_sys_tables;

SELECT * FROM pg_catalog.pg_stat_user_tables;

SELECT * FROM pg_catalog.pg_statistic;

SELECT * FROM pg_catalog.pg_stats;

SELECT * FROM pg_catalog.pg_tables;

SELECT * FROM pg_catalog.pg_ts_config;

SELECT * FROM pg_catalog.pg_user;

SELECT * FROM pg_catalog.pg_user_mapping;

SELECT * FROM pg_catalog.pg_user_mappings;

SELECT current_user;

