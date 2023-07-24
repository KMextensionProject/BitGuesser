--------------------------------------------------------------------------
---------------------------- P O S T G R E S Q L -------------------------
--------------------------------------------------------------------------

-- DROP SCHEMA IF EXISTS bitcoin;

CREATE SCHEMA IF NOT EXISTS bitcoin
--    AUTHORIZATION <postgres_user>; -- optional


-- M A I N  L O O K U P  T A B L E

-- DROP TABLE IF EXISTS bitcoin.t_address;

CREATE TABLE IF NOT EXISTS bitcoin.t_address
(
    s_address character varying(74) COLLATE pg_catalog."default" NOT NULL,
    s_private_key character varying(64) COLLATE pg_catalog."default",
    CONSTRAINT t_address_pkey PRIMARY KEY (s_address)
)
TABLESPACE pg_default;


-- S I D E  T A B L E

-- DROP TABLE IF EXISTS bitcoin.t_generated_address;

CREATE TABLE IF NOT EXISTS bitcoin.t_generated_address
(
    s_address character varying(74) COLLATE pg_catalog."default" NOT NULL,
    s_private_key character varying(64) COLLATE pg_catalog."default" NOT NULL
)
TABLESPACE pg_default;


--------------------------------------------------------------------------
------------------------------- O R A C L E ------------------------------
--------------------------------------------------------------------------

-- DROP SCHEMA BITCOIN;

CREATE SCHEMA BITCOIN
--	AUTHORIZATION <oracle_user> -- optional


-- M A I N  L O O K U P  T A B L E

-- DROP TABLE BITCOIN.T_ADDRESS;

CREATE TABLE BITCOIN.T_ADDRESS 
(
    S_ADDRESS        VARCHAR2(74 CHAR) NOT NULL,
    S_PRIVATE_KEY    VARCHAR2(64 CHAR)
    PRIMARY KEY ( S_ADDRESS )
)
TABLESPACE BITCOIN;


-- S I D E  T A B L E

-- DROP TABLE BITCOIN.T_GENERATED_ADDRESS;

CREATE TABLE BITCOIN.T_GENERATED_ADDRESS 
(
    S_ADDRESS        VARCHAR2(74 CHAR) NOT NULL,
    S_PRIVATE_KEY    VARCHAR2(64 CHAR) NOT NULL
    PRIMARY KEY ( S_ADDRESS )
)
TABLESPACE BITCOIN;

