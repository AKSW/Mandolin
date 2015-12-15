-- infer more facts
CREATE OR REPLACE FUNCTION probkb.ground() RETURNS BIGINT AS $$
DECLARE
  inferred BIGINT := 1;
  timer TEXT;
BEGIN
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'start %', timer;

  ---------- Type I -------------------------------------------------------------
  CREATE TABLE probkb.t1 AS
  SELECT mln1.head AS rel, r.ent1 AS ent1, r.class1 AS class1, 
         r.ent2 AS ent2, r.class2 AS class2
  FROM probkb.mln1
    JOIN probkb.relationships r ON mln1.body = r.rel AND r.class1 = mln1.class1 AND r.class2 = mln1.class2
  WHERE r.ent1 <> r.ent2;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type I: %; % rows inserted.', timer, inferred;

  ---------- Type II ------------------------------------------------------------
  CREATE TABLE probkb.t2 AS
  SELECT mln2.head AS rel, r.ent2 AS ent1, r.class2 AS class1,
         r.ent1 AS ent2, r.class1 AS class2
  FROM probkb.mln2
    JOIN probkb.relationships r ON mln2.body = r.rel AND r.class1 = mln2.class2 AND r.class2 = mln2.class1
  WHERE r.ent1 <> r.ent2;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type II: %; % rows inserted.', timer, inferred;

  ---------- Type III -----------------------------------------------------------
  CREATE TABLE probkb.t3 AS
  SELECT mln3.head AS rel, r1.ent1 AS ent1, r1.class1 AS class1,
         r2.ent1 AS ent2, r2.class1 AS class2
  FROM probkb.mln3
    JOIN probkb.relationships r1 ON mln3.body1 = r1.rel AND mln3.class1 = r1.class1 AND mln3.class3 = r1.class2
    JOIN probkb.relationships r2 ON mln3.body2 = r2.rel AND mln3.class2 = r2.class1 AND mln3.class3 = r2.class2
  WHERE r1.ent2 = r2.ent2 AND r1.ent1 <> r2.ent1;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type III: %; % rows inserted.', timer, inferred;

  ---------- Type IV ------------------------------------------------------------
  CREATE TABLE probkb.t4 AS
  SELECT mln4.head AS rel, r1.ent1 AS ent1, r1.class1 AS class1,
         r2.ent2 AS ent2, r2.class2 AS class2
  FROM probkb.mln4
    JOIN probkb.relationships r1 ON mln4.body1 = r1.rel AND mln4.class1 = r1.class1 AND mln4.class3 = r1.class2
    JOIN probkb.relationships r2 ON mln4.body2 = r2.rel AND mln4.class3 = r2.class1 AND mln4.class2 = r2.class2
  WHERE r1.ent2 = r2.ent1 AND r1.ent1 <> r2.ent2;
  
  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type IV: %; % rows inserted.', timer, inferred;

  ---------- Type V -------------------------------------------------------------
  CREATE TABLE probkb.t5 AS
  SELECT mln5.head AS rel, r1.ent2 AS ent1, r1.class2 AS class1,
         r2.ent1 AS ent2, r2.class1 AS class2
  FROM probkb.mln5
    JOIN probkb.relationships r1 ON mln5.body1 = r1.rel AND mln5.class3 = r1.class1 AND mln5.class1 = r1.class2
    JOIN probkb.relationships r2 ON mln5.body2 = r2.rel AND mln5.class2 = r2.class1 AND mln5.class3 = r2.class2
  WHERE r1.ent1 = r2.ent2 AND r1.ent2 <> r2.ent1;
  
  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type V: %; % rows inserted.', timer, inferred;

  ---------- Type VI ------------------------------------------------------------
  CREATE TABLE probkb.t6 AS
  SELECT mln6.head AS rel, r1.ent2 AS ent1, r1.class2 AS class1,
         r2.ent2 AS ent2, r2.class2 AS class2
  FROM probkb.mln6
    JOIN probkb.relationships r1 ON mln6.body1 = r1.rel AND mln6.class3 = r1.class1 AND mln6.class1 = r1.class2
    JOIN probkb.relationships r2 ON mln6.body2 = r2.rel AND mln6.class3 = r2.class1 AND mln6.class2 = r2.class2
  WHERE r1.ent1 = r2.ent1 AND r1.ent2 <> r2.ent2;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type VI: %; % rows inserted.', timer, inferred;

  ---------- Union all queries --------------------------------------------------
  CREATE TABLE probkb.r AS
  SELECT * FROM probkb.t1 UNION SELECT * FROM probkb.t2 UNION SELECT * FROM probkb.t3 UNION
  SELECT * FROM probkb.t4 UNION SELECT * FROM probkb.t5 UNION SELECT * FROM probkb.t6;

  INSERT INTO probkb.relationships
  WITH s AS (
    SELECT rel, ent1, class1, ent2, class2 FROM probkb.r
      EXCEPT
    SELECT rel, ent1, class1, ent2, class2 FROM probkb.relationships
  )
  SELECT nextval('probkb.relids'), *, NULL AS weight FROM s;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Union %: % rows inserted', timer, inferred;

  PERFORM probkb.qc();
  ANALYZE probkb.relationships;

  DROP TABLE probkb.r, probkb.t1, probkb.t2, probkb.t3, probkb.t4, probkb.t5, probkb.t6;
  RETURN inferred;
END;
$$ LANGUAGE plpgsql;

-- generate factors
CREATE OR REPLACE FUNCTION probkb.groundFactors() RETURNS VOID AS $$
DECLARE
  inferred INTEGER := 1;
  timer TEXT;
