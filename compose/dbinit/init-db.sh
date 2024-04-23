#!/bin/bash

echo "Creating databases and users..."

mysql -u root -p"$MYSQL_ROOT_PASSWORD" <<-EOSQL
    CREATE DATABASE IF NOT EXISTS veda_auth_central;
    CREATE DATABASE IF NOT EXISTS keycloak;
    CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'admin';
    GRANT ALL PRIVILEGES ON veda_auth_central.* TO 'admin'@'%';
    GRANT ALL PRIVILEGES ON keycloak.* TO 'admin'@'%';
    FLUSH PRIVILEGES;
EOSQL

echo "Databases and users created"