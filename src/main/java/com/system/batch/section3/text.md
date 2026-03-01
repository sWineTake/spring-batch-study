
---
docker run --name postgres-batch \
-e POSTGRES_USER=root \
-e POSTGRES_PASSWORD=password \
-p 5432:5432 \
-d postgres:latest
---


CREATE TABLE victims (
id BIGSERIAL PRIMARY KEY,
name VARCHAR(255),
process_id VARCHAR(50),
terminated_at TIMESTAMP,
status VARCHAR(20)
);

INSERT INTO victims (name, process_id, terminated_at, status) VALUES
('zombie_process', 'PID_12345', '2024-01-01 12:00:00', 'TERMINATED'),
('sleeping_thread', 'PID_45678', '2024-01-15 15:30:00', 'TERMINATED'),
('memory_leak', 'PID_98765', '2024-02-01 09:15:00', 'RUNNING'),
('infinite_loop', 'PID_24680', '2024-02-15 18:45:00', 'RUNNING');

