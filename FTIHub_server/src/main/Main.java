package main;


import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
  public static void main(String args[]) {
	  //Get Client IP
	  try {
          InetAddress ipAddr = InetAddress.getLocalHost();
          System.out.println(ipAddr.getHostAddress());
      } catch (UnknownHostException ex) {
          ex.printStackTrace();
      }
  }
	 
}
