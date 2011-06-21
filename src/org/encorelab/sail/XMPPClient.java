package org.encorelab.sail;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class XMPPClient {
	private int state;
	
	// the JID of the conference room (MUC) for sending and listening to events
	protected String roomJid;
	
	protected String username;
	protected String resource;
	
	protected XMPPConnection xmpp;
	
	private ConnectionConfiguration xmppConfig;

	
	public static class State {
		public static final int DISCONNECTED = 0;
		public static final int CONNECTED = 1;
		public static final int LOGGED_IN = 2;
	}
	
	public void configure(String host, int port) {
		if (state != State.DISCONNECTED)
			throw new NullPointerException("XMPPService must be disconnected before it can be configured!");
		
		xmppConfig = new ConnectionConfiguration(host, port);
		xmppConfig.setSASLAuthenticationEnabled(true);
		xmppConfig.setSecurityMode(SecurityMode.disabled);
	}
	
	public void connect() throws XMPPException {
		if (xmppConfig == null)
			throw new NullPointerException("XMPPService must be configured before it can connect!");
		
		if (xmpp == null) {			
			xmpp = new XMPPConnection(xmppConfig);
		}
		
		xmpp.connect();
		
		state = State.CONNECTED;
	}
	
	public void login(String username, String password, String resource) throws XMPPException {
		if (!this.isConnected())
			throw new NullPointerException("XMPPService must be connected in before it can log in!");
		
		xmpp.login(username, password, resource);
		state = State.LOGGED_IN;
		
		this.username = username;
		this.resource = resource;
	}
	
	public void joinGroupchat(String roomJid, String resource) {
		if (!this.isLoggedIn())
			throw new NullPointerException("XMPPService must be logged in before it can join a conference room!");
		
		this.roomJid = roomJid;
		Presence p = new Presence(Presence.Type.available);
		p.setStatus("chat");
		p.setTo(getRoomJid()+"/"+resource);
		xmpp.sendPacket(p);
	}
	
	// TODO: implement me
	/*public void leaveGroupchat() {
		
	}*/
	
	public void disconnect() {
		xmpp.disconnect();
		state = State.DISCONNECTED;
	}
	
	public boolean isConnected() {
		if (xmpp.isConnected() && state == State.CONNECTED) {
			return true;
		} else {
			state = State.DISCONNECTED;
			return false;
		}
	}
	
	public boolean isLoggedIn() {
		if (xmpp.isAuthenticated() && state == State.LOGGED_IN) {
			return true;
		} else {
			if (xmpp.isConnected())
				state = State.CONNECTED;
			else
				state = State.DISCONNECTED;
			return false;
		}
	}
	
	public String getMyJid() {
		return username + "@" + xmpp.getHost();
	}
	
	public String getRoomJid() {
		return roomJid;
	}
	
	public String getMyJidInRoom() {
		return getRoomJid() + "/" + resource;
	}
	
	public void sendEvent(Event ev) {
		if (!this.isLoggedIn())
			throw new NullPointerException("XMPPService must be logged in before it can send!");
		
		Message m = new Message(this.roomJid, Message.Type.groupchat);
		m.setBody(ev.toJson());
		xmpp.sendPacket(m);
	}
	
	public void addEventListener(EventListener listener) {
		addEventListener(listener, new PacketTypeFilter(Message.class));
	}
	
	public void addEventListener(EventListener listener, PacketFilter filter) {
		xmpp.addPacketListener(listener, filter);
	}
}
