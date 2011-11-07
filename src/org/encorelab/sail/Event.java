package org.encorelab.sail;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.format.DateTimeFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * A Sail Event with JSON serialization/deserialization.
 * 
 * Examples:
 * 
 *    // Serialization
 * 
 *    // String payload
 *    Event ev = new Event("example", "foo");
 *    ev.toJson();  // ==> {"eventType":"example","payload":"foo"}
 *    
 *    // List payload
 *    List<Object> list = new ArrayList<String>();
 *    list.add("foo");
 *    list.add(123);
 *    Event ev = new Event("example", list);
 *    ev.toJson();  // ==> {"eventType":"example","payload":["foo",123]}
 *    
 *    // Map payload
 *    Map<String,Object> map = new HashMap<String,Object>();
 *    map.put("alpha","Hello");
 *    map.put("omega","Goodbye!");
 *    Event ev = new Event("example", map);
 *    ev.toJson();  // ==> {"eventType":"example","payload":{"alpha":"Hello","omega":"Goodbye!"}}
 * 
 * 
 *    // Deserialization
 *    
 *    Event ev = Event.fromJson("{eventType:\"example\",payload:\"foo\"}");
 *    ev.getType();  // ==> "example"
 *    ev.getPayloadAsString();  // ==> "foo"
 *    (String) ev.getPayload(); // ==> "foo"
 *    
 *    Event ev = Event.fromJson("{eventType:\"example\",payload:{alpha:\"Hello\",omega:\"Goodbye!\"}}")
 *    ev.getPayloadAsMap().get("alpha");  // ==> "Hello"
 *    ev.getPayloadAsMap().get("beta");   // ==> "Goodbye!"
 *    
 *    Event ev = Event.fromJson("{eventType:\"example\",\"payload\":{alpha:{exampleString:\"Example!\",exampleList:[\"a\",\"b\",\"c\"]},beta:[1,2,3,5,7]}}");
 *    ((List<String>) ((Map<String,Object>) ev.getPayloadAsMap().get("alpha")).get("exampleList")).get(1)  // ==> "b"
 * @author mzukowski
 */
public class Event {
	public static String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
	
	public Event(String type, Object payload) {
		this.eventType = type;
		this.payload = payload;
		this.timestamp = new Date();
	}
	
	/**
	 * Create a new event with custom metadata.
	 * 
	 * @param type
	 * @param payload
	 * @param meta Optional keys for `(Date) timestamp`, `(String) origin`, `(JsonObject) run`, and in the future other values.
	 */
	public Event(String type, Object payload, Map<String,Object> meta) {
		this.eventType = type;
		this.payload = payload;
		
		if (meta.containsKey("origin"))
			this.origin = (String) meta.get("origin");
	
		if (meta.containsKey("timestamp"))
			this.timestamp = (Date) meta.get("timestamp");
		else
			this.timestamp = new Date();
		
		if (meta.containsKey("run"))
			this.run = (JsonObject) meta.get("run");
	}

	protected String eventType;
	protected Object payload;
	protected String origin;
	protected Date timestamp;
	protected JsonObject run;
	
	// These fields should not be serialized (hence 'transient'). 
	// They are assigned after deserialization in 
	// org.encorelab.sail.EventListener. 
	protected transient String from;
	protected transient String to;
	protected transient String stanza;
	protected transient String rawPayload;

	public String getType() {
		return eventType;
	}

