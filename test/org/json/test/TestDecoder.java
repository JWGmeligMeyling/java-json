package org.json.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.Decoder;
import org.json.JSONException;
import org.json.test.TestWrappers.ObjectWithArray;
import org.json.test.TestWrappers.*;
import org.junit.Test;

public class TestDecoder {

	/**
	 * This case tests if an empty object can be parsed into an Empty object
	 * wrapper. The JSON representation of an empty object is {}. This method
	 * tests if such an object can indeed be instantiated and parsed by the
	 * decoder.
	 */
	@Test public final void testEmptyObject() {
		String input = "{}";
		EmptyObjectWrapper result = Decoder.decode(EmptyObjectWrapper.class, input);
		assertTrue(result instanceof EmptyObjectWrapper);
	}
	
	/**
	 * This case tests if a plain object can be parsed into an PlainObjectWrapper.
	 * A plain object may contain attributes of the primitive types, String or Number
	 * objects. An object with inner object is considered a more complex structure
	 * and tested separately.
	 */
	@Test public final void testPlainObject() {
		String input = "{\"value\" : \"test\", value1:2342342, value2:23.2342352353, value3:true}";
		PlainObjectWrapper result = Decoder.decode(PlainObjectWrapper.class, input);
		PlainObjectWrapper expected = new PlainObjectWrapper();
		expected.value = "test";
		expected.value1  = 2342342;
		expected.value2 = 23.2342352353;
		expected.value3 = true;
		assertEquals(expected, result);
	}
	
	/**
	 * This test case tests if a plain object containing a required value (set by
	 * the annotation) is parsed correctly.
	 */
	@Test public final void testPOWWithRequirements() {
		String input = "{ required : \"_required\", optional : \"_optional\" }";
		POWWithRequirements result = Decoder.decode(POWWithRequirements.class, input);
		POWWithRequirements expected = new POWWithRequirements();
		expected.required = "_required";
		expected.optional  = "_optional";
		assertEquals(expected, result);
	}
	

	/**
	 * This test case tests if the decoder throws an exception as expected, when
	 * a required attribute is undefined in the input string.
	 */
	@Test(expected=JSONException.class) public final void testPOWWithRequirementsFail() {
		String input = "{ optional : \"_optional\" }";
		POWWithRequirements result = Decoder.decode(POWWithRequirements.class, input);
		POWWithRequirements expected = new POWWithRequirements();
		expected.optional  = "_optional";
		assertEquals(expected, result);
	}
	
	/**
	 * This case tests if final fields set by constructor parameters are decoded
	 * properly
	 */
	@Test public final void testFinalFields() {
		String input = "{ finalField : \"finalFieldValue\", additionalField : \"additionalFieldValue\" }";
		FinalFields result = Decoder.decode(FinalFields.class, input);
		FinalFields expected = new FinalFields("finalFieldValue");
		expected.additionalField  = "additionalFieldValue";
		assertEquals(expected, result);
	}
	
	/**
	 * This case tests if a not required final field can be decoded as null value
	 */
	@Test public final void testNullableFinalField() {
		String input = "{ additionalField : \"additionalFieldValue\" }";
		FinalFields result = Decoder.decode(FinalFields.class, input);
		FinalFields expected = new FinalFields(null);
		expected.additionalField  = "additionalFieldValue";
		assertEquals(expected, result);
	}
	
	/**
	 * This case tests if different types of attribute privacies can be accessed
	 */
	@Test public final void testPrivateAccess() {
		String input = "{ publicMember : \"publicValue\", privateMember : \"privateValue\","
				+ "protectedMember : \"protectedValue\", packageMember : \"packageValue\" }";
		TestPrivateAccess result = Decoder.decode(TestPrivateAccess.class, input);
		TestPrivateAccess expected = new TestPrivateAccess();
		expected.publicMember = "publicValue";
		expected.setPrivateMember("privateValue");
		expected.protectedMember = "protectedValue";
		expected.packageMember = "packageValue";
		assertEquals(expected, result);
	}
	