BEGIN
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'start %', timer;

  ---------- Type I -------------------------------------------------------------
  CREATE TABLE probkb.factors1 AS
  SELECT r.id AS id1, r1.id AS id2, CAST(NULL AS INT) AS id3, mln1.weight AS weight
  FROM probkb.mln1
    JOIN probkb.relationships r ON mln1.head = r.rel AND mln1.class1 = r.class1 AND mln1.class2 = r.class2
    JOIN probkb.relationships r1 ON mln1.body = r1.rel AND mln1.class1 = r1.class1 AND mln1.class2 = r1.class2
  WHERE r.ent1 = r1.ent1 AND r.ent2 = r1.ent2;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type I: %; % rows inserted.', timer, inferred;

  ---------- Type II ------------------------------------------------------------
  CREATE TABLE probkb.factors2 AS
  SELECT r.id AS id1, r1.id AS id2, CAST(NULL AS INT) AS id3, mln2.weight AS weight
  FROM probkb.mln2
    JOIN probkb.relationships r ON mln2.head = r.rel AND mln2.class1 = r.class1 AND mln2.class2 = r.class2
    JOIN probkb.relationships r1 ON mln2.body = r1.rel AND mln2.class2 = r1.class1 AND mln2.class1 = r1.class2
  WHERE r.ent1 = r1.ent2 AND r.ent2 = r1.ent1;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type II: %; % rows inserted.', timer, inferred;

  ---------- Type III -----------------------------------------------------------
  CREATE TABLE probkb.factors3 AS
  SELECT r.id AS id1, r1.id AS id2, r2.id AS id3, mln3.weight AS weight
  FROM probkb.mln3 
    JOIN probkb.relationships r ON mln3.head = r.rel AND mln3.class1 = r.class1 AND mln3.class2 = r.class2
    JOIN probkb.relationships r1 ON mln3.body1 = r1.rel AND mln3.class1 = r1.class1 AND mln3.class3 = r1.class2
    JOIN probkb.relationships r2 ON mln3.body2 = r2.rel AND mln3.class2 = r2.class1 AND mln3.class3 = r2.class2
  WHERE r.ent1 = r1.ent1 AND r.ent2 = r2.ent1 AND r1.ent2 = r2.ent2;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type III: %; % rows inserted.', timer, inferred;

  ---------- Type IV ------------------------------------------------------------
  CREATE TABLE probkb.factors4 AS
  SELECT r.id AS id1, r1.id AS id2, r2.id AS id3, mln4.weight AS weight
  FROM probkb.mln4 
    JOIN probkb.relationships r ON mln4.head = r.rel AND mln4.class1 = r.class1 AND mln4.class2 = r.class2
    JOIN probkb.relationships r1 ON mln4.body1 = r1.rel AND mln4.class1 = r1.class1 AND mln4.class3 = r1.class2
    JOIN probkb.relationships r2 ON mln4.body2 = r2.rel AND mln4.class3 = r2.class1 AND mln4.class2 = r2.class2
  WHERE r.ent1 = r1.ent1 AND r.ent2 = r2.ent2 AND r1.ent2 = r2.ent1;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type IV: %; % rows inserted.', timer, inferred;

  ---------- Type V -------------------------------------------------------------
  CREATE TABLE probkb.factors5 AS
  SELECT r.id AS id1, r1.id AS id2, r2.id AS id3, mln5.weight AS weight
  FROM probkb.mln5 
    JOIN probkb.relationships r ON mln5.head = r.rel AND mln5.class1 = r.class1 AND mln5.class2 = r.class2
    JOIN probkb.relationships r1 ON mln5.body1 = r1.rel AND mln5.class3 = r1.class1 AND mln5.class1 = r1.class2
    JOIN probkb.relationships r2 ON mln5.body2 = r2.rel AND mln5.class2 = r2.class1 AND mln5.class3 = r2.class2
  WHERE r.ent1 = r1.ent2 AND r.ent2 = r2.ent1 AND r1.ent1 = r2.ent2;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type V: %; % rows inserted.', timer, inferred;

  ---------- Type VI ------------------------------------------------------------
  CREATE TABLE probkb.factors6 AS
  SELECT r.id AS id1, r1.id AS id2, r2.id AS id3, mln6.weight AS weight
  FROM probkb.mln6 
    JOIN probkb.relationships r ON mln6.head = r.rel AND mln6.class1 = r.class1 AND mln6.class2 = r.class2
    JOIN probkb.relationships r1 ON mln6.body1 = r1.rel AND mln6.class3 = r1.class1 AND mln6.class1 = r1.class2
    JOIN probkb.relationships r2 ON mln6.body2 = r2.rel AND mln6.class3 = r2.class1 AND mln6.class2 = r2.class2
  WHERE r.ent1 = r1.ent2 AND r.ent2 = r2.ent2 AND r1.ent1 = r2.ent1;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Type VI: %; % rows inserted.', timer, inferred;
  
  ---------- Union --------------------------------------------------------------
  CREATE TABLE probkb.factors AS
  SELECT id1, id2, id3, weight FROM probkb.factors1 UNION ALL
  SELECT id1, id2, id3, weight FROM probkb.factors2 UNION ALL
  SELECT id1, id2, id3, weight FROM probkb.factors3 UNION ALL
  SELECT id1, id2, id3, weight FROM probkb.factors4 UNION ALL
  SELECT id1, id2, id3, weight FROM probkb.factors5 UNION ALL
  SELECT id1, id2, id3, weight FROM probkb.factors6;

  GET DIAGNOSTICS inferred = ROW_COUNT;
  SELECT clock_timestamp() INTO timer;
  RAISE INFO 'Union: %; % rows inserted.', timer, inferred;

  DROP TABLE probkb.factors1, probkb.factors2, probkb.factors3, probkb.factors4, probkb.factors5, probkb.factors6;
END;
$$ LANGUAGE plpgsql;
