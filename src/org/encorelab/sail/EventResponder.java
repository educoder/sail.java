package org.encorelab.sail;

public abstract class EventResponder implements Runnable {
	protected String eventType;
	private Event event;
	
	protected void setEvent(Event ev) {
		this.event = ev;
	}
	
	abstract public void respond(Event ev);
	
	public void run() {
		respond(this.event);
	}
	
	public String getEventType() {
		return eventType;
	}
}

