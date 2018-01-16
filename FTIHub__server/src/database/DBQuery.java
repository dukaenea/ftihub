package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import server.ServerClient;

public class DBQuery {
	
	public int insertSignUpCredentials(String name,String password, String email) {
		int rowsaffected = 0;
		try (
			Connection con = DBConnection.getConnection();
			PreparedStatement ps = con.prepareStatement(
					 "insert into t_users(username,password,email) select*from(select ?,?,?) as INS where not exists(select username from t_users where username = ?) LIMIT 1"
					 ,ResultSet.TYPE_SCROLL_INSENSITIVE
					 ,ResultSet.CONCUR_READ_ONLY);
					)
					{
						ps.setString(1,name);
						ps.setString(2,password);
						ps.setString(3,email);
						ps.setString(4,name);
					    rowsaffected = ps.executeUpdate();
					} catch (SQLException e) {
						e.printStackTrace();
					}
		
		   return rowsaffected;
		}
	
	public void insertChatHistoryOnline(int sender,int receiver, String message) {
		try(
				Connection con=DBConnection.getConnection();
				
				PreparedStatement ps=con.prepareStatement(
						"insert into t_privatechat(id_sender,id_receiver,online,message) values(?,?,true,?)"
						,ResultSet.TYPE_SCROLL_INSENSITIVE
						,ResultSet.CONCUR_READ_ONLY);

				) 
				{
					ps.setInt(1, sender);
					ps.setInt(2, receiver);
					ps.setString(3, message);
					ps.executeUpdate();
					 
					} 
					catch (SQLException e) {
						
						e.printStackTrace();
					}
	}
	
	public void insertChatHistoryOffline(int sender,int receiver, String message) {
		try(
				Connection con=DBConnection.getConnection();
				
				PreparedStatement ps=con.prepareStatement(
						"insert into t_privatechat(id_sender,id_receiver,online,message) values(?,?,false,?)"
						,ResultSet.TYPE_SCROLL_INSENSITIVE
						,ResultSet.CONCUR_READ_ONLY);

				) 
				{
					ps.setInt(1, sender);
					ps.setInt(2, receiver);
					ps.setString(3, message);
					ps.executeUpdate();
					 
					} 
					catch (SQLException e) {
						
						e.printStackTrace();
					}
		
	
	}
	
	public String changeChatTab(int idTab,int idSender) {
		
	
		StringBuilder s=new StringBuilder();
		
		try(
				Connection con=DBConnection.getConnection();
				
				PreparedStatement ps=con.prepareStatement(
						"select message,online from t_privatechat where id_sender=?  and id_receiver=?"
						,ResultSet.TYPE_SCROLL_INSENSITIVE
						,ResultSet.CONCUR_READ_ONLY);

				) 
				{
					ps.setInt(1, idSender);
					ps.setInt(2, idTab);
					
					s.append("{\"type\": \"new-tab\",\"messages\":[");
							
					 try (ResultSet rs = ps.executeQuery()) {
						 
						 int nrMessages=0;
						 int nrNotification=0;
						 while(rs.next()) {
						  
						    String message = rs.getString("message");
						    boolean online= rs.getBoolean("online");
						    if(!online) nrNotification++;
						    s.append("{\"online\":\""+Boolean.toString(online)+"\",\"message\":\""+message+"\"},");
						    nrMessages++; 
						 }
						 s.setLength(s.length() - 1);
						 s.append("],\"nrNotification\":\""+nrNotification+"\",\"nrMessages\":\""+nrMessages+"\"}");						 
					    }// try
					} // try
					catch (SQLException e) {
						
							e.printStackTrace();
						}
				
			return s.toString();
	}
	
	
	public void updateChatHistory(int idTab, int idSender) {
		
		try(
				Connection con=DBConnection.getConnection();
				
				PreparedStatement ps=con.prepareStatement(
						"update t_privatechat set online=true where id_sender=?  and id_receiver=?"
						,ResultSet.TYPE_SCROLL_INSENSITIVE
						,ResultSet.CONCUR_READ_ONLY);

				) 
				{
					ps.setInt(1, idSender);
					ps.setInt(2, idTab);
					ps.executeUpdate();
					 
					} 
					catch (SQLException e) {
						
						e.printStackTrace();
					}
		
	}
	
	public int validateCredentials(String username,String password){
		try(
				Connection con=DBConnection.getConnection();
				
				PreparedStatement ps=con.prepareStatement(
						"select id from t_users where username=? and password=?"
						,ResultSet.TYPE_SCROLL_INSENSITIVE
						,ResultSet.CONCUR_READ_ONLY);

				) 
				{
					ps.setString(1, username);
					ps.setString(2, password);
					 int id=0;
					 int rowCount=0;
					 try (ResultSet rs = ps.executeQuery()) {
						 
						
						 while(rs.next()) {
						  
						  rowCount++;
					      id = rs.getInt("id");
					      
						 }
						 
					     if(rowCount==1) {
					    	 return id;
					     }
					         
					    } // try
					} // try
				catch (SQLException e) {
					
						e.printStackTrace();
					}
		
		return -1;
	}
}
