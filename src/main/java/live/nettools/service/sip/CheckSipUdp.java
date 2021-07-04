package live.nettools.service.sip;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class CheckSipUdp{
    //Check remote SIP service availability
   
	public static String checkSipUdp(String ipAddress, int outPort,String login)throws Exception{
		
		String response = null;
		try {
	        DatagramSocket sipSocket = new DatagramSocket(0);
	        sipSocket.setSoTimeout(1000);
	        InetAddress inetIpAddress = InetAddress.getByName(ipAddress);
	        byte [] sendData = new byte[1024];
	        byte [] receiveData = new byte[1024];
	        String method = "REGISTER sip:" + ipAddress + ":" + outPort + " SIP/2.0\r\nCall-ID: " + generateCallId() + "@" + InetAddress.getLocalHost().getHostAddress() +"\r\nCSeq: 1 REGISTER\r\nFrom: <sip:" + InetAddress.getLocalHost().getHostAddress() + ":" + sipSocket.getLocalPort() + ">;tag=" + new Random().nextInt() + "\r\nTo: <sip:"+login+"@" + ipAddress + ":" + outPort + ">\r\nVia: SIP/2.0/UDP " + InetAddress.getLocalHost().getHostAddress() + ":" + sipSocket.getLocalPort() + ";branch=z9hG4bK-323032-" + generateCallId() + "\r\nMax-Forwards: 70\r\nContact: <sip:" + InetAddress.getLocalHost().getHostAddress()+ ":" + sipSocket.getLocalPort() + ">\r\nContent-Length: 0\r\n\r\n";
	        //System.out.println(method);        
	        sendData = method.getBytes();
	        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, inetIpAddress, 5060);
	        sipSocket.send(sendPacket);
	        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	        sipSocket.receive(receivePacket);
	        response = method +"<br />";
	        response += new String(receivePacket.getData());
	        //System.out.println(ipAddress + "\n" + response);
	        sipSocket.close();
	        
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}    
        return response;
    }

    //Generating unique callID
    public static String generateCallId(){
       Random r = new Random();
       long l1 = r.nextLong() * r.nextLong();
       long l2 = r.nextLong() * r.nextLong();
       return Long.toHexString(l1) + Long.toHexString(l2);

    }

    public static void main(String [] args) throws Exception{
        CheckSipUdp sip = new CheckSipUdp();
        //sip.checkSipUdp(args[0], Integer.parseInt(args[1]));
        sip.checkSipUdp("179.185.82.93	", 5069,"s");
    }
}