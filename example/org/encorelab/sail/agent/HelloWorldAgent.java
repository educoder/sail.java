package org.encorelab.sail.agent;

import java.util.Date;
import java.util.HashMap;

import org.encorelab.sail.Event;
import org.encorelab.sail.EventResponder;
import org.jivesoftware.smack.XMPPException;

public class HelloWorldAgent extends Agent {
	public static void main(String[] args) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					HelloWorldAgent agent = new HelloWorldAgent();
					agent.setName("TheHelloWorldAgent"); // can be omitted

					System.out.println("Connecting...");
					agent.connect("proto.encorelab.org");

					System.out.println("Logging in...");
					agent.login("hello_world",
							"a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");

					System.out.println("Setting up responders...");
					agent.setupEventResponders();

					System.out.println("Listening for Sail events...");
					agent.listen();

					System.out.println("Joining groupchat...");
					agent.joinGroupchat("s3@conference.proto.encorelab.org");

					// FIXME: is this really the best way to keep the agent alive?
					while (true) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							System.out.println("Agent killed.");
						}
					}
				} catch (XMPPException e) {
					System.err.println(e.getMessage());
				}
			}
		};

		thread.start();

		try {
			thread.join();
		} catch (InterruptedException e) {
			System.out.println("Agent killed.");
		}
	}

	public void setupEventResponders() {
		/*
		 * When the agent sees an event like:
		 * 
		 * {'eventType':'hello'}
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

	public void sayHelloTo(String to) {
		xmpp.sendEvent(new Event("welcome", "Hello " + to + "!"));
	}

	public void sayGoodbyeTo(String to) {
		HashMap<String, String> payload = new HashMap<String, String>();
		payload.put("msg", "Bye " + to + "!");
		payload.put("time", (new Date()).toString());

		Event ev = new Event("farewell", payload);

		xmpp.sendEvent(ev);
	}
}
