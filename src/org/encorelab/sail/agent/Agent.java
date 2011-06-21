package org.encorelab.sail.agent;

import org.encorelab.sail.EventListener;
import org.encorelab.sail.XMPPClient;
import org.jivesoftware.smack.XMPPException;

public abstract class Agent {
	static int DEFAULT_PORT = 5222;

	protected String name;
	protected XMPPClient xmpp;

	public EventListener listener;
	
	public Agent() {
		xmpp = new XMPPClient();
		listener = new EventListener();
	}

	public void connect(String hostname, int port) throws XMPPException {
		if (this.name == null) {
			this.name = getClass().getName();
		}
		xmpp.configure(hostname, port);
		xmpp.connect();
	}

	public void connect(String hostname) throws XMPPException {
		connect(hostname, DEFAULT_PORT);
	}
	
	public void login(String username, String password) throws XMPPException {
		xmpp.login(username, password, name);
	}
	
	public void listen() {
		xmpp.addEventListener(listener);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void joinGroupchat(String roomJid) {
		xmpp.joinGroupchat(roomJid, name);
	}

	public String getAgentJid() {
		return xmpp.getMyJid();
	}

	public String getRoomJid() {
		return xmpp.getRoomJid();
	}
	
	public String getAgentJidInRoom() {
		return xmpp.getMyJidInRoom();
	}
}
