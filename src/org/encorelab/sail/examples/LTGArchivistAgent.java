/**
 * 
 */
package org.encorelab.sail.examples;

import java.util.Date;
import java.util.HashMap;

import org.encorelab.sail.Event;
import org.encorelab.sail.EventResponder;
import org.encorelab.sail.agent.Agent;

/**
 * @author tebemis
 *
 */
public class LTGArchivistAgent extends Agent {
	

	/**
	 * Configures the XMPP connection and the DB we are connecting to
	 * @param args
	 * @throws Exception
	 */
	public void setupFromCLI(String[] args) throws Exception {
		this.connect("ltg.evl.uic.edu");
		this.login("user", "pass");
		this.joinGroupchat("conference");
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Instatiate the agent
		LTGArchivistAgent archivist = new LTGArchivistAgent();
		try {
			// Setup agent
			archivist.setName("LTG archivist agent");
			archivist.setupFromCLI(args);
			// Setup the event responders
			archivist.setupEventResponders();
			// Start agent
			archivist.start();
		} catch (Exception e) {
			System.err.println("Impossible to parse arguments");
		}
		
	}


	private void setupEventResponders() {
		/*
		 * When the agent sees an event like:
		 * 
		 * {'eventType':'hello', 'payload': {}, 'origin':'obama'}
		 * 
		 * it will respond by triggering an event like:
		 * 
		 * {'eventType':'welcome','payload':"Hello John!"}
		 */
		listener.addResponder("hello", new EventResponder() {
			public void respond(Event ev) {
				String fromJid = ev.getFrom();
				String fromUsername = fromJid.split("/")[1];
				sayHelloTo(fromUsername);
			}
		});

		/*
		 * When the agent sees an event like:
		 * 
		 * {'eventType':'goodbye'}
		 * 
		 * it will respond by triggering an event like:
		 * 
		 * {'eventType':'farewell','payload':{'msg':"Bye John!",'time':"Tue Jun
		 * 21 16:20:55 EDT 2011"}}
		 */
		listener.addResponder("goodbye", new EventResponder() {
			public void respond(Event ev) {
				String fromJid = ev.getFrom();
				String fromUsername = fromJid.split("/")[1];
				sayGoodbyeTo(fromUsername);
			}
		});
	}

	/**
	 * says hello
	 * 
	 * @param to
	 */
	private void sayHelloTo(String to) {
		xmpp.sendEvent(new Event("welcome", "Hello " + to + "!"));
	}

	/**
	 * says goodbye
	 * 
	 * @param to
	 */
	private void sayGoodbyeTo(String to) {
		HashMap<String, String> payload = new HashMap<String, String>();
		payload.put("msg", "Bye " + to + "!");
		payload.put("time", (new Date()).toString());

		Event ev = new Event("farewell", payload);

		xmpp.sendEvent(ev);
	}

}
