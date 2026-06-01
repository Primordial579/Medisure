CREATE TABLE IF NOT EXISTS insurances (
    insurance_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT,
    dob DATE,
    phone_no VARCHAR(20),
    mail_id VARCHAR(100),
    blood_group VARCHAR(10),
    plan_coverage DECIMAL(12, 2),
    remaining_coverage DECIMAL(12, 2),
    preauth_percentage DECIMAL(5, 2),
    start_date DATE,
    end_date DATE,
    policy_type VARCHAR(50),
    created_by VARCHAR(100),
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS insurance_users (
    user_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(100) UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone_no VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);