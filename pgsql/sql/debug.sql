-------------------------------------------------------
-- DEBUGGING utilities
-------------------------------------------------------
CREATE OR REPLACE FUNCTION probkb.trace(rsid INT) RETURNS VOID AS $$
DECLARE
  cnt INT := 1;
  target INT;
  rule RECORD;
BEGIN
  DROP TABLE IF EXISTS probkb.tr, probkb.queue;
  CREATE TABLE probkb.tr(head TEXT, body1 TEXT, body2 TEXT);

  CREATE TABLE probkb.queue(id INT PRIMARY KEY);
  INSERT INTO probkb.queue VALUES (rsid);

  WHILE cnt > 0 LOOP
    SELECT MAX(id) INTO target FROM probkb.queue;
    SELECT (probkb.traceStep(target)).* INTO rule;
    RAISE INFO '(%) %:-%,%', rule.id1, rule.name1, rule.name2, rule.name3;

    INSERT INTO probkb.tr(head, body1, body2) VALUES (rule.name1, rule.name2, rule.name3);
    INSERT INTO probkb.queue
    SELECT rule.id2 WHERE rule.id2 IS NOT NULL
      UNION
    SELECT rule.id3 WHERE rule.id3 IS NOT NULL
      EXCEPT
    SELECT id FROM probkb.queue;

    DELETE FROM probkb.queue WHERE id = target;
    SELECT COUNT(*) INTO cnt FROM probkb.queue;
  END LOOP;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION probkb.traceStep(rsid INT, OUT id1 INT, OUT name1 TEXT,
                                                      OUT id2 INT, OUT name2 TEXT,
                                                      OUT id3 INT, OUT name3 TEXT) AS $$
DECLARE
  factor RECORD;
BEGIN
  SELECT factors.id1, factors.id2, factors.id3 INTO factor FROM probkb.factors
  WHERE factors.id1 = rsid AND factors.id2 < rsid AND factors.id3 < rsid
  ORDER BY (factors.id2+factors.id3) LIMIT 1;

  SELECT INTO id1, name1
    r.id, relations.name || '(' || e1.name || ',' || e2.name || ')'
  FROM probkb.relationships r JOIN probkb.relations ON r.rel = relations.id
                              JOIN probkb.entities e1 ON r.ent1 = e1.id
                              JOIN probkb.entities e2 ON r.ent2 = e2.id
  WHERE r.id = rsid;

  SELECT INTO id2, name2
    r.id, relations.name || '(' || e1.name || ',' || e2.name || ')'
  FROM probkb.relationships r JOIN probkb.relations ON r.rel = relations.id
                              JOIN probkb.entities e1 ON r.ent1 = e1.id
                              JOIN probkb.entities e2 ON r.ent2 = e2.id
  WHERE r.id = factor.id2;

  SELECT INTO id3, name3
    r.id, relations.name || '(' || e1.name || ',' || e2.name || ')'
  FROM probkb.relationships r JOIN probkb.relations ON r.rel = relations.id
                              JOIN probkb.entities e1 ON r.ent1 = e1.id
                              JOIN probkb.entities e2 ON r.ent2 = e2.id
  WHERE r.id = factor.id3;

  IF id2 IS NULL THEN   -- look for urls
    SELECT extractions.url INTO name2
    FROM probkb.relationships JOIN probkb.extractions ON relationships.rel = extractions.rel
    AND relationships.ent1 = extractions.ent1 AND relationships.ent2 = extractions.ent2
    WHERE relationships.id = rsid LIMIT 1;
  END IF;
END;
$$ LANGUAGE plpgsql;
