SET work_mem='4GB';
SET enable_mergejoin=OFF;

-- import csv
COPY probkb.classes FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/classes.csv' DELIMITERS ',' CSV;
COPY probkb.entities FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/entities.csv' DELIMITERS ',' CSV;
COPY probkb.relations FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/relations.csv' DELIMITERS ',' CSV;
COPY probkb.entClasses FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/entClasses.csv' DELIMITERS ',' CSV;
COPY probkb.relClasses FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/relClasses.csv' DELIMITERS ',' CSV;
COPY probkb.functionals FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/functionals.csv' DELIMITERS ',' CSV;

-- generate random types for 0 typed entities
--INSERT INTO entClasses
--SELECT tt.ent, trunc(random()*156) AS class
--FROM (SELECT id AS ent FROM entities
--        EXCEPT
--      SELECT ent FROM entClasses) tt;

COPY probkb.extractions FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/relationships.csv' DELIMITERS ',' CSV;

-- build relationships table with type information
CREATE TABLE probkb.relationships AS
SELECT nextval('probkb.relids') AS id, r.rel AS rel,
       r.ent1 AS ent1, rc.class1 AS class1,
       r.ent2 AS ent2, rc.class2 AS class2, AVG(weight) AS weight
FROM probkb.extractions r, probkb.relClasses rc, probkb.entClasses ec1, probkb.entClasses ec2
WHERE r.rel = rc.rel
AND r.ent1 = ec1.ent AND ec1.class = rc.class1
AND r.ent2 = ec2.ent AND ec2.class = rc.class2
GROUP BY r.rel, r.ent1, rc.class1, r.ent2, rc.class2;
CREATE INDEX relationships_rel_idx ON probkb.relationships(rel);
CLUSTER probkb.relationships USING relationships_rel_idx;

DELETE FROM probkb.relationships WHERE ent1 = ent2;

SELECT probkb.qc();

COPY probkb.mln1 FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/mln1.csv' DELIMITERS ',' CSV;
COPY probkb.mln2 FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/mln2.csv' DELIMITERS ',' CSV;
COPY probkb.mln3 FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/mln3.csv' DELIMITERS ',' CSV;
COPY probkb.mln4 FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/mln4.csv' DELIMITERS ',' CSV;
COPY probkb.mln5 FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/mln5.csv' DELIMITERS ',' CSV;
COPY probkb.mln6 FROM '/Users/tom/PhD/srl/Mandolin/eval/10_publi-probkb/mln6.csv' DELIMITERS ',' CSV;

ANALYZE probkb.relationships;  -- gather statistics for better query plan
ANALYZE probkb.mln1;
ANALYZE probkb.mln2;
ANALYZE probkb.mln3;
ANALYZE probkb.mln4;
ANALYZE probkb.mln5;
ANALYZE probkb.mln6;
