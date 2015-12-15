CREATE OR REPLACE FUNCTION probkb.qc() RETURNS BIGINT AS $$
DECLARE
  deleted1 BIGINT := 0;
  deleted2 BIGINT := 0;
BEGIN
  -- Detecting ambiguity.
  INSERT INTO probkb.ambiguities
  SELECT DISTINCT r.ent1, r.class1
  FROM   probkb.relationships r JOIN probkb.functionals f ON r.rel = f.rel
  WHERE  f.arg = 1
  GROUP  BY  r.rel, ent1, class1, class2
  HAVING COUNT(*) > MIN(f.deg)
    EXCEPT
  SELECT ent, class FROM probkb.ambiguities;

  -- Remove ambiguous entities.
  DELETE FROM probkb.relationships
  WHERE (ent1, class1) IN (
    SELECT ent, class FROM probkb.ambiguities
  );
  GET DIAGNOSTICS deleted1 = ROW_COUNT;

  DELETE FROM probkb.relationships
  WHERE (ent2, class2) IN (
    SELECT ent, class FROM probkb.ambiguities
  );
  GET DIAGNOSTICS deleted2 = ROW_COUNT;

  RETURN deleted1 + deleted2;
END;
$$ LANGUAGE plpgsql;
