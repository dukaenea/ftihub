package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class DatabaseManager {
	 
// Class.forName("com.mysql.jdbc.Driver");
	
	

	 public static void main(String args[]) throws Exception
	 {
	try(
			Connection con=DBConnection.getConnection();
			
			PreparedStatement ps=con.prepareStatement(
					"select username,email from t_users where username=?"
					,ResultSet.TYPE_SCROLL_INSENSITIVE
					,ResultSet.CONCUR_READ_ONLY);

			) 
			{
				ps.setString(1, "skender");
	  
				 try (ResultSet rs = ps.executeQuery()) {
				        while ( rs.next() ) {
				            int numColumns = rs.getMetaData().getColumnCount();
				            System.out.println(rs.getString("username"));
				            for ( int i = 1 ; i <= numColumns ; i++ ) {
				                // Column numbers start at 1.
				                // Also there are many methods on the result set to return
				                // the column as a particular type. 
				                System.out.println( "COLUMN " + i + " = " + rs.getObject(i) );
				            } // for
				        } // while
				    } // try
				} // try
}
}