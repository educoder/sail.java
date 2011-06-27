package org.encorelab.sail;

import java.util.HashMap;
import java.util.logging.Logger;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class EventListener implements PacketListener {
	
	Logger  logger = Logger.getLogger(EventListener.class.getName());
	
	private HashMap<String, EventResponder> responders;
	
	public EventListener() {
		this.responders = new HashMap<String, EventResponder>();
	}
	
	public void processPacket(Packet packet) {
		Event ev = Event.fromJson(((Message) packet).getBody());
		ev.setFrom(packet.getFrom());
		ev.setTo(packet.getTo());
		ev.setStanza(packet.toXML());
		
		logger.info("event: " + ev);
		
		if (responders.containsKey(ev.getType())) {
			EventResponder er = responders.get(ev.getType());
			er.setEvent(ev);
			logger.info("responder: " + ev);
			
			er.run();
		}
	}
	
	public void addResponder(EventResponder r) {
		if (r.getEventType() == null)
			throw new NullPointerException("EventResponder's event type cannot be null!");
		responders.put(r.getEventType(), r);
	}
	
	public void addResponder(String eventType, EventResponder r) {
		responders.put(eventType, r);
	}
	
	public void replaceResponder(EventResponder r) {
		responders.remove(r.getEventType());
		responders.put(r.getEventType(), r);
	}
}