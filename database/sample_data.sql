-- Insert sample events
INSERT INTO events (title, description, start_time, end_time, location) VALUES
('Team Meeting', 'Weekly team sync-up meeting', '2024-03-20 10:00:00', '2024-03-20 11:00:00', 'Conference Room A'),
('Project Deadline', 'Submit final project deliverables', '2024-03-25 17:00:00', '2024-03-25 18:00:00', 'Office'),
('Client Presentation', 'Present quarterly results to client', '2024-03-28 14:00:00', '2024-03-28 15:30:00', 'Virtual Meeting Room');

-- Insert sample event media
INSERT INTO event_media (event_id, type, url, description) VALUES
(1, 'IMAGE', 'https://example.com/meeting-room.jpg', 'Meeting room setup'),
(2, 'DOCUMENT', 'https://example.com/project-docs.pdf', 'Project documentation'),
(3, 'PRESENTATION', 'https://example.com/quarterly-slides.pptx', 'Quarterly results presentation');

-- Insert sample event notes
INSERT INTO event_notes (event_id, content) VALUES
(1, 'Prepare agenda and send to team members'),
(2, 'Review all deliverables before submission'),
(3, 'Practice presentation and prepare Q&A'); 