DROP TABLE vote;
DROP TABLE complete_vote;
DROP TABLE precinct_names;
DROP TABLE csv_log;

-- contains the start vote time
CREATE TABLE vote (
    uuid uuid PRIMARY KEY,
    precinct INT,
    startTime TIMESTAMP
);

-- contains the total wait time
CREATE TABLE complete_vote (
    uuid uuid PRIMARY KEY,
    precinct INT,
    waitTime INT -- wait time in minutes
);

CREATE TABLE precinct_names (
    precinct INT,
    name TEXT
);

CREATE TABLE csv_log (
    timeStamp TIMESTAMP,
    eventName TEXT,
    uuid1 uuid,
    uuid2 uuid,
    precinct1 INT,
    precinct2 INT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO voter;