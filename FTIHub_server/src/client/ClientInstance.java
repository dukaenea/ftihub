package client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import org.json.JSONObject;

import messageTemplate.ParseMessages;
import messageTemplate.TemplateMessages;


public class ClientInstance extends JFrame implements Runnable{

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField txtMessage;
	private JTextArea history;
	private DefaultCaret caret;
	private Thread run, listen;
	private Client client;

	private boolean running = false;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;

	private OnlineUsers users;
	
	private ParseMessages JSON;
	private TemplateMessages Template;
	//Kjo do te ndryshoje kur ndryshon tabe per ti folur njerezve privatisht kur je ne room-in global do t jet default =-1
	private int currentTabIdUser=2;

	public ClientInstance(Client client) {
		setTitle("FTI-HUB");
		this.client=client;
		this.JSON=new ParseMessages();
		this.Template=new TemplateMessages();
		boolean connect = client.openConnection();
		if (!connect) {
			//Nuk behet do connection me serverin
			System.err.println("Connection failed!");
		}
		//this.users = new OnlineUsers();
		running = true;
		run = new Thread(this, "Running");
		run.start();
	}

	private void createWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(880, 550);
		setLocationRelativeTo(null);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmOnlineUsers = new JMenuItem("Online Users");
		mntmOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				users.setVisible(true);
			}
		});
		mnFile.add(mntmOnlineUsers);

		mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 28, 815, 30, 7 }; // SUM = 880
		gbl_contentPane.rowHeights = new int[] { 25, 485, 40 }; // SUM = 550
		contentPane.setLayout(gbl_contentPane);

		history = new JTextArea();
		history.setEditable(false);
		JScrollPane scroll = new JScrollPane(history);
		caret = (DefaultCaret) history.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.insets = new Insets(0, 0, 5, 5);
		scrollConstraints.fill = GridBagConstraints.BOTH;
		scrollConstraints.gridx = 0;
		scrollConstraints.gridy = 0;
		scrollConstraints.gridwidth = 3;
		scrollConstraints.gridheight = 2;
		scrollConstraints.weightx = 1;
		scrollConstraints.weighty = 1;
		scrollConstraints.insets = new Insets(0, 5, 0, 0);
		contentPane.add(scroll, scrollConstraints);

		txtMessage = new JTextField();
		txtMessage.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					routeMessage(txtMessage.getText());
				}
			}
		});
		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
		gbc_txtMessage.insets = new Insets(0, 0, 0, 5);
		gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMessage.gridx = 0;
		gbc_txtMessage.gridy = 2;
		gbc_txtMessage.gridwidth = 2;
		gbc_txtMessage.weightx = 1;
		gbc_txtMessage.weighty = 0;
		contentPane.add(txtMessage, gbc_txtMessage);
		txtMessage.setColumns(10);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				routeMessage(txtMessage.getText());
			}

		});
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 0, 0, 5);
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 2;
		gbc_btnSend.weightx = 0;
		gbc_btnSend.weighty = 0;
		contentPane.add(btnSend, gbc_btnSend);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println(client.getId());
				String disconnect = Template.disconnect(client.getId());
				send(disconnect,false);
				running = false;
				client.close();
			}
		});

		setVisible(true);

		txtMessage.requestFocusInWindow();
	}

	public void routeMessage(String text) {
		if(currentTabIdUser!=-1) { 	//For private chat
			String privateMessage=Template.privateMessage(currentTabIdUser, client.getId(), text);
			send(privateMessage,true);
		}else {
			String globalMesssage=Template.message(text);
			send(globalMesssage,true);
		}
	}
	
	public void run() {
		listen();
	}

	private void send(String message,boolean text) {
		if(text) {
			if(JSON.parse(message).has("message")){
				if (JSON.parse(message).getString("message").equals("")) return;
			}else if(JSON.parse(message).has("private-message")) {
				if (JSON.parse(message).getString("private-message").equals("")) return;
			}
			txtMessage.setText("");
		}
		client.send(message.getBytes());
	}
	
	public void listen() {
		listen = new Thread("Listen") {
			public void run() {
				String loginCredentials = Template.loginCredentials(client.getName(), client.getPassword());
				System.out.println(loginCredentials);
				client.send(loginCredentials.getBytes());
				while (running) {
					String string = client.receive().split("/e/")[0];
					switch(JSON.getTypeOfMessage(string)) {
					case "login-success":
						JSONObject parsedMessage = JSON.parse(string);
						dispose();
						client.setId(parsedMessage.getInt("id"));
						createWindow();
						console("Attempting a connection to " + client.getAddress() + ":" + client.getPort() + ", user: " + client.getName());
						break;
					case "login-fail":
						// Print wrong credentials or send to sign up Tab
						System.out.println("Wrong username or password!");
						break;
					case "global-message":
						JSONObject message=JSON.parse(string);
						console(message.getString("message"));
						break;
					case "ping":
						String text=Template.responsePingClient(client.getId());
						send(text,false);
						break;
					case "private-message":
						JSONObject privateMessage=JSON.parse(string);
					    int idOfSender=privateMessage.getInt("idOfSender");
					    String pm = privateMessage.getString("privateMessage");
					    console("PrivateMessage from "+idOfSender+" to "+client.getName()+": "+pm);
						break;
					default:
						System.out.println(string);
						break;
					}	
					
					//TODO:PER ONLINE USERS
					
//					if (string.startsWith("/c/")) {
//						console("Successfully connected to server! ID: " + client.getId());
//					} else if (string.startsWith("/m/")) {
//						String text = string.substring(3);
//						text = text.split("/e/")[0];
//						console(text);
//					} else if (string.startsWith("/i/")) {
//						String text = "/i/" + client.getId() + "/e/";
//						send(text, false);
//					} else if (string.startsWith("/u/")) {
//						String[] u = string.split("/u/|/n/|/e/");
//						users.update(Arrays.copyOfRange(u, 1, u.length - 1));
//					} else if (string.startsWith("/p/")) {
//						String[] strSplit=string.split("/p/|/m/");
//				        int idOfSender=Integer.parseInt(strSplit[1]);
//				        String privateMessage=strSplit[2].split("/e/")[0];
//				        console("PrivateMessage from "+idOfSender+" to "+client.getName()+": "+privateMessage);
//					}
				}
			}
		};
		listen.start();
	}

	public void console(String message) {
		history.append(message + "\n\r");
		history.setCaretPosition(history.getDocument().getLength());
	}
}