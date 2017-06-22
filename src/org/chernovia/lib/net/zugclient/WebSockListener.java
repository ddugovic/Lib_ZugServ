package org.chernovia.lib.net.zugclient;

public interface WebSockListener {
	public void sock_msg(WebSock sock, String message);
	public void sock_fin(WebSock sock);
}
