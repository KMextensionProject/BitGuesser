--------------------------------------------------------------------------
---------------------------- P O S T G R E S Q L -------------------------
--------------------------------------------------------------------------

-- DROP SCHEMA IF EXISTS bitcoin;

CREATE SCHEMA IF NOT EXISTS bitcoin
--    AUTHORIZATION <postgres_user>; -- optional


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


--------------------------------------------------------------------------
------------------------------- O R A C L E ------------------------------
--------------------------------------------------------------------------

-- DROP SCHEMA BITCOIN;

CREATE SCHEMA BITCOIN
--	AUTHORIZATION <oracle_user> -- optional
    
-- DROP TABLE BITCOIN.T_ADDRESS;

CREATE TABLE BITCOIN.T_ADDRESS 
(
    S_ADDRESS        VARCHAR2(74 CHAR) NOT NULL,
    S_PRIVATE_KEY    VARCHAR2(64 CHAR)
    PRIMARY KEY ( S_ADDRESS )
)
TABLESPACE BITCOIN;
