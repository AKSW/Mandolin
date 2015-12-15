SET work_mem='4GB';
SET enable_mergejoin=OFF;

-- generate random types for 0 typed entities
--INSERT INTO entClasses
--SELECT tt.ent, trunc(random()*156) AS class
--FROM (SELECT id AS ent FROM entities
--        EXCEPT
--      SELECT ent FROM entClasses) tt;

-- import csv
