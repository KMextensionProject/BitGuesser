-- DROP SCHEMA IF EXISTS bitcoin;

CREATE SCHEMA IF NOT EXISTS bitcoin
    AUTHORIZATION postgres;


-- DROP TABLE IF EXISTS bitcoin.t_address;

CREATE TABLE IF NOT EXISTS bitcoin.t_address
(
    s_address character varying(74) COLLATE pg_catalog."default" NOT NULL,
    s_private_key character varying(64) COLLATE pg_catalog."default",
    CONSTRAINT t_address_pkey PRIMARY KEY (s_address)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS bitcoin.t_address
    OWNER to postgres;
