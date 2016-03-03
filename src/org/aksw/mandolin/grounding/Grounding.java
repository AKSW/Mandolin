package org.aksw.mandolin.grounding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.aksw.mandolin.util.Bundle;
import org.aksw.mandolin.util.Shell;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Grounding {

	public static void ground(String base) throws FileNotFoundException {
		// prepare SQL files
		prepare(base);
		// generate tables and procedures
		generate(base);
		// run scripts for grounding
		run();
	}

	private static void run() {
		String[] cmd = {
		// Drop schema
		Bundle.getString("pgsql_home") + "/bin/psql probkb -h "
				+ Bundle.getString("pgsql_url") + " -p 5432 -f "
				+ System.getProperty("user.dir") + "/pgsql/sql/run.sql", };
		for (String c : cmd) {
			System.out.println("> " + c);
			Shell.execute(c, true);
		}
	}

	private static void generate(String base) {
		String[] cmd = {
				// Drop schema
				Bundle.getString("pgsql_home") + "/bin/psql probkb -h "
						+ Bundle.getString("pgsql_url") + " -p 5432 -f "
						+ System.getProperty("user.dir")
						+ "/pgsql/sql/drop.sql",
				// Create db
				Bundle.getString("pgsql_home") + "/bin/createdb probkb -h "
						+ Bundle.getString("pgsql_url") + " -p 5432",
				// Create the probkb schema and tables.
				Bundle.getString("pgsql_home") + "/bin/psql probkb -h "
						+ Bundle.getString("pgsql_url") + " -p 5432 -f "
						+ System.getProperty("user.dir")
						+ "/pgsql/sql/create.sql",
				// Create quality control procedures.
				Bundle.getString("pgsql_home") + "/bin/psql probkb -h "
						+ Bundle.getString("pgsql_url") + " -p 5432 -f "
						+ System.getProperty("user.dir") + "/pgsql/sql/qc.sql",
				// Load the files in CSV format.
				Bundle.getString("pgsql_home") + "/bin/psql probkb -h "
						+ Bundle.getString("pgsql_url") + " -p 5432 -f "
						+ System.getProperty("user.dir") + "/" + base
						+ "/load.sql",
				// Create grounding procedures.
				Bundle.getString("pgsql_home") + "/bin/psql probkb -h "
						+ Bundle.getString("pgsql_url") + " -p 5432 -f "
						+ System.getProperty("user.dir")
						+ "/pgsql/sql/ground.sql" };
		for (String c : cmd) {
			System.out.println("> " + c);
			Shell.execute(c, true);
		}
	}

	private static void prepare(String base) throws FileNotFoundException {

		PrintWriter load = new PrintWriter(new File(base + "/load.sql"));

		// write head
		write("pgsql/sql/load-head.sql", load);

		// write graph tables
		String[] tables = { "classes", "entities", "relations", "entClasses",
				"relClasses", "functionals", "extractions", };
		// due to a stylistic choice from ProbKB, table `extractions`
		// corresponds to file `relationships.csv`
		String[] csv = { "classes", "entities", "relations", "entClasses",
				"relClasses", "functionals", "relationships", };
		for (int i = 0; i < tables.length; i++)
			load.write("COPY probkb." + tables[i] + " FROM '"
					+ System.getProperty("user.dir") + "/" + base + "/"
					+ csv[i] + ".csv' DELIMITERS ',' CSV;\n");

		// write body
		write("pgsql/sql/load-body.sql", load);

		// write MLN tables
		for (int i = 1; i <= 6; i++)
			load.write("COPY probkb.mln" + i + " FROM '"
					+ System.getProperty("user.dir") + "/" + base + "/mln" + i
					+ ".csv' DELIMITERS ',' CSV;\n");

		// write tail
		write("pgsql/sql/load-tail.sql", load);
		load.close();

	}

	private static void write(String filename, PrintWriter pw)
			throws FileNotFoundException {
		Scanner in = new Scanner(new File(filename));
		while (in.hasNextLine())
			pw.write(in.nextLine() + "\n");
		in.close();
	}

	public static void main(String[] args) throws FileNotFoundException {

		ground("eval/0001");

	}

}
