package org.encorelab.sail.examples;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.encorelab.sail.Event;
import org.encorelab.sail.EventResponder;
import org.encorelab.sail.agent.Agent;
import org.jivesoftware.smack.XMPPException;

import com.google.gdata.client.Query;
import com.google.gson.Gson;

/**
 * Test agent that looks up a word in google dictionary.
 * 
 * @author anthonjp
 *
 */
public class DictionaryAgent extends Agent {

	Logger logger = Logger.getLogger(DictionaryAgent.class.getName());

	/**
	 * fires the agent
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					DictionaryAgent agent = new DictionaryAgent();
					agent.setName("DictionaryAgent"); // can be omitted

					System.out.println("TheDictionaryAgent Connecting...");
					agent.connect("imediad.uio.no");

					System.out.println("Logging in...");
					agent.login("dictionaryagent", "dictionaryagent");

					System.out.println("Setting up responders...");
					agent.setupEventResponders();

					System.out.println("Listening for Sail events...");
					agent.listen();

					System.out.println("Joining groupchat...");
					agent.joinGroupchat("s@c.imedi.uio.no");

					// FIXME: is this really the best way to keep the agent
					// alive?
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
		 * {"eventType":"lookup","payload":{"word":"organic"}}
		 * 
		 * it will respond by triggering an event like:
		 * 
		 * {"eventType":"definition","payload":{"definition":"Of, relating to, or denoting compounds containing carbon
		 *  (other than simple binary compounds and salts) and chiefly or ultimately of biological origin","word":"organic"}}
		 */
		listener.addResponder("lookup", new EventResponder() {
			public void respond(Event ev) {
				logger.info("lookup " + ev);
				String fromJid = ev.getFrom();
				String fromUsername = fromJid.split("/")[1];
				Map payload = ev.getPayloadAsMap();
				String word = (String) payload.get("word");
				String definition = lookUpWord(word, ev);

				if (definition != null) {
					HashMap<String, String> newPayload = new HashMap<String, String>();
					newPayload.put("word", word);
					newPayload.put("definition", definition);
					Event event = new Event("definition", newPayload);
					xmpp.sendEvent(event);
				}

			}
		});
	}

	/**
	 * looks up a word and sends its definition back. 
	 * 
	 * @param word
	 * @param ev 
	 * @return
	 */
	public String lookUpWord(String word, Event ev) {

		String definition = null;
		HttpGet httpget = new HttpGet(
				"http://www.google.com/dictionary/json?callback=dict_api.callbacks.id100&q="
						+ word + "&sl=en&tl=en");
		System.out.println(httpget.getURI());
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String callback = EntityUtils.toString(entity);

			String removeStart = StringUtils.stripStart(callback,
					"dict_api.callbacks.id100(");
			String finalString = StringUtils
					.stripEnd(removeStart, ",200,null)");

			logger.info("String to lookup: " + finalString);

			if (finalString == null) {
				return "No Definition Found";
			} else {
				
				//FIXME remove this shit. 
//				Gson gson = new Gson();
//				Query q = gson.fromJson(finalString,Query.class);
//				
				//Query q = (String) ev.getPayloadAsMap().get("text");
		
				logger.info("definition: " + definition);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return definition;
	}

}
