-- =====================================================
-- LawBlox Database
-- =====================================================

CREATE DATABASE IF NOT EXISTS lawblox_dbb
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE lawblox_dbb;

-- =====================================================
-- TABLE 1: users (Login / Signup)
-- =====================================================
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABLE 2: chat_sessions
-- One session per chat screen visit
-- =====================================================
CREATE TABLE chat_sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE SET NULL
);

-- =====================================================
-- TABLE 3: chat_messages
-- Stores user + bot messages
-- =====================================================
CREATE TABLE chat_messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    sender ENUM('USER', 'BOT') NOT NULL,
    message_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id)
        ON DELETE CASCADE
);

-- =====================================================
-- TABLE 4: legal_categories
-- Example: Constitutional Law, Property Law, etc.
-- =====================================================
CREATE TABLE legal_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- =====================================================
-- TABLE 5: keywords
-- Individual detectable words
-- =====================================================
CREATE TABLE keywords (
    keyword_id INT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL UNIQUE
);

-- =====================================================
-- TABLE 6: keyword_category_map
-- Many keywords → one category
-- =====================================================
CREATE TABLE keyword_category_map (
    map_id INT AUTO_INCREMENT PRIMARY KEY,
    keyword_id INT NOT NULL,
    category_id INT NOT NULL,
    FOREIGN KEY (keyword_id) REFERENCES keywords(keyword_id)
        ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES legal_categories(category_id)
        ON DELETE CASCADE
);

-- =====================================================
-- TABLE 7: keyword_responses
-- Hardcoded bot replies (NOT AI)
-- =====================================================
CREATE TABLE keyword_responses (
    response_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    response_text TEXT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES legal_categories(category_id)
        ON DELETE CASCADE
);
INSERT INTO legal_categories (category_name, description) VALUES
('Constitutional Law', 'Fundamental rights, police powers, Constitution of India'),
('Criminal Law', 'IPC, CrPC, FIR, arrest, bail'),
('Property Law', 'Land disputes, ownership, tenancy'),
('Family Law', 'Marriage, divorce, maintenance, custody'),
('Labour Law', 'Employment, wages, termination'),
('Consumer Law', 'Consumer protection, deficiency of service'),
('Cyber Law', 'IT Act, online fraud, data misuse'),
('Intellectual Property', 'Copyright, trademark, patents'),
('Environmental Law', 'Pollution, wildlife, environment protection'),
('Administrative Law', 'Government actions, authorities, public officials');

INSERT INTO keywords (keyword) VALUES
-- Constitutional
('police'),
('warrant'),
('fundamental rights'),
('article 21'),
('illegal detention'),

-- Criminal
('fir'),
('arrest'),
('bail'),
('ipc'),
('crpc'),
('theft'),
('assault'),
('cheating'),
('dowry'),
('domestic violence'),

-- Property
('property'),
('land'),
('boundary'),
('encroachment'),
('tenant'),
('rent'),
('eviction'),

-- Family
('divorce'),
('maintenance'),
('custody'),
('alimony'),
('marriage'),
('domestic abuse'),

-- Labour
('salary'),
('termination'),
('wrongful dismissal'),
('minimum wage'),
('overtime'),

-- Consumer
('consumer'),
('refund'),
('defective product'),
('service deficiency'),
('consumer court'),

-- Cyber
('online fraud'),
('cyber crime'),
('identity theft'),
('data leak'),
('hacking'),

-- IP
('copyright'),
('trademark'),
('patent'),
('logo theft'),

-- Environment
('pollution'),
('illegal construction'),
('forest land'),

-- Administrative
('government notice'),
('public authority'),
('rtI');

-- Constitutional Law (1)
INSERT INTO keyword_category_map VALUES
(NULL, 1, 1),
(NULL, 2, 1),
(NULL, 3, 1),
(NULL, 4, 1),
(NULL, 5, 1);

