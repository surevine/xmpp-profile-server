CREATE TABLE schema_version (version INT NOT NULL PRIMARY KEY,
                             "when" TIMESTAMP,
                             description TEXT);
INSERT INTO schema_version (version, "when", description)
       VALUES (1, NOW(), 'Initial install');
       
CREATE TABLE owners ("owner" TEXT NOT NULL,
    last_updated TIMESTAMP,
    PRIMARY KEY ("owner"));
    
CREATE TABLE vcards ("owner" TEXT NOT NULL REFERENCES owners("owner"),
    vcard TEXT,
    name TEXT NOT NULL,
    "default" BOOLEAN DEFAULT false,
    priority INT NOT NULL,
    last_updated TIMESTAMP,
    PRIMARY KEY ("owner", name));

CREATE TABLE roster ("owner" TEXT NOT NULL REFERENCES owners("owner"),
    "group" TEXT NOT NULL,
    "user" TEXT NOT NULL,
    PRIMARY KEY ("owner", "user"));

CREATE TABLE rostermap ("owner" TEXT NOT NULL REFERENCES owners("owner"),
    "group" TEXT NOT NULL,
    vcard TEXT NOT NULL,
    PRIMARY KEY ("owner", "group"));