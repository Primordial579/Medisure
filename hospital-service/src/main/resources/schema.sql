CREATE TABLE IF NOT EXISTS patients (
    patient_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dob DATE,
    phone_no VARCHAR(20),
    email VARCHAR(255),
    blood_group VARCHAR(20),
    insurance_id VARCHAR(255),
    health_report TEXT,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hospital_users (
    user_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(100) UNIQUE,
    name VARCHAR(255) NOT NULL,
    hospital_name VARCHAR(255),
    email VARCHAR(255),
    phone_no VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);