
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

