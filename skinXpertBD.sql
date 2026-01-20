DROP SCHEMA IF EXISTS skinXpert;
CREATE SCHEMA skinXpert;
USE skinXpert;


/* 1. CLINICAS*/

DROP TABLE IF EXISTS clinics;
CREATE TABLE clinics (
    id_clinic INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255)
);


/* 2. ADMIN */

DROP TABLE IF EXISTS admin;
CREATE TABLE admin (
    id_admin INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150),
    email VARCHAR(150) UNIQUE,
    password VARCHAR(256) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


/* 3. MEDICOS */

DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (
    id_doctor INT AUTO_INCREMENT PRIMARY KEY,
    doctor_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE,
    password VARCHAR(256) NOT NULL,
    id_clinic INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_clinic) REFERENCES clinics(id_clinic)
        ON DELETE SET NULL ON UPDATE CASCADE
);


/* 4. PACIENTES */

DROP TABLE IF EXISTS patients;
CREATE TABLE patients (
    id_patient INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(15) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE,
    password VARCHAR(256) NOT NULL,
    id_doctor INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_doctor) REFERENCES doctors(id_doctor)
        ON DELETE SET NULL ON UPDATE CASCADE
);



/* 5. CITAS */

DROP TABLE IF EXISTS appointments;
CREATE TABLE appointments (
    id_appointment INT AUTO_INCREMENT PRIMARY KEY,
    id_patient INT NOT NULL,
    id_doctor INT NOT NULL,
    date DATETIME NOT NULL,
    status ENUM('pending','completed','canceled') DEFAULT 'pending',
    comments TEXT,
    FOREIGN KEY (id_patient) REFERENCES patients(id_patient)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_doctor) REFERENCES doctors(id_doctor)
        ON DELETE CASCADE ON UPDATE CASCADE
);


/* 6. SOLICITUDES DE FOTOS */

DROP TABLE IF EXISTS photo_requests;
CREATE TABLE photo_requests (
    id_request INT AUTO_INCREMENT PRIMARY KEY,
    id_patient INT NOT NULL,
    urgency TINYINT DEFAULT 1,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pending','reviewing','diagnosed') DEFAULT 'pending',
    FOREIGN KEY (id_patient) REFERENCES patients(id_patient)
        ON DELETE CASCADE ON UPDATE CASCADE
);


/* 7. IMAGENES */

DROP TABLE IF EXISTS images;
CREATE TABLE images (
    id_image INT AUTO_INCREMENT PRIMARY KEY,
    id_request INT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_request) REFERENCES photo_requests(id_request)
        ON DELETE CASCADE ON UPDATE CASCADE
);



/* 8. ENFERMEDADES  */

DROP TABLE IF EXISTS skin_diseases;
CREATE TABLE skin_diseases (
	id_skindiseases INT AUTO_INCREMENT PRIMARY KEY,
	disease VARCHAR(255) NOT NULL,
    ICD_code VARCHAR(255),
    standard_treatment TEXT,
    medications TEXT,
    alternatives TEXT,
    recommendations TEXT,
    referral VARCHAR(255),
    source VARCHAR(255)
);


/* 9. DIAGNOSTICOS MEDICOS */

DROP TABLE IF EXISTS diagnoses;
CREATE TABLE diagnoses (
    id_diagnosis INT AUTO_INCREMENT PRIMARY KEY,
    id_request INT NOT NULL,
    id_doctor INT NOT NULL,
    id_patient INT NOT NULL,
    id_skindiseases INT NOT NULL,
    confidence DECIMAL(5,2),
    diagnosis_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    doctor_notes TEXT,
    FOREIGN KEY (id_request) REFERENCES photo_requests(id_request)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_patient) REFERENCES patients(id_patient)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_doctor) REFERENCES doctors(id_doctor)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_skindiseases) REFERENCES skin_diseases(id_skindiseases)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

/* INSERTS */

INSERT INTO clinics (name, address) VALUES
('Central Dermatology Clinic', 'Health Ave 123'),
('Southern Specialists Clinic', 'Medicine Street 45');


INSERT INTO admin (name, email, password) VALUES
('Admin1', 'admin@skinXpert.com', SHA2('admin123',256));


INSERT INTO doctors (doctor_code, name, email, password, id_clinic) VALUES
('MED001', 'Dr. Juan Perez', 'jperez@clinic.com', SHA2('med123',256), 1),
('MED002', 'Dr. Laura Gomez', 'lgomez@clinic.com', SHA2('med123',256), 1),
('MED003', 'Dr. Carlos Ruiz', 'cruiz@clinic.com', SHA2('med123',256), 2);


INSERT INTO patients (dni, name, email, password, id_doctor) VALUES
('12345678A', 'Ana Martinez', 'ana@example.com', SHA2('ana123',256), 1),
('87654321B', 'Pedro Lopez', 'pedro@example.com', SHA2('pedro123',256), 1),
('11223344C', 'Carla Torres', 'carla@example.com', SHA2('carla123',256), 2);


INSERT INTO appointments (id_patient, id_doctor, date, status, comments) VALUES
(1, 1, '2025-02-15 10:00:00', 'pending', 'Initial check-up'),
(2, 1, '2025-02-16 12:00:00', 'completed', 'Follow-up evaluation'),
(3, 2, '2025-02-20 09:30:00', 'pending', 'Lesion assessment');


