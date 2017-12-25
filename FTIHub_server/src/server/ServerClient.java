package server;

import java.net.InetAddress;

public class ServerClient {
	
	public String name;
	public InetAddress address;
	public int port;
	private final int ID;
	public int attempt = 0;
	public boolean online;
	public String password;
	
	public ServerClient(String name,String password,int id) {
		this.name=name;
		this.ID=id;
		this.password=password;
	}
	
	
	public int getID() {
		return ID;
	}
}
