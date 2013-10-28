CREATE TABLE schema_version ("version" INT NOT NULL PRIMARY KEY,
                             "when" TIMESTAMP,
                             "description" TEXT);
       
CREATE TABLE owners ("owner" TEXT NOT NULL,
    "last_updated" TIMESTAMP,
    PRIMARY KEY ("owner"));
    
CREATE TABLE vcards ("owner" TEXT NOT NULL REFERENCES owners("owner") ON DELETE CASCADE,
    vcard TEXT,
    "name" TEXT NOT NULL,
    "default" BOOLEAN DEFAULT false,
    "priority" INT NOT NULL,
    "last_updated" TIMESTAMP,
    PRIMARY KEY ("owner", "name"));

CREATE TABLE roster ("owner" TEXT NOT NULL REFERENCES owners("owner") ON DELETE CASCADE,
    "group" TEXT NOT NULL,
    "user" TEXT NOT NULL,
    PRIMARY KEY ("owner", "user", "group"));

CREATE TABLE rostermap ("owner" TEXT NOT NULL REFERENCES owners("owner") ON DELETE CASCADE,
    "group" TEXT NOT NULL,
    "vcard" TEXT NOT NULL,
    PRIMARY KEY ("owner", "group"));