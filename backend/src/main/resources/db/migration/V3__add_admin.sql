INSERT INTO users (username, email, password, role, score)
VALUES (
    'admin',
    'admin@grooveguess.com',
    '$2a$12$cm8CSZrUT8ijNESCPQZTnOGdMZxroZL8Up9JlXzsVrSi68GEJD6Ci', 
    'ADMIN',
    0
)
ON CONFLICT (email) DO NOTHING;