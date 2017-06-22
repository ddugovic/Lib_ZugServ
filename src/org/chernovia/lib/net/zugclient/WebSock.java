package org.chernovia.lib.net.zugclient;

import java.util.ArrayList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;

@WebSocket
public class WebSock {
	String sock_name;
	
	private static final Logger LOG = Log.getLogger(WebSock.class);
	private ArrayList<WebSockListener> listeners;
    public Session session;
    
    public void addListener(WebSockListener l) { listeners.add(l); }
    public void removeListener(WebSockListener l) { listeners.remove(l); }

    
    public WebSock(String name) {
    	LOG.info("Socket created: " + name);
    	sock_name = name;
    	listeners = new ArrayList<WebSockListener>();
    }
    
    public boolean isConnecting() { return (session == null); }
    public boolean isConnected() { return (session != null && session.isOpen()); }
    
    public void end() {	if (session!=null) session.close(); }
    
    public void send(String msg) { send(msg,false); }
    public void send(String msg, boolean log) {
    	if (isConnected()) {
    		if (log) LOG.info("Sending -> " + msg);
    		session.getRemote().sendStringByFuture(msg);
    	}
    }

    @OnWebSocketConnect
    public void onConnect(Session sess) {
        LOG.info("onConnect({})",sess);
        session = sess; 
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        LOG.info("onClose({}, {})", statusCode, reason);
    	if (session != null) session.close();
    	for (WebSockListener l : listeners) l.sock_fin(this);
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        LOG.warn(cause);
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
    	LOG.info(sock_name + "---> NEW MESSAGE: " + msg + "," + listeners.size());
        for (WebSockListener l : listeners) l.sock_msg(this,msg);
    }
    
    @OnWebSocketFrame
    public void onFrame(Frame frame) {
    	LOG.info(frame.toString());
    }
    
    public String toString() {
    	return sock_name;
    }
}

