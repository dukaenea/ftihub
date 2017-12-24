package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;

import database.DBConnection;
import messageTemplate.ParseMessages;
import messageTemplate.TemplateMessages;

public class Server implements Runnable{

	private HashMap<Integer,ServerClient> allClients= new HashMap<Integer,ServerClient>();
	private List<Integer> clientResponse = new ArrayList<Integer>();

	private DatagramSocket socket;
	private int port;
	private boolean running = false;
	private Thread run, manage, send, receive;
	private final int MAX_ATTEMPTS = 5;

	private boolean raw = false;
	
	private ParseMessages JSON;
	private TemplateMessages Template;
	
	
	public Server(int port) {
		this.port = port;
		this.JSON=new ParseMessages();
		this.Template=new TemplateMessages();
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		new Thread() {
			public void run() {
		if(allClients.isEmpty()) {
				try {
					getAllClients();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		 }
		}.start();
		run = new Thread(this, "Server");
		run.start();
	}
	private void getAllClients() throws SQLException {
		try(
				Connection con=DBConnection.getConnection();
				
				PreparedStatement ps=con.prepareStatement(
						"select id,username,password from t_users"
						,ResultSet.TYPE_SCROLL_INSENSITIVE
						,ResultSet.CONCUR_READ_ONLY);

				) 
				{

					 try (ResultSet rs = ps.executeQuery()) {
						
						 while(rs.next()) {
							 int id=rs.getInt("id");
							 allClients.put(id,new ServerClient(rs.getString("username"),rs.getString("password"),id));
						 	}
						   
					    } // try
					} // try
	}
	
	
	public void run() {
		running = true;
		System.out.println("Server started on port " + port);
		manageClients();
		receive();
		Scanner scanner = new Scanner(System.in);
		while (running) {
			System.out.println(allClients.size());
			String text = scanner.nextLine();
			if (!text.startsWith("/")) {
				sendToAll("/m/Server: " + text + "/e/");
				continue;
			}
			text = text.substring(1);
			if (text.equals("raw")) {
				if (raw) System.out.println("Raw mode off.");
				else System.out.println("Raw mode on.");
				raw = !raw;
			} else if (text.equals("clients")) {
				System.out.println("Clients:");
				System.out.println("========");
				for (ServerClient c : allClients.values()) {
					if(c.online)
						System.out.println(c.name + "(" + c.getID() + "): " + c.address.toString() + ":" + c.port);
				}
				System.out.println("========");
			} else if (text.startsWith("kick")) {
				String name = text.split(" ")[1];
				int id = -1;
				boolean number = true;
				try {
					id = Integer.parseInt(name);
				} catch (NumberFormatException e) {
					number = false;
				}
				if (number) {
					boolean exists = false;
					
					ServerClient c=allClients.get(id);
					
					if(c!=null) {
						exists=true;
					}
					if (exists) disconnect(id, true);
					else System.out.println("Client " + id + " doesn't exist! Check ID number.");
				} else {
					for (ServerClient c : allClients.values()) {
						if(c.online) {
							if (name.equals(c.name)) {
								disconnect(c.getID(), true);
								break;
							}
						}
					}
					
				}
			} else if (text.equals("help")) {
				printHelp();
			} else if (text.equals("quit")) {
				quit();
			} else {
				System.out.println("Unknown command.");
				printHelp();
			}
		}
		scanner.close();
	}

	private void printHelp() {
		System.out.println("Here is a list of all available commands:");
		System.out.println("=========================================");
		System.out.println("/raw - enables raw mode.");
		System.out.println("/clients - shows all connected clients.");
		System.out.println("/kick [users ID or username] - kicks a user.");
		System.out.println("/help - shows this help message.");
		System.out.println("/quit - shuts down the server.");
	}

	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while (running) {
					String ping=Template.ping();
					sendToAll(ping);
					sendStatus();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (ServerClient c : allClients.values()) {
						if(c.online) {
							if (!clientResponse.contains(c.getID())) {
								if (c.attempt >= MAX_ATTEMPTS) {
									disconnect(c.getID(), false);
								} else {
									c.attempt++;
								}
							} else {
								clientResponse.remove(new Integer(c.getID()));
								c.attempt = 0;
							}
						}
					}

				}
			}
		};
		manage.start();
	}

	private void sendStatus() {
		//if (clients.size() <= 0) return;
		StringBuilder users =new StringBuilder();
		users.append("/u/");
		for (ServerClient c : allClients.values()) {
			if(c.online)
				users.append(c.name + "/n/");
		}

		users.append("/e/");
		
		sendToAll(users.toString());
	}

	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while (running) {
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (SocketException e) {
					} catch (IOException e) {
						e.printStackTrace();
					}
					process(packet);
				}
			}
		};
		receive.start();
	}

	private void printMap() {
		for (ServerClient c : allClients.values()) {
			//if(c.online) {
				System.out.println(c.name+" :"+c.online+" :"+c.password);
		//	}
	   }
	}
	
	private void sendToAll(String message) {
//		if (message.startsWith("/m/")) {
//			String text = message.substring(3);
//			text = text.split("/e/")[0];
//			System.out.println(message);
//		}
		for (ServerClient c : allClients.values()) {
			if(c.online) {
				
				send(message.getBytes(), c.address, c.port);
			}
		}

	}

	private void send(final byte[] data, final InetAddress address, final int port) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}
	private int validateCredentials(String username,String password) throws Exception{
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
		
		return -1;
	}


	private void process(DatagramPacket packet) {
		String string = new String(packet.getData());
		if (raw) System.out.println(string);
		switch(JSON.getTypeOfMessage(string)) {
		case "login":
			JSONObject loginCredetials=JSON.parse(string);
			System.out.println(string);
			printMap();
			System.out.println(loginCredetials.getString("username"));
			System.out.println(loginCredetials.getString("password"));
			boolean success=false;
			for (ServerClient c : allClients.values()) {
				
				if(c.name.equals(loginCredetials.getString("username")) && 
				    c.password.equals(loginCredetials.getString("password"))) {
					c.address=packet.getAddress();
					c.port=packet.getPort();
					c.online=true;
					success=true;
					send(Template.loginSuccess(c.getID()).getBytes(), c.address, c.port);	
					break;
				}
					
			}
			if(!success) 
				send(Template.loginFail().getBytes(), packet.getAddress(), packet.getPort());	
			break;
		case "global-message":
			JSONObject message=JSON.parse(string);
			sendToAll(message.getString("message"));
			break;
		case "disconnect":
			JSONObject disconnect=JSON.parse(string);
			disconnect(disconnect.getInt("id"), true);
			break;
		case "response-ping":
			JSONObject ping=JSON.parse(string);
			clientResponse.add(ping.getInt("id"));
			break;
		case "private-message":
			JSONObject privateMessage=JSON.parse(string);
		    int idOfReceiver=privateMessage.getInt("idOfReceiver");
		    int idOfSender=privateMessage.getInt("idOfSender");
		    String pm = Template.preparePrivateMessageFromServer(idOfSender,privateMessage.getString("privateMessage"));
			ServerClient c=allClients.get(idOfReceiver);
			if(c.online) {
			send(pm.getBytes(), c.address, c.port);
			}else {
				//Add to db as unread because he was offline when the message
				//was send
			}
			break;
		default:
			System.out.println(string);
			break;
		}
//		if (string.startsWith("/c/")) {
//			
//			String name = string.split("/c/|/e/")[1].split(" ")[0];
//			int id = Integer.parseInt(string.split("/c/|/e/")[1].split(" ")[1]);
//			System.out.println(name + "(" + id + ") connected!");
//			ServerClient c=allClients.get(id);
//			c.address=packet.getAddress();
//			c.port=packet.getPort();
//			c.online=true;
//			
//		} else if (string.startsWith("/m/")) {
//			sendToAll(string);
//		} else if (string.startsWith("/d/")) {
//			String id = string.split("/d/|/e/")[1];
//			disconnect(Integer.parseInt(id), true);
//		} else if (string.startsWith("/i/")) {
//			clientResponse.add(Integer.parseInt(string.split("/i/|/e/")[1]));
//		} else if(string.startsWith("/p/")) {
//			
//				String[] strSplit=string.split("/p/|/s/|/m/");
//		        int idOfReceiver=Integer.parseInt(strSplit[1]);
//		        int idOfSender=Integer.parseInt(strSplit[2]);
//		        String privateMessage="/p/"+idOfSender+"/m/"+strSplit[3];
//		        ServerClient c=allClients.get(idOfReceiver);
//		        if(c.online) {
//		        	send(privateMessage.getBytes(), c.address, c.port);
//		        }else {
//		        	//Add to db as unread
//		        }
//		} else {
//			System.out.println(string);
//		}
	}

	private void quit() {
		for (Integer key : allClients.keySet()) {
			disconnect(key, true);
		}

		running = false;
		socket.close();
	}

	private void disconnect(int id, boolean status) {
		ServerClient c = allClients.get(id);
		boolean exists = false;
		if(c!=null){
			c.online=false;
			exists = true;
		}
		
		if (!exists) return;
		String message = "";
		if (status) {
			message = "Client " + c.name + " (" + c.getID() + ") @ " + c.address.toString() + ":" + c.port + " disconnected.";
		} else {
			message = "Client " + c.name + " (" + c.getID() + ") @ " + c.address.toString() + ":" + c.port + " timed out.";
		}
		System.out.println(message);
	}
}