	/**
	 * This case tests if complex objects (objects with inner structures) can be
	 * decoded properly
	 */
	@Test public final void testCompexObject() {
		String inner = "{\"value\" : \"test\", value1:2342342, value2:23.2342352353, value3:true}";
		String input = "{\"test\" : \"ownAttribute\", innerObject : " + inner + "}";
	
		PlainObjectWrapper innerObj = new PlainObjectWrapper();
		innerObj.value = "test";
		innerObj.value1  = 2342342;
		innerObj.value2 = 23.2342352353;
		innerObj.value3 = true;
		
		ComplexObject expected = new ComplexObject();
		expected.test = "ownAttribute";
		expected.innerObject = innerObj;
		
		ComplexObject result = Decoder.decode(ComplexObject.class, input);
		assertEquals(expected, result);
	}
	
	/**
	 * Values in a JSON Object should always be an key value pair, and a Key can only
	 * be a String or Number (which is treated as a String as well)
	 */
	@Test(expected=JSONException.class) public final void testInvalidInput1() {
		String input = "{{}}";
		Decoder.decode(EmptyObjectWrapper.class, input);
	}

	/**
	 * Values in a JSON Object should always be key/value pairs. 
	 */
	@Test(expected=JSONException.class) public final void testInvalidInput2() {
		String input = "{ a, b : true}";
		Decoder.decode(EmptyObjectWrapper.class, input);
	}

	/**
	 * Keys and values should be separated by a colon, multiple colons cannot be parsed
	 */
	@Test(expected=JSONException.class) public final void testInvalidInput3() {
		String input = "{ a : \"a\" : \"a\", b : true}";
		Decoder.decode(EmptyObjectWrapper.class, input);
	}

	/**
	 * Quotes should be escaped
	 */
	@Test(expected=JSONException.class) public final void testInvalidInput4() {
		String input = "{ a : \"as\"df\", b : true}";
		Decoder.decode(EmptyObjectWrapper.class, input);
	}

	/**
	 * Key/value pairs
	 */
	@Test(expected=JSONException.class) public final void testInvalidInput5() {
		String input = "{ a : \"as\", \"df\", b : true}";
		Decoder.decode(EmptyObjectWrapper.class, input);
	}
	
	/**
	 * Array values should not be separated by a colon
	 */
	@Test(expected=JSONException.class) public final void testArrayWithColon() {
		String input = "{ stringValue : \"myStringvalue\", stringList : [ \"arrayValue1\" : \"array Value With Space\" ] }";
		Decoder.decode(ObjectWithArray.class, input);
	}

	/**
	 * Arrays should not be closed with an unmatched Object bracket
	 */
	@Test(expected=JSONException.class) public final void testArrayWithCloseBracket() {
		String input = "{ stringValue : \"myStringvalue\", stringList : [ \"arrayValue1\" , \"array Value With Space\" } ] }";
		Decoder.decode(ObjectWithArray.class, input);
	}

	/**
	 * Correctly escaped quote
	 */
	@Test public final void testEscapedQuote() {
		String input = "{\"value\" : \"te\\\"st\"}";
		PlainObjectWrapper result = Decoder.decode(PlainObjectWrapper.class, input);
		PlainObjectWrapper expected = new PlainObjectWrapper();
		expected.value = "te\"st";
		assertEquals(expected, result);
	}
	
	/**
	 * Escaped bracket
	 */
	@Test public final void testEscapedBracket() {
		String input = "{\"value\" : \"te\\}st\"}";
		PlainObjectWrapper result = Decoder.decode(PlainObjectWrapper.class, input);
		PlainObjectWrapper expected = new PlainObjectWrapper();
		expected.value = "te}st";
		assertEquals(expected, result);
	}
	
