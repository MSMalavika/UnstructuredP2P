package UnstructuredP2P;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class command {
	
// Storage of nodes details received from Bootstrap Server
	ArrayList<String> Node_info = new ArrayList<String>();	
	Hashtable<String, ArrayList<String>> routingTable = new Hashtable<String, ArrayList<String>>();
	ArrayList<String> Sock1_RTDetails = new ArrayList<String>();
	ArrayList<String> Sock2_RTDetails = new ArrayList<String>();
	ArrayList<String> Sock3_RTDetails = new ArrayList<String>();
	
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
    String nInfo = new String();
    for(int j= 0; j<Node_info.size(); j++){
    	nInfo  = nInfo + Node_info.get(j) + " ";
    }
    nInfo = nInfo.trim();
    byte[] nInfoB = nInfo.getBytes();
    FileOutputStream nodeInfo = new FileOutputStream("Node_info.txt");
    nodeInfo.write(nInfoB);
    nodeInfo.close();
        
    client_Socket.close();
	}

public void join(int Node_port) throws IOException{
	
	String Node_IP = InetAddress.getLocalHost().getHostAddress();
	String join_req = "JOIN" + " " + Node_IP + " " + Node_port;
	int len = join_req.length() + 4;
	String join_msg = String.format("%04d", len) + " " +join_req;
	byte[] join_request = join_msg.getBytes();
	
	File inFile = new File ("Node_info.txt");
    String line=null;
    Scanner sc = new Scanner(inFile);
    while (sc.hasNextLine())
    {
        line = sc.nextLine();
    }
    sc.close();
    System.out.println ("Data from Input file: "+ line);
    String[] Info_node = line.split(" ");
    for(int n=0; n<Info_node.length;n++){
    Node_info.add(Info_node[n]);	
    }
    
	int noIP = Node_info.size();
	
	if(noIP == 0) { 
		System.out.println("this is the first node");			
	} else{
	
	DatagramSocket client_Socket = new DatagramSocket();	
	
// sending JOIN message to the nodes	
	for(int k=0; k<noIP; k++){	
		
		String[] SockADD= Node_info.get(k).split(":");
		System.out.println("node_info "+ k +" "+ Node_info.get(k));
		System.out.println("sock add "+ k+" "+ SockADD[0]+" "+SockADD[1] );
		System.out.println(new String(join_request));
		DatagramPacket JOIN_Packet = new DatagramPacket(join_request, join_request.length, InetAddress.getByName(SockADD[0]), Integer.parseInt(SockADD[1]));
		System.out.println("join mesg sent to node "+k);
		client_Socket.send(JOIN_Packet);
	}	
	
// receiving JOIN response
	
	byte[] JOIN_response = new byte[65000];
	
	int i=0;
	
	for(int n=0; n<noIP; n++){
		DatagramPacket JResponse = new DatagramPacket(JOIN_response, JOIN_response.length);
	    client_Socket.receive(JResponse);
	    
	    String JR = new String(JResponse.getData(),0,JResponse.getLength());
	    
	    String[] S_JR= JR.split(" ");
	    if(S_JR[2].equals("0")){	    	
	    	System.out.println("Status: Join Successful with " + JResponse.getAddress().toString());	    	
	    	String socket = JResponse.getAddress().toString()+":"+ Integer.toString(JResponse.getPort());
	    	i=i+1;
	    		if (i==1){
	    			routingTable.put(socket, Sock1_RTDetails);
	    			}else if (i==2){
	    				routingTable.put(socket, Sock2_RTDetails);
		    			} else if (i==3){
		    				routingTable.put(socket, Sock3_RTDetails);
		    				}
	    		
	    		System.out.println("map is "+ routingTable);
	    		
	    		File file = new File("RoutingTable.txt");
				try
				{
				   BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				   for(String p:routingTable.keySet())
				   {
				      bw.write(p + ":" + routingTable.get(p));
				      bw.newLine();
				   }
				   bw.flush();
				   bw.close();
				}catch (IOException e) {
					System.out.println("Error: " + e);
					e.printStackTrace();
				}
				
	    	}else if(S_JR[2].equals("9999")){
	    	System.err.println("Error: while adding new node to routing table");
	    }else {
	    	System.out.println("Status: "+ JR);
	    }	   
		}client_Socket.close();
	}
}

		public void DEL(InetAddress BS_ip, int BS_port) throws IOException{
			
			System.out.println("If you want to unregister a node give: <IP:Port> <user name>");	
			System.out.println("If you want to unregister the entire network give: <user name>");
			String unReg =System.console().readLine();
			
			DatagramSocket client_Socket = new DatagramSocket();
			
			String[] unreg = unReg.split(" ");
			switch(unreg.length) {
				case 1:
					
					String userName = unreg[0];
					String req =  "DEL UNAME" + " " +  userName ;
					int msglen =  req.length() + 5;
					String delmsg = String.format("%04d", msglen) + " " +req;
					byte[] delrequest = delmsg.getBytes();
					
				// Sending the unregister request to the BS
					
					DatagramPacket DELPacket = new DatagramPacket(delrequest, delrequest.length, BS_ip, BS_port);
					client_Socket.send(DELPacket);
					
				//receiving node socket address from BS
					
					byte[] BSunRegresp = new byte[65000];
				    DatagramPacket BSunReg = new DatagramPacket(BSunRegresp, BSunRegresp.length);
				    client_Socket.receive(BSunReg); 
				    
				    String BSr = new String(BSunReg.getData(),0,BSunReg.getLength());
				    String[] BSunReg_res = BSr.split(" ");
				    
				    if(BSunReg_res.length ==6) {
				    	
				    	switch(BSunReg_res[5]) {
				    	case "1":
				    		System.out.println();
				    		break;
				    	case "-1":
				    		System.err.println("Status: Error occurred while unregistering the node ");
				    		break;
				    	case "9999":
				    		System.out.println("Error: Username is not registered with BS");
				    		break;
				    	default:				    			
				    		System.out.println("Error: "+ BSr);
				    		break;
				    	
				    	}
				    	
					    }else if(BSunReg_res.length ==4) {
					    	System.err.println("Error: Error in DEL command ");
						    }else {
					    	System.out.println("Error: "+BSr);
						    	}
					
				case 2:
					String unRegIP = unreg[0].split(":")[0];
					String unRegPort= unreg[0].split(":")[1];
					String uname = unreg[1];
					
					String request =  "DEL IPADDRESS" + " " + unRegIP + " " + unRegPort + " " + uname ;					
					int msg_len =  request.length() + 5;
					String del_msg = String.format("%04d", msg_len) + " " +request;
					byte[] del_request = del_msg.getBytes();
					
				// Sending the unregister request to the BS
					
					
					DatagramPacket DEL_Packet = new DatagramPacket(del_request, del_request.length, BS_ip, BS_port);
					client_Socket.send(DEL_Packet);
					
				//receiving node socket address from BS
					
					byte[] BS_response = new byte[65000];
				    DatagramPacket BSResponse = new DatagramPacket(BS_response, BS_response.length);
				    client_Socket.receive(BSResponse); 
				    
				    String BSR = new String(BSResponse.getData(),0,BSResponse.getLength());
				    String[] BS_Response = BSR.split(" ");
				    
				    if (BS_Response[5].equals("9998")){
					    	System.err.println("Error: Not registered for the given user name" );
					    }else if(BS_Response[4].equals("-1")){
					    	System.out.println("Error: Error in DEL command ");
					    }else if(BS_Response[7].equals("1")){
					    	System.out.println("Status: UNREGISTERED");
					    }else{
					    	System.out.println("The DEL response received from BS: "+ BSR);
					    }
				            
				    client_Socket.close();
				    break;
				default:
					System.out.println("Give the command properly");
						
			}
			
		}

}