# java -Xmx2G -XX:+HeapDumpOnOutOfMemoryError -cp bin/:lib/* org.aksw.simba.semsrl.SemMLN publications alchemy
java -Xmx2g -jar target/SemSRL-0.0.1-SNAPSHOT-jar-with-dependencies.jar publications alchemy start

echo "Copying files to Alchemy..."

cp publications-alchemy/publications.* ../alchemy-2/exdata/

../alchemy-2/bin/infer -maxSteps 100 -burnMaxSteps 100 -i "../alchemy-2/exdata/publications.mln" -r "publications-alchemy/publications-out.mln" -e "../alchemy-2/exdata/publications.db" -q EqualsEnt

# java -Xmx2G -XX:+HeapDumpOnOutOfMemoryError -cp bin/:lib/* org.aksw.simba.semsrl.eval.AlchemyEvaluation publications

java -Xmx2g -jar target/SemSRL-0.0.1-SNAPSHOT-jar-with-dependencies.jar publications alchemy eval