CREATE SCHEMA probkb;

-- data tables
CREATE TABLE probkb.classes(id INT PRIMARY KEY, name TEXT);
CREATE TABLE probkb.entities(id INT PRIMARY KEY, name TEXT);
CREATE TABLE probkb.relations(id INT PRIMARY KEY, name TEXT);

CREATE TABLE probkb.entClasses(
  ent INT, class INT,
  PRIMARY KEY(ent, class)
);

CREATE TABLE probkb.relClasses(
  rel INT, class1 INT, class2 INT,
  PRIMARY KEY(rel, class1, class2)
);

CREATE TABLE probkb.extractions(
  rel INT, ent1 INT, ent2 INT, weight DOUBLE PRECISION, url TEXT,
  PRIMARY KEY(rel, ent1, ent2)
);

CREATE TABLE probkb.functionals(
  rel INT, arg INT, deg INT,
  PRIMARY KEY (rel, arg)
);
  
CREATE TABLE probkb.ambiguities(
  ent INT, class INT,
  PRIMARY KEY(ent, class)
);

CREATE TABLE probkb.trash(
  id INT PRIMARY KEY
);

CREATE SEQUENCE probkb.relids;

-- mln tables
CREATE TABLE probkb.mln1(head INT, body INT, class1 INT, class2 INT, weight DOUBLE PRECISION);
CREATE TABLE probkb.mln2(head INT, body INT, class1 INT, class2 INT, weight DOUBLE PRECISION);
CREATE TABLE probkb.mln3(head INT, body1 INT, body2 INT,
                  class1 INT, class2 INT, class3 INT, weight DOUBLE PRECISION);
CREATE TABLE probkb.mln4(head INT, body1 INT, body2 INT,
                  class1 INT, class2 INT, class3 INT, weight DOUBLE PRECISION);
CREATE TABLE probkb.mln5(head INT, body1 INT, body2 INT,
                  class1 INT, class2 INT, class3 INT, weight DOUBLE PRECISION);
CREATE TABLE probkb.mln6(head INT, body1 INT, body2 INT,
                  class1 INT, class2 INT, class3 INT, weight DOUBLE PRECISION);
