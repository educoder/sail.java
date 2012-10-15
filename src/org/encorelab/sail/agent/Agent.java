package org.encorelab.sail.agent;

import org.encorelab.sail.EventListener;
import org.encorelab.sail.XMPPClient;
import org.jivesoftware.smack.XMPPException;

/**
 * Base agent class
 * 
 * @author anthonjp
 *
 */
public abstract class Agent {
	static int DEFAULT_PORT = 5222;

	protected String name;
	protected XMPPClient xmpp;

	public EventListener listener;
	
	public Agent() {
		xmpp = new XMPPClient();
		listener = new EventListener();
	}

	/**
	 * Connects to the xmpp server.
	 * 
	 * @param hostname
	 * @param port
	 * @throws XMPPException
	 */
	public void connect(String hostname, int port) throws XMPPException {
		if (this.name == null) {
			this.name = getClass().getName();
		}
		xmpp.configure(hostname, port);
		xmpp.connect();
	}

	/**
	 * Connects to the xmpp server.
	 * 
	 * @param hostname
	 * @throws XMPPException
	 */
	public void connect(String hostname) throws XMPPException {
		connect(hostname, DEFAULT_PORT);
	}
	
	/**
	 * Logins into the xmpp server
	 * 
	 * @param username
	 * @param password
	 * @throws XMPPException
	 */
	public void login(String username, String password) throws XMPPException {
		xmpp.login(username, password, name);
	}
	
	/**
	 * Starts listening for SAIL events.
	 */
	public void listen() {
		xmpp.addEventListener(listener);
	}
	
	
	/** 
	 * Starts listening for SAIL events
	 */
	public void start() {
		while(!Thread.currentThread().isInterrupted())
			listener.processPacket(xmpp.getPC().nextResult());
	}
	
	
	/**
	 * sets the name
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * joins the group chat
	 * 
	 * @param roomJid
	 */
	public void joinGroupchat(String roomJid) {
		xmpp.joinGroupchat(roomJid, name);
	}

	/**
	 * gets the agent id
	 * 
	 * @return
	 */
	public String getAgentJid() {
		return xmpp.getMyJid();
	}

	/**
	 * gets the room id
	 * 
	 * @return
	 */
	public String getRoomJid() {
		return xmpp.getRoomJid();
	}
	
	/**
	 * Gets the agent jid in the chat room.
	 * 
	 * @return
	 */
	public String getAgentJidInRoom() {
		return xmpp.getMyJidInRoom();
	}
}