INSERT INTO photo_requests (id_patient, urgency, status) VALUES
(1, 3, 'pending'),
(2, 1, 'reviewing'),
(3, 2, 'pending');


INSERT INTO images (id_request, file_path) VALUES
(1, '/uploads/ana_photo_01.jpg'),
(2, '/uploads/pedro_photo_01.jpg'),
(3, '/uploads/carla_photo_01.jpg');


INSERT INTO skin_diseases
(disease, ICD_code, standard_treatment, medications, alternatives, recommendations, referral, source)
VALUES
('Acne Rosacea', 'L70.0, L71',
'Hygiene and topical treatment; antibiotics if moderate to severe inflammation.',
'Topical retinoids, Benzoyl peroxide, Doxycycline, Isotretinoin, Topical metronidazole',
'Hormonal therapies, Vascular laser',
'Avoid irritants and triggers; dermatologic follow-up',
'Dermatology',
'Dermatology guidelines'),

('Actinic Keratosis', 'L57.0',
'Cryotherapy or topical treatments; follow-up due to progression risk.',
'5-FU, Imiquimod, Diclofenac gel, Photodynamic therapy',
'PDT, Laser, Observation in selected cases',
'Strict photoprotection; periodic check-ups',
'Dermatology',
'Skin cancer guidelines'),

('Basal Cell Carcinoma', 'C44.x',
'Surgical excision or destructive techniques depending on size and location.',
'Imiquimod (superficial cases), Vismodegib (selected advanced cases)',
'Mohs surgery, Radiotherapy (selected)',
'Photoprotection; dermatologic surveillance',
'Dermatology / Oncology',
'Skin cancer guidelines'),

('Melanoma', 'C43',
'Excision and staging; oncologic management if advanced.',
'Pembrolizumab, Nivolumab; BRAF/MEK targeted therapy if applicable',
'Clinical trials, Chemotherapy (selected cases)',
'Avoid sun exposure; close follow-up',
'Oncology (urgent)',
'Melanoma guidelines'),

('Squamous Cell Carcinoma', 'C44.x',
'Surgical excision; staging if high risk.',
'Topical 5-FU (selected in situ cases), Immunotherapy (selected advanced cases)',
'Mohs surgery, Radiotherapy (selected)',
'Photoprotection; periodic reviews',
'Oncology / Dermatology (urgent)',
'Squamous carcinoma guidelines'),

('Benign Tumor', 'D23.x, L82.x',
'Observation or removal if symptomatic or suspicious.',
'Usually no medication required',
'Cryotherapy, Curettage, Laser',
'Monitor for changes',
'Dermatology',
'Dermatology guidelines'),

('Dermatitis', 'L20, L30',
'Emollients plus topical corticosteroids; escalation if severe.',
'Topical corticosteroids, Tacrolimus/Pimecrolimus, Dupilumab (severe)',
'Phototherapy, Selected immunosuppression',
'Moisturization; avoid irritants/allergens',
'Dermatology',
'Dermatitis guidelines'),

('Fungal Infection', 'B35.x, B37.x',
'Topical or oral antifungals depending on extent.',
'Terbinafine, Itraconazole, Topical azoles',
'Debridement in selected cases',
'Hygiene; avoid moisture',
'Dermatology',
'Mycosis guidelines'),

('Hair Disorder', 'L63, L66',
'Etiologic diagnosis; topical or infiltrative treatments as needed.',
'Minoxidil, Corticosteroids, Immunomodulators (depending on cause)',
'Hair transplant, Laser therapy (selected)',
'Investigate systemic causes; psychological support if needed',
'Dermatology',
'Alopecia guidelines'),

('Nevus', 'D22',
'Observation; excision if atypical or suspicious.',
'No medication required',
'Serial dermoscopy, excision if criteria met',
'Monitor ABCDE criteria; photoprotection',
'Dermatology',
'Nevus references'),

('Psoriasis', 'L40, L43',
'Topicals → phototherapy → systemic/biologic therapy depending on severity.',
'Topical corticosteroids, Calcipotriol, Methotrexate, Biologics (case-dependent)',
'UVB phototherapy, Apremilast',
'Manage comorbidities; treatment adherence',
'Dermatology',
'Psoriasis guidelines'),

('Systemic Disease', 'Variable',
'Treatment of the underlying systemic disease.',
'According to etiology (refer for evaluation)',
'Multidisciplinary management',
'Detect systemic symptoms; follow-up',
'Internal Medicine',
'Multidisciplinary guidelines'),

('Urticaria', 'L50',
'H1 antihistamines; escalation if refractory.',
'Cetirizine/Loratadine; Omalizumab (refractory)',
'Short-course corticosteroids (selected)',
'Identify triggers',
'Allergy / Dermatology',
'Urticaria guidelines'),

('Viral Infection', 'B07, A63',
'Destructive or topical therapies depending on lesion.',
'Salicylic acid, Imiquimod, Cryotherapy',
'Observation (children/small lesions), topical immunotherapy',
'Contagion prevention advice',
'Dermatology',
'Viral infection guidelines');


INSERT INTO diagnoses (id_request, id_doctor, id_patient, id_skindiseases, confidence, doctor_notes) VALUES
(1, 1, 1, 1, 87.5, 'Lesion compatible with mild acne.'),
(2, 1, 2, 2, 92.3, 'Dermatitis in active phase.'),
(3, 2, 3, 3, 75.0, 'Plaques compatible with psoriasis.');