	public void setType(String type) {
		this.eventType = type;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getStanza() {
		return stanza;
	}

	public void setStanza(String stanza) {
		this.stanza = stanza;
	}

	public Object getPayload() {
		return payload;
	}
	
	public Object getPayload(Class payloadType) {
		if (rawPayload == null)
			return this.getPayload();
		
		Gson gson = new Gson();
		return gson.fromJson(rawPayload, payloadType);
	}

	public int getPayloadAsInt() {
		return ((Integer) payload).intValue();
	}

	public float getPayloadAsFloat() {
		return ((Float) payload).floatValue();
	}

	public double getPayloadAsDouble() {
		return ((Double) payload).doubleValue();
	}

	public String getPayloadAsString() {
		return (String) payload;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getPayloadAsMap() {
		return (Map<String, Object>) payload;
	}

	@SuppressWarnings("unchecked")
	public List<Object> getPayloadAsList() {
		return (List<Object>) payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public String toJson() {
		Gson gson = new GsonBuilder()
			.setDateFormat(Event.DATETIME_PATTERN).create();
		return gson.toJson(this);
	}
	
	public String toString() {
		return toJson();
	}

	public static Event fromJson(String json) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Event.class, new EventDeserializer());
		Gson gson = gsonBuilder.create();
		return gson.fromJson(json, Event.class);
	}
	
	public static Event fromJson(String json, Class payloadType) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Event.class, new EventDeserializer());
		Gson gson = gsonBuilder.create();
		return gson.fromJson(json, Event.class);
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	// based on code from http://stackoverflow.com/questions/2779251/convert-json-to-hashmap-using-gson-in-java/4799594#4799594
	private static class EventDeserializer implements JsonDeserializer<Event> {
		public Event deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonNull()) {
				return null;
			} else if (json.isJsonObject()) {
				JsonObject eventJson = json.getAsJsonObject();
				
				String eventType = eventJson.get("eventType").getAsString();
				Object payload = deserializePayload(eventJson.get("payload"), context);
				
				
//				
//				Date timestamp = 
//						DateTimeFormat.forPattern(DATETIME_PATTERN).
//						parseDateTime(eventJson.get("timestamp").getAsString()).
//						toDate();
				
				Map<String,Object> meta = new HashMap<String, Object>();
				meta.put("origin", eventJson.get("origin").getAsString());
				//meta.put("timestamp", timestamp);
				//meta.put("run", eventJson.get("run").getAsString());
				
				Event ev = new Event(eventType, payload, meta);
				if (eventJson.get("payload") == null)
					ev.rawPayload = null;
				else
					ev.rawPayload = eventJson.get("payload").toString();
				return ev;
			} else {
				throw new JsonParseException("Not a valid Sail Event: "+json.toString());
			}
		}
		
		private Object deserializePayload(JsonElement rawPayload, JsonDeserializationContext context) {
			if (rawPayload == null) {
				return null;
			} else if (rawPayload.isJsonPrimitive()) {
				return parsePrimitivePayload(rawPayload.getAsJsonPrimitive(), context);
			} else if (rawPayload.isJsonArray()) {
				return handleArrayPayload(rawPayload.getAsJsonArray(), context);
			} else { // TODO: are there other possibilities?
				return parseObjectPayload(rawPayload.getAsJsonObject(), context);
			}
		}
		
		private Map<String, Object> parseObjectPayload(JsonObject payload, JsonDeserializationContext context) {
			Map<String,Object> map = new LinkedHashMap<String, Object>();
			
			for(Map.Entry<String, JsonElement> entry : payload.entrySet())
				map.put(entry.getKey(), deserializePayload(entry.getValue(), context));
			
			return map;
		}
		
		private List<Object> handleArrayPayload(JsonArray payload, JsonDeserializationContext context) {
			List<Object> arr = new ArrayList<Object>(payload.size());
			for(int i = 0; i < payload.size(); i++)
				arr.add(i, deserializePayload(payload.get(i), context));
			return arr;
		}
		
		private Object parsePrimitivePayload(JsonPrimitive payload, JsonDeserializationContext context) {
			if (payload.isBoolean())
				return payload.getAsBoolean();
			else if (payload.isString())
				return payload.getAsString();
			else {
				BigDecimal bigDec = payload.getAsBigDecimal();
				try {
					bigDec.toBigIntegerExact();
					try {
						return bigDec.intValueExact();
					} catch (ArithmeticException e) {}
					return bigDec.longValue();
				} catch (ArithmeticException e) {}
				return bigDec.doubleValue();
			}
		}
	}
}
