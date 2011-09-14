package org.encorelab.sail;

/**
 * Event responder
 * 
 * @author anthonjp
 */
public abstract class EventResponder implements Runnable {
	protected String eventType;
	private Event event;

	/**
	 * sets an event
	 * 
	 * @param ev
	 */
	protected void setEvent(Event ev) {
		this.event = ev;
	}
	
	/**
	 * sends a message in response to an event
	 * 
	 * @param ev
	 */
	abstract public void respond(Event ev);
	
	/**
	 * Does the response for an event
	 */
	public void run() {
		respond(this.event);
	}
	
	/**
	 * gets the event type
	 * 
	 * @return
	 */
	public String getEventType() {
		return eventType;
	}
}

