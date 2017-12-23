package server;

public class ServerInstance {

	private int port;
	private Server server;

	public ServerInstance(int port) {
		this.port = port;
		server = new Server(port);
	}

	public static void main(String[] args) {
		int port = 8193;
		new ServerInstance(port);
	}
}
