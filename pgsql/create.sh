createdb probkb
psql probkb -f sql/create.sql  # Create the probkb schema and tables.
psql probkb -f sql/qc.sql      # Create quality control procedures.
psql probkb -f sql/load.sql    # Load the files in CSV format.
psql probkb -f sql/ground.sql  # Create grounding procedures.
