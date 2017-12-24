package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

	private static final long serialVersionUID = 1L;

	private DatagramSocket socket;

	private String name, address;
	private int port;
	private InetAddress ip;
	private Thread send;
	private int id;

	public Client(String name, String address, int port,int id) {
		this.name = name;
		this.address = address;
		this.port = port;
		this.id=id;
	}

	public Client(String name, String address, int port) {
		this.name = name;
		this.address = address;
		this.port = port;
	}

	public void setId(int id) {
		this.id=id;
	}
	
	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public boolean openConnection() {
		try {
			socket = new DatagramSocket();
			ip = InetAddress.getByName(this.address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String receive() {
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message = new String(packet.getData());
		return message;
	}

	public void send(final byte[] data) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	public void close() {
		new Thread() {
			public void run() {
				synchronized (socket) {
					socket.close();
				}
			}
		}.start();
	}

	public int getId() {
		return id;
	}
}
