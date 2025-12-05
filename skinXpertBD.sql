/* ===========================================================
   SCHEMA
   =========================================================== */

DROP SCHEMA IF EXISTS skinXpert;
CREATE SCHEMA skinXpert;
USE skinXpert;


/* ===========================================================
   1. CLINICAS
   =========================================================== */

DROP TABLE IF EXISTS clinics;
CREATE TABLE clinics (
    id_clinic INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255)
);

INSERT INTO clinics (name, address) VALUES
('Clínica Dermatológica Central', 'Av. Salud 123'),
('Clínica Especialistas del Sur', 'Calle Medicina 45');


/* ===========================================================
   2. ADMIN
   =========================================================== */

DROP TABLE IF EXISTS admin;
CREATE TABLE admin (
    id_admin INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150),
    email VARCHAR(150) UNIQUE,
    password VARCHAR(256) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO admin (name, email, password) VALUES
('Admin1', 'admin@skinXpert.com', SHA2('admin123',256));


/* ===========================================================
   3. MÉDICOS
   =========================================================== */

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

INSERT INTO doctors (doctor_code, name, email, password, id_clinic) VALUES
('MED001', 'Dr. Juan Pérez', 'jperez@clinic.com', SHA2('med123',256), 1),
('MED002', 'Dra. Laura Gómez', 'lgomez@clinic.com', SHA2('med123',256), 1),
('MED003', 'Dr. Carlos Ruiz', 'cruiz@clinic.com', SHA2('med123',256), 2);


/* ===========================================================
   4. PACIENTES
   =========================================================== */

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

INSERT INTO patients (dni, name, email, password, id_doctor) VALUES
('12345678A', 'Ana Martínez', 'ana@example.com', SHA2('ana123',256), 1),
('87654321B', 'Pedro López', 'pedro@example.com', SHA2('pedro123',256), 1),
('11223344C', 'Carla Torres', 'carla@example.com', SHA2('carla123',256), 2);


/* ===========================================================
   5. CITAS (Médico <-> Paciente)
   =========================================================== */

DROP TABLE IF EXISTS appointments;
CREATE TABLE appointments (
    id_appointment INT AUTO_INCREMENT PRIMARY KEY,
    id_patient INT NOT NULL,
    id_doctor INT NOT NULL,
    date DATETIME NOT NULL,
    status ENUM('pendiente','realizada','cancelada') DEFAULT 'pendiente',
    comments TEXT,
    FOREIGN KEY (id_patient) REFERENCES patients(id_patient)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_doctor) REFERENCES doctors(id_doctor)
        ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO appointments (id_patient, id_doctor, date, status, comments) VALUES
(1, 1, '2025-02-15 10:00:00', 'pendiente', 'Revisión inicial'),
(2, 1, '2025-02-16 12:00:00', 'realizada', 'Control de evolución'),
(3, 2, '2025-02-20 09:30:00', 'pendiente', 'Estudio de lesión');


/* ===========================================================
   6. SOLICITUDES DE FOTOS
   =========================================================== */

DROP TABLE IF EXISTS photo_requests;
CREATE TABLE photo_requests (
    id_request INT AUTO_INCREMENT PRIMARY KEY,
    id_patient INT NOT NULL,
    urgency TINYINT DEFAULT 1,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pendiente','revisando','diagnosticada') DEFAULT 'pendiente',
    FOREIGN KEY (id_patient) REFERENCES patients(id_patient)
        ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO photo_requests (id_patient, urgency, status) VALUES
(1, 3, 'pendiente'),
(2, 1, 'revisando'),
(3, 2, 'pendiente');


/* ===========================================================
   7. IMÁGENES
   =========================================================== */

DROP TABLE IF EXISTS images;
CREATE TABLE images (
    id_image INT AUTO_INCREMENT PRIMARY KEY,
    id_request INT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_request) REFERENCES photo_requests(id_request)
        ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO images (id_request, file_path) VALUES
(1, '/uploads/foto_ana_01.jpg'),
(2, '/uploads/foto_pedro_01.jpg'),
(3, '/uploads/foto_carla_01.jpg');


/* ===========================================================
   8. ENFERMEDADES (ICD)
   =========================================================== */

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


/* ===========================================================
   9. DIAGNÓSTICOS MÉDICOS
   =========================================================== */

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

INSERT INTO diagnoses (id_request, id_doctor, id_patient, id_skindiseases, confidence, doctor_notes) VALUES
(1, 1, 1, 1, 87.5, 'Lesión compatible con acné leve.'),
(2, 1, 2, 2, 92.3, 'Dermatitis en fase activa.'),
(3, 2, 3, 3, 75.0, 'Placas compatibles con psoriasis.');


/* ===========================================================
   FIN DEL SCRIPT
   =========================================================== */
   
