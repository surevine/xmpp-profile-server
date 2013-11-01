INSERT INTO "owners" VALUES ('owner@example.com', NOW());

INSERT INTO "roster" VALUES 
    ('owner@example.com', 'family', 'mum@example.com'),
    ('owner@example.com', 'advisor', 'mum@example.com'),
    ('owner@example.com', 'family', 'dad@example.com'),
    ('owner@example.com', 'colleagues', 'boss@company.org'),
    ('owner@example.com', 'colleagues', 'friend@company.org'),
    ('owner@example.com', 'people-i-dont-like', 'boss@company.org'),
    ('owner@example.com', 'friends', 'friend@company.org');
    
INSERT INTO "vcards" VALUES
    ('owner@example.com', '<family-false/>', 'family', false, NOW()),
    ('owner@example.com', '<public-true/>', 'public', true, NOW()),
    ('owner@example.com', '<work-false/>', 'work', false, NOW()),
    ('owner@example.com', '<friends-false/>', 'friends', false, NOW()),
    ('owner@example.com', '<advisor-false/>', 'advisor', false, NOW());
       
INSERT INTO "rostermap" VALUES
    ('owner@example.com', 'family', 'family', 1),
    ('owner@example.com', 'advisor', 'advisor', 2),
    ('owner@example.com', 'work', 'colleagues', 3),
    ('owner@example.com', 'friends', 'family', 4);
