-- Database Setup Script for PostgreSQL
-- Run this manually in pgAdmin or PostgreSQL command line

-- Create database if it doesn't exist
CREATE DATABASE reservation_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'English_United States.1252'
    LC_CTYPE = 'English_United States.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

-- Connect to the database (you need to switch to reservation_db)
-- \c reservation_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE reservation_db TO postgres;

-- The Spring Boot application will create the tables automatically
-- when you run it with spring.jpa.hibernate.ddl-auto=create-drop
