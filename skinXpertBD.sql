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

DROP TABLE IF EXISTS `skin_diseases`;
CREATE TABLE skin_diseases (
	`id_skindiseases` INT AUTO_INCREMENT PRIMARY KEY,
	`disease` VARCHAR(255) NOT NULL,
    `ICD_code` VARCHAR(255),
    `standard_treatment` TEXT,
    `medications` TEXT,
    `alternatives` TEXT,
    `recommendations` TEXT,
    `referral` VARCHAR(255),
    `source` VARCHAR(255)
);
-- ------------------------------------------------------------
-- 5. Results
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `results`;
CREATE TABLE IF NOT EXISTS `results` (
  `id_result` INT NOT NULL AUTO_INCREMENT,
  `id_request` INT NOT NULL,
  `confidence_level` DECIMAL(5,2),
  `id_skindiseases` INT NOT NULL,
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
    ON UPDATE CASCADE,
  CONSTRAINT `fk_result_skindiseases`
    FOREIGN KEY (`id_skindiseases`)
    REFERENCES `skin_diseases` (`id_skindiseases`)
    ON DELETE RESTRICT
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

-- admin

INSERT INTO `admin` (`name`, `email`, `password`)
VALUES
('admin', 'admin@skinXpert.com', SHA2('admin',256));


INSERT INTO skin_diseases 
(disease, ICD_code, standard_treatment, medications, alternatives, recommendations, referral, source)
VALUES
-- 1. Acne and Rosacea
('Acne and Rosacea', 'L70.0, L71', 
'Hygiene + topical treatment; antibiotics in inflammatory cases.',
'Topical retinoids, Benzoyl peroxide, Doxycycline, Isotretinoin, Topical metronidazole',
'Hormonal therapies, Vascular laser',
'Avoid irritants, avoid rosacea triggers, dermatology follow-up',
'Dermatology for moderate or severe cases',
'Dermatology guidelines'),

-- 2. Actinic keratosis / Basal cell carcinoma
('Actinic keratosis and basal cell carcinoma', 'L57.0, C44.x',
'Cryotherapy or topical treatments; surgery for BCC.',
'5-FU, Imiquimod, Vismodegib',
'Photodynamic therapy, Radiotherapy',
'Strict photoprotection, regular check-ups',
'Dermatology / Oncology',
'Skin cancer guidelines'),

-- 3. Atopic dermatitis
('Atopic dermatitis', 'L20',
'Emollients + topical corticosteroids; biologics in severe cases.',
'Topical corticosteroids, Tacrolimus, Pimecrolimus, Dupilumab',
'Phototherapy, immunosuppression in selected cases',
'Hydration, avoid allergens, control secondary infections',
'Dermatology',
'Atopic dermatitis guidelines'),

-- 4. Bullous diseases
('Bullous diseases', 'L10, L12',
'Systemic corticosteroids + immunosuppressants.',
'Systemic corticosteroids, Azathioprine, Mycophenolate, Rituximab',
'Biological therapies, wound care',
'Skin care, infection control',
'Dermatology / Internal medicine',
'Bullous disease guidelines'),

-- 5. Cellulitis and impetigo
('Cellulitis and impetigo', 'L03, A46',
'Oral or IV antibiotics depending on severity.',
'Dicloxacillin, Cephalexin, Mupirocin, Clindamycin',
'Abscess drainage, hospitalization if sepsis',
'Hygiene, local wound care',
'Emergency or infectious disease depending on severity',
'Skin infection guidelines'),

-- 6. Eczema
('Eczema', 'L30',
'Topical corticosteroids + irritant avoidance.',
'Topical corticosteroids, Tacrolimus',
'Phototherapy, immunosuppression',
'Avoid irritants, hydration',
'Dermatology if refractory',
'Dermatology guidelines'),

-- 7. Exanthems and drug reactions
('Exanthems and drug reactions', 'T88.7, L27',
'Discontinue drug, symptomatic support.',
'Antihistamines, topical or systemic corticosteroids',
'Hospital treatment if SCAR/TEN',
'Document allergies, follow-up testing',
'Emergency or allergy department',
'Skin reaction protocols'),

-- 8. Alopecia and hair disorders
('Alopecia and hair disorders', 'L63, L66',
'Topical/injected corticosteroids, minoxidil.',
'Corticosteroids, Minoxidil, JAK inhibitors',
'Hair transplantation, laser therapy',
'Evaluate systemic causes, psychological support',
'Dermatology',
'Alopecia guidelines'),

-- 9. Herpes, HPV, and other STIs
('Herpes, HPV and other STIs', 'A60-A64, B97.7',
'Antivirals for herpes; imiquimod/cryotherapy for warts.',
'Acyclovir, Valacyclovir, Imiquimod, Salicylic acid',
'Physical removal, HPV vaccination',
'Sexual counseling, prevention',
'Dermatology / Sexual health',
'STI guidelines'),

-- 10. Pigmentation disorders
('Pigmentation disorders', 'L80, L81.x',
'Phototherapy, topical treatments.',
'Topical corticosteroids, Topical calcineurin inhibitors',
'Depigmentation, micropigmentation',
'Photoprotection',
'Dermatology',
'Pigmentation guidelines'),

-- 11. Lupus and connective tissue diseases
('Lupus and connective tissue diseases', 'M32, L93',
'Antimalarials + corticosteroids + immunosuppressants.',
'Hydroxychloroquine, Methotrexate, Azathioprine',
'Biological therapies',
'Strict photoprotection',
'Rheumatology and dermatology',
'Multidisciplinary guidelines'),

-- 12. Melanoma and nevi
('Cutaneous melanoma', 'C43',
'Surgery + staging + immunotherapy.',
'Nivolumab, Pembrolizumab, BRAF/MEK inhibitors',
'Chemotherapy, clinical trials',
'Avoid sun exposure, follow-up',
'Immediate oncology',
'NCCN Guidelines'),

-- 13. Onychomycosis and nail disorders
('Nail fungal infections and disorders', 'B35.1, L60.x',
'Topical or oral antifungals depending on severity.',
'Terbinafine, Itraconazole, Ciclopirox',
'Laser, debridement',
'Hygiene, avoid moisture',
'Dermatology',
'Onychomycosis guidelines'),

-- 14. Contact dermatitis and poison ivy
('Contact dermatitis (including poison ivy)', 'L23-L25',
'Topical/systemic corticosteroids + avoid irritant.',
'Topical corticosteroids, Antihistamines',
'Protective barriers',
'Wash skin after exposure',
'Allergy / Dermatology',
'Dermatitis protocols'),

-- 15. Psoriasis and lichen planus
('Psoriasis and lichen planus', 'L40, L43',
'Topical therapies → phototherapy → systemic/biologics.',
'Methotrexate, Adalimumab, Secukinumab, Ustekinumab',
'Apremilast, UVB phototherapy',
'Manage comorbidities',
'Dermatology',
'Psoriasis guidelines'),

-- 16. Scabies, Lyme and other infestations
('Scabies and Lyme disease', 'B86, A69',
'Permethrin/ivermectin; antibiotics for Lyme.',
'Permethrin, Ivermectin, Doxycycline',
'Environmental measures',
'Treat contacts, bite prevention',
'Dermatology / Infectious diseases',
'Infestation guidelines'),

-- 17. Seborrheic keratosis
('Seborrheic keratosis and benign tumors', 'D23.x, L82.x',
'Observation or removal.',
'No medication required',
'Cryotherapy, curettage',
'Monitor for changes',
'Dermatology',
'Dermatology guidelines'),

-- 18. Systemic disease with skin manifestations
('Systemic diseases with cutaneous manifestations', 'Variable',
'Treatment of underlying disease.',
'Medications depending on etiology',
'Multidisciplinary management',
'Detect systemic symptoms',
'Internal medicine',
'Multidisciplinary guidelines'),

-- 19. Tinea and candidiasis
('Tinea, dermatophytosis and candidiasis', 'B35.x, B37.x',
'Topical or oral antifungals.',
'Terbinafine, Itraconazole, Topical azoles',
'Debridement, hygiene',
'Control predisposing factors',
'Dermatology',
'Mycosis guidelines'),

-- 20. Urticaria
('Urticaria', 'L50',
'H1 antihistamines; omalizumab if refractory.',
'Cetirizine, Loratadine, Omalizumab',
'Short-term systemic corticosteroids',
'Identify triggers',
'Allergy department',
'Urticaria guidelines'),

-- 21. Vascular tumors
('Vascular tumors', 'D18.0',
'Observation or laser/sclerosing treatment.',
'Propranolol, Sclerosing agents',
'Surgery, laser',
'Follow-up depending on evolution',
'Dermatology / Vascular surgery',
'Vascular malformation guidelines'),

-- 22. Vasculitis
('Cutaneous vasculitis', 'M30-M31',
'Systemic corticosteroids + immunosuppression.',
'Methotrexate, Azathioprine, Cyclophosphamide, Rituximab',
'Biologics',
'Evaluate systemic involvement',
'Rheumatology',
'Vasculitis guidelines'),

-- 23. Warts and molluscum
('Warts and molluscum contagiosum', 'B07, A63',
'Destructive or topical therapies.',
'Salicylic acid, Imiquimod, Cryotherapy',
'Observation in children, topical immunotherapy',
'Advice on contagion',
'Dermatology',
'Viral infection guidelines');

-- results

INSERT INTO `results` (`id_request`, `confidence_level`, `id_skindiseases`, `id_doctor`)
VALUES
(2, 88.50, 3, 1);