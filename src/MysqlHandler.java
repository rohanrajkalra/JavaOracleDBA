import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!

public class MysqlHandler {
	
	private Connection conn;
	private String authResults;
	private Boolean isAuthenticated;
	private String dbError;
	
    public MysqlHandler(String username, String password) {

    	conn = null;
    	isAuthenticated = false;
    	
    	try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_dealer",username,password);
            
            isAuthenticated = true;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());        // handle the error
        }
    }
    
    public String getAuthenticationResults() {
    	
    	if(isAuthenticated)
    		authResults = "Successfully connected to the database";
    	else 
            authResults = "Issue connecting to the database"; 
    	
    	return authResults;
    }
    
    public String getLastDbError() {
    	return dbError;
    }
    
    public void flushDbError() {
    	dbError = null;
    }
    
    public Boolean isAuthenticated() {
    	
    	return isAuthenticated;
    }
    
    public Connection getConnection() {
    	return conn;
    }

	
	public Integer executeQuery(String sql) {
        System.out.println(sql);
	    Statement stmt;
	    Integer rowsAffected = 0;
	    dbError = null;

	    try {
			stmt = getConnection().createStatement();
			rowsAffected = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			dbError = e.getLocalizedMessage();
			e.printStackTrace();
		}
	    
	    return rowsAffected;
	}
	
	public ResultSet fetchQuery(String sql) {

	    Statement stmt;
	    ResultSet result = null;
	    dbError = null;

	    try {
			stmt = getConnection().createStatement();
			result = stmt.executeQuery(sql);
		} catch (SQLException e) {
			dbError = e.getLocalizedMessage();
			e.printStackTrace();
		}
		
	    return result;
	}
	
}