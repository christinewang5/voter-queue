CREATE USER voter WITH PASSWORD 'voting-rocks';
CREATE DATABASE voter_queue;
\connect voter_queue
GRANT ALL PRIVILEGES ON DATABASE voter_queue TO voter;
