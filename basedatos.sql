CREATE DATABASE IF NOT EXISTS hosteleria_practica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'practica'@'localhost' IDENTIFIED BY 'practica_pw123';
GRANT ALL PRIVILEGES ON hosteleria_practica.* TO 'practica'@'localhost';
FLUSH PRIVILEGES;

show tables from hosteleria_practica;
select * from usuarios;
