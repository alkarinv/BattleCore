package com.alk.serializers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * 
 * @author Alkarin
 *
 */
public abstract class SQLSerializer{
	static public final String version = "1.1.3.2";
	static protected final boolean DEBUG = false;
	static final boolean DEBUG_UPDATE = false;

	static public final int MAX_NAME_LENGTH = 16;

	private DataSource ds ;

	protected String DB = "minecraft";
	protected String DRIVER ="com.mysql.jdbc.Driver";
	protected String URL = "localhost";
	protected String PORT = "3306";
	protected String USERNAME = "root";
	protected String PASSWORD = "";

	private String create_database = "CREATE DATABASE IF NOT EXISTS " + DB;

	public String getURL() {return URL;}
	public void setURL(String url) {URL = url;}

	public String getPort() {return PORT;}
	public void setPort(String port) {PORT = port;}

	public String getUsername() {return USERNAME;}
	public void setUsername(String username) {USERNAME = username;}

	public String getPassword() {return PASSWORD;}
	public void setPassword(String password) {PASSWORD = password;}

	public String getDB() {return DB;}
	public void setDB(String dB) {
		DB = dB;
		create_database = "CREATE DATABASE IF NOT EXISTS " + DB;
	}

	protected class RSCon{
		public ResultSet rs;
		public Connection con;
	}


