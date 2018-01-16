package server;

public class ServerInstance {

	private int port;
	private Server server;
	
	public static void main(String[] args) {
		int port = 8192;
		new ServerInstance(port);
	}
	public ServerInstance(int port) {
		this.port = port;
		server = new Server(port);
	}

	
}
