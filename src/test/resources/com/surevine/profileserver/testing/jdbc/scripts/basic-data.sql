INSERT INTO owners VALUES ('owner@example.com', NOW());

INSERT INTO roster VALUES 
    ('owner@example.com', 'family', 'mum@example.com'),
    ('owner@example.com', 'advisor', 'mum@example.com'),
    ('owner@example.com', 'family', 'dad@example.com'),
    ('owner@example.com', 'colleagues', 'boss@company.org'),
    ('owner@example.com', 'colleagues', 'friend@company.org'),
    ('owner@example.com', 'people-i-dont-like', 'boss@company.org'),
    ('owner@example.com', 'friends', 'friend@company.org');
    
INSERT INTO vcards VALUES
    ('owner@example.com', '<family-false-1/>', 'family', false, 1, NOW()),
    ('owner@example.com', '<public-true-2/>', 'public', true, 2, NOW()),
    ('owner@example.com', '<work-false-3/>', 'work', false, 3, NOW()),
    ('owner@example.com', '<friends-false-4/>', 'friends', false, 4, NOW()),
    ('owner@example.com', '<advisor-false-5/>', 'advisor', false, 5, NOW());
       
INSERT INTO rostermap VALUES
    ('owner@example.com', 'family', 'family'),
    ('owner@example.com', 'advisor', 'advisor'),
    ('owner@example.com', 'work', 'colleagues');
