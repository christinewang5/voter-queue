DROP TABLE IF EXISTS CASCADE vote;
CREATE TABLE vote (
    vote_uuid uuid PRIMARY KEY,
    precinct INT,
    start_time DATE
);
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO voter;
