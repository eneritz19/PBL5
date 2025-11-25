-- Create schema
CREATE SCHEMA IF NOT EXISTS `skinXpert` DEFAULT CHARACTER SET utf8 ;
USE `skinXpert` ;
USE `skinXpert`;

-- ------------------------------------------------------------
-- 1. TABLE: Users (ahora con password)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id_user` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NULL,
  `email` VARCHAR(150) UNIQUE,
  `password` VARCHAR(256) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_user`)
);

-- ------------------------------------------------------------
-- 2. TABLE: Doctors (ahora con password)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `doctors`;
CREATE TABLE IF NOT EXISTS `doctors` (
  `id_doctor` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) UNIQUE,
  `password` VARCHAR(256) NOT NULL,
  `role` VARCHAR(50),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_doctor`)
);

-- ------------------------------------------------------------
-- 3. Requests
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `requests`;
CREATE TABLE IF NOT EXISTS `requests` (
  `id_request` INT NOT NULL AUTO_INCREMENT,
  `id_user` INT NULL,
  `upload_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `status` ENUM('pending','processing','completed','rejected') DEFAULT 'pending',
  `assigned_doctor_id` INT NULL,
  PRIMARY KEY (`id_request`),
  CONSTRAINT `fk_request_user`
    FOREIGN KEY (`id_user`)
    REFERENCES `users` (`id_user`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_request_doctor`
    FOREIGN KEY (`assigned_doctor_id`)
    REFERENCES `doctors` (`id_doctor`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
);

-- ------------------------------------------------------------
-- 4. Images
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `images`;
CREATE TABLE IF NOT EXISTS `images` (
  `id_image` INT NOT NULL AUTO_INCREMENT,
  `id_request` INT NOT NULL,
  `file_path` VARCHAR(255) NOT NULL,
  `upload_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_image`),
  CONSTRAINT `fk_image_request`
    FOREIGN KEY (`id_request`)
    REFERENCES `requests` (`id_request`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

-- ------------------------------------------------------------
-- 5. Results
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `results`;
CREATE TABLE IF NOT EXISTS `results` (
  `id_result` INT NOT NULL AUTO_INCREMENT,
  `id_request` INT NOT NULL,
  `diagnosis` TEXT NOT NULL,
  `confidence_level` DECIMAL(5,2),
  `recommendations` TEXT,
  `analysis_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `id_doctor` INT,
  PRIMARY KEY (`id_result`),
  CONSTRAINT `fk_result_request`
    FOREIGN KEY (`id_request`)
    REFERENCES `requests` (`id_request`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_result_doctor`
    FOREIGN KEY (`id_doctor`)
    REFERENCES `doctors` (`id_doctor`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
);

-- ------------------------------------------------------------
-- Admin con password
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE IF NOT EXISTS `admin` (
  `id_admin` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NULL,
  `email` VARCHAR(150) UNIQUE,
  `password` VARCHAR(256) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_admin`)
);

-- ---------------------------------------------------
-- INSERT EXAMPLES
-- ------------------------------------------------------------

-- Users
INSERT INTO `users` (`name`, `email`, `password`)
VALUES 
('John Smith', 'john@example.com', SHA2('user123',256)),
('Emily Carter', 'emily@example.com', SHA2('user123',256));


-- Doctors
INSERT INTO `doctors` (`name`, `email`, `password`, `role`)
VALUES
('Dr. Michael Brown', 'mbrown@clinic.com', SHA2('doc123',256), 'dermatologist'),
('Dr. Sarah Johnson', 'sjohnson@clinic.com', SHA2('doc123',256), 'dermatologist');


-- Requests
INSERT INTO `requests` (`id_user`, `status`)
VALUES
(1, 'pending'),
(2, 'processing');

-- Images
INSERT INTO `images` (`id_request`, `file_path`)
VALUES
(1, '/uploads/request1_photo.jpg'),
(2, '/uploads/request2_photo.png');

-- Results
INSERT INTO `results` (`id_request`, `diagnosis`, `confidence_level`, `recommendations`, `id_doctor`)
VALUES
(2, 'Atopic dermatitis', 88.50, 'Apply moisturizer and consult a dermatologist.', 1);

-- admin

INSERT INTO `admin` (`name`, `email`, `password`)
VALUES
('admin', 'admin@skinXpert.com', SHA2('admin',256));
