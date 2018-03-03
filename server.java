package UnstructuredP2P;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class server {
	
	public int Nodeport;

	   public server(int Nodeport)
	   {
	      
		   this.Nodeport = Nodeport;
	   }
	   
	   
	 public void run(){
			try {
				DatagramSocket serverSocket = new DatagramSocket(Nodeport);
				
				while (true){
					byte[] reqData = new byte[65000];
					
				// Receiving a request from other nodes
		            DatagramPacket incomming_req = new DatagramPacket(reqData, reqData.length);		            
		            serverSocket.receive(incomming_req);
		            InetAddress reqIP = incomming_req.getAddress();
		            int reqPort = incomming_req.getPort();
		            String inRequest = new String(incomming_req.getData(),0,incomming_req.getLength());
		            
		            System.out.println("msg received in server: " + inRequest );
		            
		            String[] server_req = inRequest.split(" ");
		            String server_cmd = server_req[1];
		            
		            switch (server_cmd){
		            case "JOIN":
		            	
		            	String joinmsg= "0014 JOINOK 0"; 
		            	byte[] joinOkmsg = joinmsg.getBytes();
		         // Rending the JOINOK response
						DatagramPacket join_response = new DatagramPacket(joinOkmsg, joinOkmsg.length, reqIP, reqPort);
						serverSocket.send(join_response);
		            	break;
		            }
		            	
				}				
								
				
			} catch (NumberFormatException | SocketException e) {				
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

}
