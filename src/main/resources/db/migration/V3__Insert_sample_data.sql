-- Migration V3: Insert sample data
INSERT INTO app_user (username, email, avatar_url) VALUES 
    ('Jan Kowalski', 'jan.kowalski@example.com', 'https://randomuser.me/api/portraits/men/1.jpg'),
    ('Anna Nowak', 'anna.nowak@example.com', 'https://randomuser.me/api/portraits/women/2.jpg'),
    ('Piotr Wiśniewski', 'piotr.wisniewski@example.com', 'https://randomuser.me/api/portraits/men/3.jpg');

INSERT INTO task (title, description, due_date, status, priority, user_id, change_log) VALUES 
    ('Implementacja logowania', 'Dodanie systemu logowania użytkowników', '2025-06-15', 'TODO', 'HIGH', 1, '[]'),
    ('Optymalizacja bazy danych', 'Analiza i optymalizacja zapytań SQL', '2025-06-20', 'IN_PROGRESS', 'MEDIUM', 2, '[]'),
    ('Testy jednostkowe', 'Napisanie testów dla API', '2025-06-10', 'DONE', 'HIGH', 3, '[]'),
    ('Dokumentacja API', 'Utworzenie dokumentacji OpenAPI', '2025-06-25', 'TODO', 'LOW', 1, '[]'); 