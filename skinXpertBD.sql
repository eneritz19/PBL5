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
('Clinica Dermatológica Central', 'Av. Salud 123'),
('Clinica Dermatologica Central', 'Av. Salud 123'),
('Clinica Especialistas del Sur', 'Calle Medicina 45');


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
   3. MEDICOS
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
('MED001', 'Dr. Juan Perez', 'jperez@clinic.com', SHA2('med123',256), 1),
('MED002', 'Dra. Laura Gomez', 'lgomez@clinic.com', SHA2('med123',256), 1),
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
('12345678A', 'Ana Martinez', 'ana@example.com', SHA2('ana123',256), 1),
('87654321B', 'Pedro Lopez', 'pedro@example.com', SHA2('pedro123',256), 1),
('11223344C', 'Carla Torres', 'carla@example.com', SHA2('carla123',256), 2);


/* ===========================================================
   5. CITAS (Medico <-> Paciente)
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
(1, 1, '2025-02-15 10:00:00', 'pendiente', 'Revision inicial'),
(2, 1, '2025-02-16 12:00:00', 'realizada', 'Control de evolucion'),
(3, 2, '2025-02-20 09:30:00', 'pendiente', 'Estudio de lesion');


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
   7. IMAGENES
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
   8. ENFERMEDADES 
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
-- 1) Acne_Rosacea
('Acne_Rosacea', 'L70.0, L71',
'Higiene + tratamiento tópico; antibióticos si inflamatorio moderado-severo.',
'Retinoides tópicos, Peróxido de benzoilo, Doxiciclina, Isotretinoína, Metronidazol tópico',
'Terapias hormonales, Láser vascular',
'Evitar irritantes y desencadenantes; seguimiento dermatológico',
'Dermatología',
'Guías dermatología'),

-- 2) AK
('AK', 'L57.0',
'Crioterapia o tratamientos tópicos; seguimiento por riesgo de progresión.',
'5-FU, Imiquimod, Diclofenaco gel, Terapia fotodinámica',
'PDT, Láser, Observación en casos seleccionados',
'Fotoprotección estricta; revisiones periódicas',
'Dermatología',
'Guías cáncer cutáneo'),

-- 3) BCC
('BCC', 'C44.x',
'Escisión quirúrgica o técnicas destructivas según localización/tamaño.',
'Imiquimod (casos superficiales), Vismodegib (avanzados seleccionados)',
'Cirugía de Mohs, Radioterapia (seleccionados)',
'Fotoprotección; control dermatológico',
'Dermatología / Oncología',
'Guías cáncer cutáneo'),

-- 4) MEL
('MEL', 'C43',
'Escisión + estadificación; manejo oncológico si avanzado.',
'Pembrolizumab, Nivolumab; terapias dirigidas BRAF/MEK si aplica',
'Ensayos clínicos, Quimioterapia (casos seleccionados)',
'Evitar sol; seguimiento estrecho',
'Oncología (urgente)',
'Guías melanoma'),

-- 5) SCC
('SCC', 'C44.x',
'Escisión quirúrgica; estadificación si alto riesgo.',
'5-FU tópico (in situ seleccionados), Inmunoterapia (avanzados seleccionados)',
'Cirugía de Mohs, Radioterapia (seleccionados)',
'Fotoprotección; revisiones periódicas',
'Oncología / Dermatología (urgente)',
'Guías carcinoma escamoso'),

-- 6) Benign_Tumor
('Benign_Tumor', 'D23.x, L82.x',
'Observación o eliminación si molesto/sospechoso.',
'No suele requerir medicación',
'Crioterapia, Curetaje, Láser',
'Monitorizar cambios',
'Dermatología',
'Guías dermatología'),

-- 7) Dermatitis
('Dermatitis', 'L20, L30',
'Emolientes + corticoide tópico; escalado si grave.',
'Corticoides tópicos, Tacrolimus/Pimecrolimus, Dupilumab (grave)',
'Fototerapia, inmunosupresión seleccionada',
'Hidratación; evitar irritantes/alérgenos',
'Dermatología',
'Guías dermatitis'),

-- 8) Fungal_Infection
('Fungal_Infection', 'B35.x, B37.x',
'Antifúngicos tópicos u orales según extensión.',
'Terbinafina, Itraconazol, Azoles tópicos',
'Desbridamiento en casos seleccionados',
'Higiene; evitar humedad',
'Dermatología',
'Guías micosis'),

-- 9) Hair_Disorder
('Hair_Disorder', 'L63, L66',
'Diagnóstico etiológico; tratamientos tópicos/infiltrados según caso.',
'Minoxidil, Corticoides, Inmunomoduladores (según etiología)',
'Trasplante capilar, láser (seleccionados)',
'Buscar causas sistémicas; apoyo psicológico si precisa',
'Dermatología',
'Guías alopecia'),

-- 10) Nevus
('Nevus', 'D22',
'Observación; extirpación si atípico/sospechoso.',
'No requiere medicación',
'Dermatoscopia seriada, excisión si criterios',
'Vigilar ABCDE; fotoprotección',
'Dermatología',
'Referencias nevos'),

-- 11) Psoriasis
('Psoriasis', 'L40, L43',
'Tópicos → fototerapia → sistémicos/biológicos según severidad.',
'Corticoides tópicos, Calcipotriol, Metotrexato, Biológicos (según caso)',
'Fototerapia UVB, Apremilast',
'Control comorbilidades; adherencia al tratamiento',
'Dermatología',
'Guías psoriasis'),

-- 12) Systemic
('Systemic', 'Variable',
'Tratamiento de la enfermedad sistémica subyacente.',
'Según etiología (derivar para estudio)',
'Manejo multidisciplinar',
'Detectar síntomas sistémicos; seguimiento',
'Medicina interna',
'Guías multidisciplinares'),

-- 13) Urticaria
('Urticaria', 'L50',
'Antihistamínicos H1; escalado si refractaria.',
'Cetirizina/Loratadina; Omalizumab (refractaria)',
'Corticoides cortos (seleccionados)',
'Identificar desencadenantes',
'Alergología / Dermatología',
'Guías urticaria'),

-- 14) Viral_Infection
('Viral_Infection', 'B07, A63',
'Terapias destructivas o tópicas según lesión.',
'Ácido salicílico, Imiquimod, Crioterapia',
'Observación (niños/lesiones pequeñas), inmunoterapia tópica',
'Consejos contagio; prevención',
'Dermatología',
'Guías infecciones virales');

/* ===========================================================
   9. DIAGNOSTICOS MEDICOS
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
(1, 1, 1, 1, 87.5, 'Lesion compatible con acne leve.'),
(2, 1, 2, 2, 92.3, 'Dermatitis en fase activa.'),
(3, 2, 3, 3, 75.0, 'Placas compatibles con psoriasis.');

   