-- Criminal Law (2)
INSERT INTO keyword_category_map VALUES
(NULL, 6, 2),
(NULL, 7, 2),
(NULL, 8, 2),
(NULL, 9, 2),
(NULL, 10, 2),
(NULL, 11, 2),
(NULL, 12, 2),
(NULL, 13, 2),
(NULL, 14, 2),
(NULL, 15, 2);

-- Property Law (3)
INSERT INTO keyword_category_map VALUES
(NULL, 16, 3),
(NULL, 17, 3),
(NULL, 18, 3),
(NULL, 19, 3),
(NULL, 20, 3),
(NULL, 21, 3),
(NULL, 22, 3);

-- Family Law (4)
INSERT INTO keyword_category_map VALUES
(NULL, 23, 4),
(NULL, 24, 4),
(NULL, 25, 4),
(NULL, 26, 4),
(NULL, 27, 4),
(NULL, 28, 4);

-- Labour Law (5)
INSERT INTO keyword_category_map VALUES
(NULL, 29, 5),
(NULL, 30, 5),
(NULL, 31, 5),
(NULL, 32, 5),
(NULL, 33, 5);

-- Consumer Law (6)
INSERT INTO keyword_category_map VALUES
(NULL, 34, 6),
(NULL, 35, 6),
(NULL, 36, 6),
(NULL, 37, 6),
(NULL, 38, 6);

-- Cyber Law (7)
INSERT INTO keyword_category_map VALUES
(NULL, 39, 7),
(NULL, 40, 7),
(NULL, 41, 7),
(NULL, 42, 7),
(NULL, 43, 7);

-- Intellectual Property (8)
INSERT INTO keyword_category_map VALUES
(NULL, 44, 8),
(NULL, 45, 8),
(NULL, 46, 8),
(NULL, 47, 8);

-- Environmental Law (9)
INSERT INTO keyword_category_map VALUES
(NULL, 48, 9),
(NULL, 49, 9),
(NULL, 50, 9);

-- Administrative Law (10)
INSERT INTO keyword_category_map VALUES
(NULL, 51, 10),
(NULL, 52, 10),
(NULL, 53, 10);

INSERT INTO keyword_responses (category_id, response_text) VALUES

(1, 'This issue may involve Constitutional Law under the Constitution of India, particularly fundamental rights such as Article 21 (Right to Life and Personal Liberty).'),

(2, 'Your concern appears related to Criminal Law under the Indian Penal Code (IPC) or Criminal Procedure Code (CrPC). You may need to check FIR, arrest, or bail provisions.'),

(3, 'This seems to be a Property Law issue involving land, tenancy, or boundary disputes. Relevant laws include Transfer of Property Act and Rent Control Acts.'),

(4, 'This issue may fall under Family Law, covering matters such as divorce, maintenance, custody, or domestic violence under Indian personal laws.'),

(5, 'This concern appears related to Labour Law, including wrongful termination, wages, or employment rights under Indian labour legislation.'),

(6, 'This appears to be a Consumer Law matter. You may approach the Consumer Protection Act remedies for defective goods or deficiency of services.'),

(7, 'Your issue may involve Cyber Law under the Information Technology Act, 2000, such as online fraud, identity theft, or data misuse.'),

(8, 'This matter appears related to Intellectual Property Rights in India, including copyright, trademark, or patent protection.'),

(9, 'This issue may involve Environmental Law, including pollution control or illegal construction under Indian environmental regulations.'),

(10, 'This concern relates to Administrative Law, involving actions of government authorities or public officials. You may have remedies under writ jurisdiction.');

ALTER TABLE chat_messages
ADD COLUMN bot_response TEXT,
ADD COLUMN detected_keywords TEXT;

ALTER TABLE chat_messages
ADD COLUMN user_id BIGINT;
ALTER TABLE chat_messages
ADD CONSTRAINT fk_user
FOREIGN KEY (user_id) REFERENCES users(user_id)
ON DELETE SET NULL;

USE lawblox_dbb;
ALTER TABLE chat_messages
DROP FOREIGN KEY chat_messages_ibfk_1;
ALTER TABLE chat_messages
MODIFY session_id VARCHAR(100) NULL;
SHOW CREATE TABLE chat_messages



