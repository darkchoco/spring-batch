-- INSERT INTO medicine VALUES ('ec278dd3-87b9-4ad1-858f-dfe5bc34bdb5', 'Lidocaine', 'ANESTHETICS', DATEADD('DAY', 120, CURRENT_DATE), 10, null);
-- INSERT INTO medicine VALUES ('9d39321d-34f3-4eb7-bb9a-a69734e0e372', 'Flucloxacillin', 'ANTIBACTERIALS', DATEADD('DAY', 40, CURRENT_DATE), 20, null);
-- INSERT INTO medicine VALUES ('87f4ff13-de40-4c7f-95db-627f309394dd', 'Amoxicillin', 'ANTIBACTERIALS', DATEADD('DAY', 70, CURRENT_DATE), 30, null);
-- INSERT INTO medicine VALUES ('acd99d6a-27be-4c89-babe-0edf4dca22cb', 'Prozac', 'ANTIDEPRESSANTS', DATEADD('DAY', 30, CURRENT_DATE), 40, null);

INSERT INTO medicine VALUES ('ec278dd3-87b9-4ad1-858f-dfe5bc34bdb5', 'Lidocaine', 'ANESTHETICS', CURRENT_DATE +  120 * INTERVAL '1 day', 10, null);
INSERT INTO medicine VALUES ('9d39321d-34f3-4eb7-bb9a-a69734e0e372', 'Flucloxacillin', 'ANTIBACTERIALS', CURRENT_DATE +  40 * INTERVAL '1 day', 20, null);
INSERT INTO medicine VALUES ('87f4ff13-de40-4c7f-95db-627f309394dd', 'Amoxicillin', 'ANTIBACTERIALS', CURRENT_DATE +  70 * INTERVAL '1 day', 30, null);
INSERT INTO medicine VALUES ('acd99d6a-27be-4c89-babe-0edf4dca22cb', 'Prozac', 'ANTIDEPRESSANTS', CURRENT_DATE +  30 * INTERVAL '1 day', 40, null);