import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class command {
	
// Storage of nodes details received from Bootstrap Server
	ArrayList<String> Node_info = new ArrayList<String>();
	
	static HashMap<Integer, String > map = new HashMap<>();
	
public void REG(InetAddress BS_ip, int BS_port, int Node_port, String uname) throws IOException{
	
	String Node_IP = InetAddress.getLocalHost().getHostAddress();
	String request =  "REG" + " " + Node_IP + " " + Node_port + " " + uname ;
	int msg_len =  request.length() + 5;
	String reg_msg = String.format("%04d", msg_len) + " " +request;
	byte[] Reg_request = reg_msg.getBytes();
	
	DatagramSocket client_Socket = new DatagramSocket();	
	
//sending the register request to the BS
	
	DatagramPacket REG_Packet = new DatagramPacket(Reg_request, Reg_request.length, BS_ip, BS_port);
	client_Socket.send(REG_Packet);
	
//receiving node socket address from BS
	
	byte[] BS_response = new byte[65000];
    DatagramPacket BSResponse = new DatagramPacket(BS_response, BS_response.length);
    client_Socket.receive(BSResponse); 
    
    String BSR = new String(BSResponse.getData(),0,BSResponse.getLength());
    String[] BS_Response = BSR.split(" ");
    
    if (BS_Response[3].equals("9999"))
	    {
	    	System.err.println("Error: Registration failure" );
	    }else if (BS_Response[3].equals("9998")){
	    	System.err.println("Error: Already registered, unregister first" );
	    }else if(BS_Response[3].equals("-1")){
	    	System.out.println("Error: Unknow REG comand ");
	    } else{
	    	System.out.println("Status: REGISTERED");
	    }
    
  // Storing the details of the other nodes received from Bootstrap Server
    if ( BS_Response[3].equals("1") )
	    {
	    	String Node_1 = BS_Response[4]+":"+BS_Response[5];
	    	Node_info.add(Node_1);
	    }else if(BS_Response[3].equals("2"))
		    {
	    	String Node_1 = BS_Response[4]+":"+BS_Response[5];
	    	Node_info.add(Node_1);
	    	
	    	String Node_2 = BS_Response[6]+":"+BS_Response[7];
	    	Node_info.add(Node_2);
		    	
		    }else if(BS_Response[3].equals("3"))
			    {
		    	String Node_1 = BS_Response[4]+":"+BS_Response[5];
		    	Node_info.add(Node_1);
		    	
		    	String Node_2 = BS_Response[6]+":"+BS_Response[7];
		    	Node_info.add(Node_2);
		    	
		    	String Node_3 = BS_Response[8]+":"+BS_Response[9];
		    	Node_info.add(Node_3);
			    	
			    }
        
    client_Socket.close();
	}

public void join(int Node_port) throws IOException{
	
	String Node_IP = InetAddress.getLocalHost().getHostAddress();
	String join_req = " JOIN" + " " + Node_IP + " " + Node_port;
	int len = join_req.length() + 4;
	String join_msg = String.format("%04d", len) + " " +join_req;
	byte[] join_request = join_msg.getBytes();
	
	int noIP = Node_info.size();
	
	if(noIP == 0) { 
		System.out.println("this is the first node");			
	} else{
	
	DatagramSocket client_Socket = new DatagramSocket();	
	
// sending JOIN message to the nodes	
	for(int k=0; k<noIP; k++){	
		
		String[] SockADD= Node_info.get(k).split(":");		
		DatagramPacket JOIN_Packet = new DatagramPacket(join_request, join_request.length, InetAddress.getByName(SockADD[0]), Integer.parseInt(SockADD[1]));
		client_Socket.send(JOIN_Packet);
	}	
	
// receiving JOIN response
	
	byte[] JOIN_response = new byte[65000];
	
	for(int n=0; n<noIP; n++){
		DatagramPacket JResponse = new DatagramPacket(JOIN_response, JOIN_response.length);
	    client_Socket.receive(JResponse);
	    
	    String JR = new String(JResponse.getData(),0,JResponse.getLength());
	    
	    String[] S_JR= JR.split(" ");
	    if(S_JR[0].equals("0")){	    	
	    	System.out.println("Status: Join Successful with " + JResponse.getAddress().toString());
	    	
	    	//routing table lo ki add cheyi IP's nee
	    	
	    }else if(S_JR[1].equals("9999")){
	    	System.err.println("Error: while adding new node to routing table");
	    }else {
	    	System.out.println("Status: "+  JR);
	    }	   
		}client_Socket.close();
	}
}
}