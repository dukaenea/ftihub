package client;


import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import database.DBConnection;

public class Login extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtAddress;
	private JTextField txtPort;
	private JLabel lblIpAddress;
	private JLabel lblPort;
	private JLabel lblAddressDesc;
	private JLabel lblPortDesc;

	public Login() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		setResizable(false);
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 380);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		txtName = new JTextField();
		txtName.setBounds(67, 50, 165, 28);
		contentPane.add(txtName);
		txtName.setColumns(10);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(127, 34, 45, 16);
		contentPane.add(lblName);

		txtAddress = new JTextField();
		txtAddress.setBounds(67, 116, 165, 28);
		contentPane.add(txtAddress);
		txtAddress.setColumns(10);

		lblIpAddress = new JLabel("IP Address:");
		lblIpAddress.setBounds(111, 96, 77, 16);
		contentPane.add(lblIpAddress);

		txtPort = new JTextField();
		txtPort.setColumns(10);
		txtPort.setBounds(67, 191, 165, 28);
		contentPane.add(txtPort);

		lblPort = new JLabel("Port:");
		lblPort.setBounds(133, 171, 34, 16);
		contentPane.add(lblPort);

		lblAddressDesc = new JLabel("(eg. 192.168.0.2)");
		lblAddressDesc.setBounds(94, 142, 112, 16);
		contentPane.add(lblAddressDesc);

		lblPortDesc = new JLabel("(eg. 8192)");
		lblPortDesc.setBounds(116, 218, 68, 16);
		contentPane.add(lblPortDesc);

		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = txtName.getText();
				String address = txtAddress.getText();
				int port = Integer.parseInt(txtPort.getText());
				try {
					login(name, address, port);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnLogin.setBounds(91, 311, 117, 29);
		contentPane.add(btnLogin);
	}

	private void login(String name, String address, int port) throws Exception {
		int userId=validateCredentials("skender","a");
		String localAddress="";
		 try {
	          InetAddress ipAddr = InetAddress.getLocalHost();
	          localAddress=ipAddr.getHostAddress();
	      } catch (UnknownHostException ex) {
	          ex.printStackTrace();
	      }
		
		if(userId!=-1 && localAddress!="") {
			dispose();
			new ClientInstance(name, address, port,userId);
		}else {
			//Print wrong credentials or send to sign up Tab
		}
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


	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}