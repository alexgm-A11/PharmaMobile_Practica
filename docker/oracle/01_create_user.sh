#!/bin/bash
# Se ejecuta al arrancar Oracle; crear el usuario es idempotente.
if [[ ! "$DB_PASSWORD" =~ ^[A-Za-z0-9_]{12,64}$ ]]; then
  echo "DB_PASSWORD debe tener 12-64 letras, numeros o guiones bajos" >&2
  exit 1
fi

sqlplus -s "/ as sysdba" <<SQL
WHENEVER SQLERROR EXIT SQL.SQLCODE
ALTER SESSION SET CONTAINER=FREEPDB1;
DECLARE
  user_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO user_count FROM dba_users WHERE username = 'PHARMADB';
  IF user_count = 0 THEN
    EXECUTE IMMEDIATE 'CREATE USER PHARMADB IDENTIFIED BY "$DB_PASSWORD"';
  END IF;
END;
/
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW, CREATE TRIGGER TO PHARMADB;
ALTER USER PHARMADB QUOTA UNLIMITED ON USERS;
EXIT;
SQL