	/**
	 * Test object with unused array
	 */
	@Test public final void testObjWithUnusedArray() {
		String input = "{\"value\" : \"test\", arr : [ 1,2,3 ], value1:2342342, value2:23.2342352353, value3:true}";
		PlainObjectWrapper result = Decoder.decode(PlainObjectWrapper.class, input);
		PlainObjectWrapper expected = new PlainObjectWrapper();
		expected.value = "test";
		expected.value1  = 2342342;
		expected.value2 = 23.2342352353;
		expected.value3 = true;
		assertEquals(expected, result);
	}
	
	/**
	 * Test arrays with String values
	 */
	@Test public final void testObjectWithArray(){
		String input = "{ stringValue : \"myStringvalue\", stringList : [ \"arrayValue1\", \"array Value With Space\" ] }";
		ObjectWithArray result = Decoder.decode(ObjectWithArray.class, input);
		ObjectWithArray expected = new ObjectWithArray();
		expected.stringValue = "myStringvalue";
		expected.stringList = new ArrayList<String>();
		expected.stringList.add("arrayValue1");
		expected.stringList.add("array Value With Space");
		assertEquals(expected, result);
	}
	

	/**
	 * Test array with JSONSerializable objects within
	 */
	@Test public final void testObjectWithComplexArray(){
		String input = "{ stringValue : \"myStringvalue\", complexList : ["
				+ "{\"value\" : \"test\", value1:2342342, value2:23.2342352353, value3:true}, "
				+ "{\"value\" : \"test\", value1:2342342, value2:23.2342352353, value3:true}"
			+ "] }";
		ObjectWithComplexArray result = Decoder.decode(ObjectWithComplexArray.class, input);
		
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
		
		ObjectWithComplexArray expected = new ObjectWithComplexArray();
		expected.stringValue = "myStringvalue";
		expected.complexList = new ArrayList<PlainObjectWrapper>();
		expected.complexList.add(object1);
		expected.complexList.add(object2);
		
		assertEquals(expected, result);
	}
	
	/**
	 * Test to set a final array through constructor
	 */
	@Test public final void testFinalArray() {
		String input = "{ stringList : [ \"arrayValue1\", \"array Value With Space\" ] }";
		ObjectWithFinalArray result = Decoder.decode(ObjectWithFinalArray.class, input);
		List<String> list = new ArrayList<String>();
		list.add("arrayValue1");
		list.add("array Value With Space");
		ObjectWithFinalArray expected = new ObjectWithFinalArray(list);
		assertEquals(expected, result);
	}
	
	/**
	 * Test for a simple String map
	 */
	@Test public final void testStringMap() {
		String input = "{ myMap : { key1 : \"value1\", key2 : \"value2\" }}";
		ObjectWithMap result = Decoder.decode(ObjectWithMap.class, input);
		ObjectWithMap expected = new ObjectWithMap();
		expected.myMap = new HashMap<String, String>();
		expected.myMap.put("key1", "value1");
		expected.myMap.put("key2", "value2");
		assertEquals(expected, result);
	}
	
	/**
	 * Test a Map that contains Objects as values
	 */
	@Test public final void testComplexMap() {
		String input = "{ complexObject : { key1 : {\"value\" : \"test\", value1:2342342, value2:23.2342352353, value3:true},"
				+ "key2 : {\"value\" : \"test\", value1:2342342, value2:23.2342352353, value3:false }}}";
		ObjectWithComplexMap result = Decoder.decode(ObjectWithComplexMap.class, input);
		ObjectWithComplexMap expected = new ObjectWithComplexMap();
		expected.complexObject = new HashMap<String, PlainObjectWrapper>();
		
		PlainObjectWrapper object1 = new PlainObjectWrapper();
		object1.value = "test";
		object1.value1  = 2342342;
		object1.value2 = 23.2342352353;
		object1.value3 = true;
		expected.complexObject.put("key1", object1);
		
		PlainObjectWrapper object2 = new PlainObjectWrapper();
		object2.value = "test";
		object2.value1  = 2342342;
		object2.value2 = 23.2342352353;
		object2.value3 = false;
		expected.complexObject.put("key2", object2);
		
		assertEquals(expected, result);
	}
}
