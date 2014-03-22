package org.json.test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.Decoder;
import org.json.Encoder;
import org.json.test.TestWrappers.ComplexObject;
import org.json.test.TestWrappers.EmptyObjectWrapper;
import org.json.test.TestWrappers.FinalFields;
import org.json.test.TestWrappers.ObjectWithArray;
import org.json.test.TestWrappers.ObjectWithComplexArray;
import org.json.test.TestWrappers.ObjectWithComplexMap;
import org.json.test.TestWrappers.ObjectWithMap;
import org.json.test.TestWrappers.PlainObjectWrapper;
import org.json.test.TestWrappers.TestPrivateAccess;
import org.junit.Test;

public class TestEncoder {

	@Test public final void testEmptyObject() {
		String expected = "{}";
		EmptyObjectWrapper object = new EmptyObjectWrapper();
		assertEquals(expected, Encoder.encode(object));
	}
	
	@Test public final void testPlainObject() {
		String expected = "{\"value\":\"test\",\"value1\":2342342,\"value2\":23.2342352353,\"value3\":true}";
		PlainObjectWrapper object = new PlainObjectWrapper();
		object.value = "test";
		object.value1  = 2342342;
		object.value2 = 23.2342352353;
		object.value3 = true;
		assertEquals(expected, Encoder.encode(object));
	}
	
	/**
	 * This case tests if a not required final field can be decoded as null value
	 */
	@Test public final void testNullableFinalField() {
		String expected = "{\"finalField\":null,\"additionalField\":\"additionalFieldValue\"}";
		FinalFields object = new FinalFields(null);
		object.additionalField  = "additionalFieldValue";
		assertEquals(expected, Encoder.encode(object));
	}
	
	/**
	 * This case tests if different types of attribute privacies can be accessed
	 */
	@Test public final void testPrivateAccess() {
		String expected = "{\"publicMember\":\"publicValue\",\"protectedMember\":\"protectedValue\","
				+ "\"packageMember\":\"packageValue\",\"privateMember\":\"privateValue\"}";
		TestPrivateAccess object = new TestPrivateAccess();
		object.publicMember = "publicValue";
		object.setPrivateMember("privateValue");
		object.protectedMember = "protectedValue";
		object.packageMember = "packageValue";
		assertEquals(expected, Encoder.encode(object));
	}
	
	/**
	 * This case tests if complex objects (objects with inner structures) can be
	 * decoded properly
	 */
	@Test public final void testCompexObject() {
		String expected = "{\"test\":\"ownAttribute\",\"innerObject\":"
				+ "{\"value\":\"test\",\"value1\":2342342,\"value2\":23.2342352353,\"value3\":true}}";
		
		PlainObjectWrapper innerObj = new PlainObjectWrapper();
		innerObj.value = "test";
		innerObj.value1  = 2342342;
		innerObj.value2 = 23.2342352353;
		innerObj.value3 = true;
		
		ComplexObject object = new ComplexObject();
		object.test = "ownAttribute";
		object.innerObject = innerObj;
		
		assertEquals(expected, Encoder.encode(object));
	}
	
	@Test public final void testNullList() {
		ObjectWithArray object = new ObjectWithArray();
		object.stringValue = "myStrValue";
		object.stringList = null;
		assertEquals("{\"stringValue\":\"myStrValue\",\"stringList\":null}", Encoder.encode(object));
	}
	
	@Test public final void testEmptyList() {
		ObjectWithArray object = new ObjectWithArray();
		object.stringValue = "myStrValue";
		object.stringList = new ArrayList<String>();
		assertEquals("{\"stringValue\":\"myStrValue\",\"stringList\":[]}", Encoder.encode(object));
	}
	
	@Test public final void testSimpleList() {
		ObjectWithArray object = new ObjectWithArray();
		object.stringValue = "myStrValue";
		object.stringList = new ArrayList<String>();
		object.stringList.add("value1");
		object.stringList.add("value2");
		assertEquals("{\"stringValue\":\"myStrValue\",\"stringList\":[\"value1\",\"value2\"]}", Encoder.encode(object));
	}
	
	@Test public final void testComplexList() {
		String expected = "{\"stringValue\":\"myStringvalue\",\"complexList\":["
				+ "{\"value\":\"test\",\"value1\":2342342,\"value2\":23.2342352353,\"value3\":true},"
				+ "{\"value\":\"test\",\"value1\":2342342,\"value2\":23.2342352353,\"value3\":true}"
			+ "]}";
		
		PlainObjectWrapper object1 = new PlainObjectWrapper();
		object1.value = "test";
		object1.value1  = 2342342;
		object1.value2 = 23.2342352353;
		object1.value3 = true;

		PlainObjectWrapper object2 = new PlainObjectWrapper();
		object2.value = "test";
		object2.value1  = 2342342;
		object2.value2 = 23.2342352353;
		object2.value3 = true;
		
		ObjectWithComplexArray object = new ObjectWithComplexArray();
		object.stringValue = "myStringvalue";
		object.complexList = new ArrayList<PlainObjectWrapper>();
		object.complexList.add(object1);
		object.complexList.add(object2);
		
		assertEquals(expected, Encoder.encode(object));
	}

	@Test public final void testNullMap() {
		ObjectWithMap object = new ObjectWithMap();
		object.myMap = null;
		assertEquals("{\"myMap\":null}", Encoder.encode(object));
	}

	@Test public final void testEmptyMap() {
		ObjectWithMap object = new ObjectWithMap();
		object.myMap = new HashMap<String, String>();
		assertEquals("{\"myMap\":{}}", Encoder.encode(object));
	}

	@Test public final void testSimpleMap() {
		ObjectWithMap object = new ObjectWithMap();
		object.myMap = new HashMap<String, String>();
		object.myMap.put("key1", "value1");
		object.myMap.put("key2", "value2");
		assertEquals("{\"myMap\":{\"key2\":\"value2\",\"key1\":\"value1\"}}", Encoder.encode(object));
	}

	@Test public final void testComplexMap() {
		ObjectWithComplexMap object = new ObjectWithComplexMap();
		object.complexObject = new HashMap<String, PlainObjectWrapper>();
		
		PlainObjectWrapper object1 = new PlainObjectWrapper();
		object1.value = "test";
		object1.value1  = 2342342;
		object1.value2 = 23.2342352353;
		object1.value3 = true;
		object.complexObject.put("key1", object1);
		
		PlainObjectWrapper object2 = new PlainObjectWrapper();
		object2.value = "test";
		object2.value1  = 2342342;
		object2.value2 = 23.2342352353;
		object2.value3 = false;
		object.complexObject.put("key2", object2);
		
		assertEquals(Encoder.encode(Decoder.decode(ObjectWithComplexMap.class, Encoder.encode(object))), Encoder.encode(object));
	}
}