	public Connection getConnection(){
		return getConnection(true);
	}
	public Connection getConnection(boolean autoCommit){
		if (ds == null)
			return null;
		try {
			Connection con = ds.getConnection();
			con.setAutoCommit(autoCommit);
			return con; 
		} catch (SQLException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	public void closeConnection(RSCon rscon) {
		if (rscon == null || rscon.con == null)
			return;
		try {rscon.con.close();} catch (SQLException e) {}		
	}
	public void closeConnection(Connection con) {
		if (con ==null)
			return;
		try {con.close();} catch (SQLException e) {e.printStackTrace();}
	}


	protected boolean init(){
		Connection con = null;  /// Our database connection
		try {
			Class.forName(DRIVER);
			if (DEBUG) System.out.println("Got Driver");
		} catch (ClassNotFoundException e1) {
			System.err.println("Failed getting driver");
			e1.printStackTrace();
			return false;
		}
		try {
			ds = setupDataSource("jdbc:mysql://"+URL+":"+PORT+"/"+DB,USERNAME,PASSWORD,10,20 );
			//            LOG.debug("Connection attempt to database succeeded.");
		} catch(Exception e) {
			e.printStackTrace();
		}

		String strStmt = create_database;
		try {
			//			con = getConnection();
			con = DriverManager.getConnection("jdbc:mysql://"+URL+":" + PORT, USERNAME,PASSWORD);
			Statement st = con.createStatement();
			st.executeUpdate(strStmt);
			if (DEBUG) System.out.println("Creating db");
		} catch (SQLException e) {
			System.err.println("Failed creating db: "  + strStmt);
			e.printStackTrace();
			return false;
		} finally{
			closeConnection(con);			
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DataSource setupDataSource(String connectURI, String username, String password,
			int minIdle, int maxActive) throws Exception {
		GenericObjectPool connectionPool = new GenericObjectPool(null);

		connectionPool.setMinIdle( minIdle );
		connectionPool.setMaxActive( maxActive );

		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI,username, password);

		final boolean defaultReadOnly = false;
		final boolean defaultAutoCommit = true;
		final String validationQuery = null;
		KeyedObjectPoolFactory statementPool = new GenericKeyedObjectPoolFactory(null);
		new PoolableConnectionFactory(connectionFactory, connectionPool, statementPool,
				validationQuery, defaultReadOnly, defaultAutoCommit);		
		PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
		return dataSource;
	}

	protected boolean createTable(Connection con, String tableName, String sql_create_table,String sql_update_table) {
		/// Check to see if our table exists;
		boolean table_exists = false;
		try {
			Statement st = con.createStatement();
			st.executeUpdate("desc " + tableName);
			if (DEBUG) System.out.println("table " + tableName +" exists");
			table_exists = true;
		} catch (SQLException e) {
			if (DEBUG) System.out.println("table " + tableName +" does not exist");
		}
		/// If the table exists nothing left to do
		if (table_exists){
			return true;
		}
		/// Create our table and index
		String strStmt = sql_create_table;
		Statement st = null;
		int result =0;
		try {
			st = con.createStatement();
			result = st.executeUpdate(strStmt);
			if (DEBUG) System.out.println("Created Table with stmt=" + strStmt);
		} catch (SQLException e) {
			System.err.println("Failed in creating Table " +strStmt + " result=" + result);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	protected RSCon executeQuery(String strRawStmt, Object... varArgs){
		Connection con = getConnection();  /// Our database connection
		PreparedStatement ps = null;
		RSCon rscon = null;

		try {
			ps = getStatement(strRawStmt,con,varArgs);
			if (DEBUG) System.out.println("Executing   =" + ps.toString());
			ResultSet rs = ps.executeQuery();
			rscon = new RSCon();
			rscon.con = con;
			rscon.rs = rs;
		} catch (SQLException e) {
			System.err.println("Couldnt execute query "  + ps);
			e.printStackTrace();
		}
		return rscon;
	}


	protected void executeBatch(String updateStatement, List<List<Object>> batch) {
		Connection con = getConnection();
		PreparedStatement ps = null;
//		System.out.println("executingBatch = " + updateStatement + "  batch=" + batch);
		try{con.setAutoCommit(false);} catch(Exception e){e.printStackTrace();}
		try{ps = con.prepareStatement(updateStatement);} catch(Exception e){e.printStackTrace();}
		for (List<Object> update: batch){
			try{
				for (int i=0;i<update.size();i++){
					if (DEBUG_UPDATE) System.out.println(i+" = " + update.get(i));
					ps.setObject(i+1, update.get(i));
				}
				if (DEBUG) System.out.println("Executing   =" + ps);
				ps.addBatch();
			} catch(Exception e){
				System.err.println("statement = " + ps);
				e.printStackTrace();
			}
		}
		try{
			ps.executeBatch();
			con.commit();
		} catch(Exception e){
			System.err.println("statement = " + ps);
			e.printStackTrace();
		} finally {
			closeConnection(con);
		}

	}

	protected PreparedStatement getStatement(String strRawStmt, Connection con, Object... varArgs){
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(strRawStmt);
			for (int i=0;i<varArgs.length;i++){
				if (DEBUG_UPDATE) System.out.println(i+" = " + varArgs[i]);
				ps.setObject(i+1, varArgs[i]);
			}
		} catch (Exception e){
			System.err.println("Couldnt prepare statment "  + ps);
			e.printStackTrace();
		}
		return ps;
	}

	protected int executeUpdate(String strRawStmt, Object... varArgs){
		int result= -1;
		Connection con = getConnection();  /// Our database connection
		PreparedStatement ps = null;
		try {
			ps = getStatement(strRawStmt,con,varArgs);
			if (DEBUG) System.out.println("Executing   =" + ps.toString());
			result = ps.executeUpdate();
		} catch (Exception e) {
			System.err.println("Couldnt execute update "  + ps);
			e.printStackTrace();
		} finally {
			closeConnection(con);
		}
		return result;
	}

	public Double getDouble(String query, Object... varArgs){
		RSCon rscon = executeQuery(query,varArgs);
		if (rscon == null || rscon.con == null)
			return null;
		try {
			ResultSet rs = rscon.rs;
			while (rs.next()){
				return rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try{rscon.con.close();}catch(Exception e){}
		}
		return null;		
	}

	public Integer getInteger(String query, Object... varArgs){
		RSCon rscon = executeQuery(query,varArgs);
		if (rscon == null || rscon.con == null)
			return null;
		try {
			ResultSet rs = rscon.rs;
			while (rs.next()){
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{rscon.con.close();}catch(Exception e){}
		}
		return null;		
	}

	public Boolean getBoolean(String query, Object... varArgs){
		RSCon rscon = executeQuery(query,varArgs);
		if (rscon == null || rscon.con == null)
			return null;
		try {
			ResultSet rs = rscon.rs;
			while (rs.next()){
				Integer i = rs.getInt(1);
				if (i==null)
					return null;
				return i > 0; 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{rscon.con.close();}catch(Exception e){}
		}
		return null;		
	}

	public String getString(String query, Object... varArgs){
		RSCon rscon = executeQuery(query,varArgs);
		if (rscon == null || rscon.con == null)
			return null;
		try {
			ResultSet rs = rscon.rs;
			while (rs.next()){
				return rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{rscon.con.close();}catch(Exception e){}
		}
		return null;		
	}

	public List<Object> getObjects(String query, Object... varArgs){
		RSCon rscon = executeQuery(query,varArgs);
		if (rscon == null || rscon.con == null)
			return null;
		try {
			ResultSet rs = rscon.rs;
			while (rs.next()){
				java.sql.ResultSetMetaData rsmd = rs.getMetaData();
				int nCol = rsmd.getColumnCount();
				List<Object> objs = new ArrayList<Object>(nCol);
				for (int i=0;i<nCol;i++){
					objs.add(rs.getObject(i+1));
				}
				return objs;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{rscon.con.close();}catch(Exception e){}
		}
		return null;		
	}

}
