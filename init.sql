-- Database initialization script for Big Data Hackathon
-- This script creates tables based on JPA entities

-- Drop tables if exist (for clean setup)
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS complaints CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE
);

-- Create user_roles table (for @ElementCollection)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index on user_roles
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- Create complaints table
CREATE TABLE complaints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    raw_text TEXT,
    route TEXT,
    object TEXT,
    time TIMESTAMP WITH TIME ZONE,
    place TEXT,
    actor TEXT,
    aspect TEXT[],
    priority TEXT,
    evidence TEXT[],
    confidence DOUBLE PRECISION,
    created_by VARCHAR(100),
    status VARCHAR(32) DEFAULT 'NEW',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    user_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes on complaints table
CREATE INDEX idx_complaints_route ON complaints(route);
CREATE INDEX idx_complaints_priority ON complaints(priority);
CREATE INDEX idx_complaints_actor ON complaints(actor);
CREATE INDEX idx_complaints_place ON complaints(place);
CREATE INDEX idx_complaints_created_by ON complaints(created_by);
CREATE INDEX idx_complaints_status ON complaints(status);
CREATE INDEX idx_complaints_user_id ON complaints(user_id);
CREATE INDEX idx_complaints_created_at ON complaints(created_at);

-- Insert sample admin user (password: admin123)
-- BCrypt hash for "admin123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (username, password, email, enabled, created_at) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@bdh.kz', true, NOW());

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role) 
VALUES ((SELECT id FROM users WHERE username = 'admin'), 'ADMIN');

-- Insert sample resident user (password: user123)
-- BCrypt hash for "user123": $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a
INSERT INTO users (username, password, email, enabled, created_at) 
VALUES ('resident', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'resident@bdh.kz', true, NOW());

-- Assign USER role to resident user
INSERT INTO user_roles (user_id, role) 
VALUES ((SELECT id FROM users WHERE username = 'resident'), 'USER');

-- Display created users
SELECT u.id, u.username, u.email, array_agg(r.role) as roles 
FROM users u 
LEFT JOIN user_roles r ON u.id = r.user_id 
GROUP BY u.id, u.username, u.email;

COMMIT;

