package org.json.test;
import static org.junit.Assert.*;

import org.json.Encoder;
import org.json.test.TestWrappers.ComplexObject;
import org.json.test.TestWrappers.EmptyObjectWrapper;
import org.json.test.TestWrappers.FinalFields;
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
	
}
