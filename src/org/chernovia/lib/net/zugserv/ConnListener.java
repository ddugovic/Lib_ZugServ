package org.chernovia.lib.net.zugserv;

public interface ConnListener {
	void newMsg(Connection conn, String msg);
	void loggedIn(Connection conn);
	void disconnected(Connection conn);
}
