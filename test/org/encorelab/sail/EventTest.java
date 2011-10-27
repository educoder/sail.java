package org.encorelab.sail;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

public class EventTest {

	@Test
	public void testEventStringObject() {
		Event sev = new Event("test", null);
		assertEquals("Event type", "test", sev.getType());
		assertNull("Event payload", sev.getPayload());
		
		Map<String,Object> payload = new HashMap<String,Object>();
		payload.put("foo", "faa");
		payload.put("double", 123.456);
		
		Event sev2 = new Event("test2", payload);
		assertEquals("Event type", "test2", sev2.getType());
		assertEquals("Event payload 'foo'", "faa", sev2.getPayloadAsMap().get("foo"));
		assertEquals("Event payload 'double'", 123.456, sev2.getPayloadAsMap().get("double"));
		assertNotNull(sev2.getTimestamp());
	}

	@Test
	public void testEventStringObjectMapOfStringObject() {
		// with explicit timestamp
		
		Map<String,Object> meta = new HashMap<String,Object>();
		meta.put("origin", "foo");
		meta.put("timestamp", getTestTimestamp().toDate());
		
		Event sev = new Event("test", null, meta);
		
		assertEquals("Event origin", "foo", sev.getOrigin());
		assertEquals("Event timestamp", getTestTimestamp().toDate(), sev.getTimestamp());
		
		// with explicit null timestamp
		
		Map<String,Object> meta2 = new HashMap<String,Object>();
		meta2.put("timestamp", null);
		
		Event sev2 = new Event("test", null, meta2);
		
		assertNull("Event timestamp should be null", sev2.getTimestamp());
		
		// automatic timestamp 
		
		Map<String,Object> meta3 = new HashMap<String,Object>();
		meta.put("timestamp", null);
		
		Event sev3 = new Event("test", null, meta3);
		
		assertNotNull("Event timestamp should not be null", sev3.getTimestamp());
	}

	@Test
	public void testToJson() {
		Map<String,Object> meta = new HashMap<String,Object>();
		meta.put("origin", "foo");
		meta.put("timestamp", getTestTimestamp().toDate()); 
		Map<String,Object> payload = new HashMap<String,Object>();
		payload.put("foo", "faa");
		payload.put("double", 123.456);
		
		Event sev = new Event("test", payload, meta);
		
		String expectedJson = "{\"eventType\":\"test\",\"payload\":{\"foo\":\"faa\",\"double\":123.456},\"origin\":\"foo\",\"timestamp\":\"2011-10-27T17:21:17Z\"}";
		
		// FIXME: timezone issues... timestamp should be in UTC but is currently coming back in local timezone... how to fix this? 
		assertEquals(expectedJson, sev.toJson());
	}

	@Test
	public void testFromJsonString() {
		fail("Not yet implemented");
	}

	@Test
	public void testFromJsonStringClass() {
		fail("Not yet implemented");
	}

	
	/**
	 * @return Date instance for Thu Oct 27 17:21:17 UTC 2011
	 */
	protected DateTime getTestTimestamp() {
//		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//		cal.set(2011, 9, 27, 17, 21, 17);
//		return cal.getTime();
		
//		Date timestamp = null;
//		try {
//			timestamp = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")).parse("2011-10-27T17:21:17Z");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return timestamp;
		
		return DateTimeFormat.forPattern(Event.DATETIME_PATTERN).
				parseDateTime("2011-10-27T17:21:17Z");
	}
}
