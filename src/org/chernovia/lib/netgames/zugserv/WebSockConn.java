package org.chernovia.lib.netgames.zugserv;

import java.net.InetAddress;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@WebSocket
public class WebSockConn extends ConnAdapter {
	
	public static WebSockServ SERVER;
	public static int VERBOSITY = 4;
	private Session session;
	private String last_msg = "";
	private int repeats = 0;
	private ObjectMapper mapper = new ObjectMapper();
	
	public WebSockConn() { setServ(SERVER); }
	
	@OnWebSocketConnect
    public void onConnect(Session s) {
		//log("Connecting: " + s.getLocalAddress());
		session = s; 
		getServ().connect(this);
    }
		
	public void log(String msg) {
		System.out.println(msg);
	}
	
	@OnWebSocketMessage
    public void onMessage(String message){
		if (last_msg.equals(message)) repeats++; 
		else { 
			if (repeats > 0) log("(repeats " + repeats + "x)");  
			last_msg = message; repeats = 0; 
			log("Message received: " + message);
		}
		getServ().newMsg(this,message);
    }
 
	@OnWebSocketClose
    public void onClose(int statusCode, String reason){
        log(reason);
        getServ().disconnected(this);
    }
	
    @OnWebSocketError
    public void onError(Throwable t) {
    	log("Error: " + t.getMessage());
    }
	
    public void augh(String msg) { System.out.println(msg); System.exit(1); }
	public Session getSession() { return session; }

	@Override
	public void close() { session.close(); }
	
	@Override
	public InetAddress getAddress() { return session.getRemoteAddress().getAddress(); }
	
	@Override
	public void tell(String type, String msg) {
		ObjectNode obj = mapper.createObjectNode();
		obj.put(type,msg);  
		tell(obj.toString());
	}
	
	@Override
	public void tell(String type, JsonNode msg) { 
		ObjectNode obj = mapper.createObjectNode();
		obj.set(type,msg);
		tell(obj.toString());
	}
	
	private void tell(String message) {
		String sendStr = "--> " + getHandle() + ": ";
		try {
			if (VERBOSITY >= 1 && VERBOSITY < 8) {
				int i = VERBOSITY * 16;
				log(sendStr + (message.length() > i ? message.substring(0,i) : message));
			}
			else if (VERBOSITY >= 8) log(sendStr + message);
			getSession().getRemote().sendString(message,null);
		} 
		catch (Exception e) { 
			log("Error writing to socket: " + e.getMessage());
			getSession().close();
		}
	}

}