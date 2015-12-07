package org.aksw.mandolin.eval;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class PostgreDB {
	
	private Connection con = null;
	private Statement st = null;
	private ResultSet rs = null;
	
	public PostgreDB() {
		super();
	}

	public void connect() {


		String url = "jdbc:postgresql://localhost/probkb";
		String user = "tom";
		String password = "";

		try {
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();

		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(PostgreDB.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	public void linksAbove(double d) {
		
		try {
			rs = st.executeQuery("SELECT * FROM probkb.factors WHERE weight >= 100;");
		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(PostgreDB.class.getName());
			lgr.log(Level.WARNING, ex.getMessage(), ex);
		}
		
	}

	public ResultSet next() {
		try {
			if(rs.next())
				return rs;
			else
				return null;
		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(PostgreDB.class.getName());
			lgr.log(Level.WARNING, ex.getMessage(), ex);
			return null;
		}
	}

	public void close() {
		try {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				st.close();
			}
			if (con != null) {
				con.close();
			}

		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(PostgreDB.class.getName());
			lgr.log(Level.WARNING, ex.getMessage(), ex);
		}
	}
	
}
