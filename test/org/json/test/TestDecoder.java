package org.json.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.Decoder;
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
	@Test(expected=Decoder.ParseException.class) public final void testPOWWithRequirementsFail() {
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
	
	
	@Test(expected=Decoder.ParseException.class) public final void testInvalidInput1() {
		String input = "{{}}";
		Decoder.decode(EmptyObjectWrapper.class, input);
	}

	@Test(expected=Decoder.ParseException.class) public final void testInvalidInput2() {
		String input = "{ a, b : true}";
		Decoder.decode(EmptyObjectWrapper.class, input);
	}

	@Test(expected=Decoder.ParseException.class) public final void testInvalidInput3() {
		String input = "{ a : \"a\" : \"a\", b : true}";
		Decoder.decode(EmptyObjectWrapper.class, input);
	}

	@Test(expected=Decoder.ParseException.class) public final void testInvalidInput4() {
		String input = "{ a : \"as\"df\", b : true}";
		Decoder.decode(EmptyObjectWrapper.class, input);
	}

	@Test(expected=Decoder.ParseException.class) public final void testInvalidInput5() {
		String input = "{ a : \"as\", \"df\", b : true}";
		Decoder.decode(EmptyObjectWrapper.class, input);
	}
	
	@Test public final void testEscapedQuote() {
		String input = "{\"value\" : \"te\\\"st\"}";
		PlainObjectWrapper result = Decoder.decode(PlainObjectWrapper.class, input);
		PlainObjectWrapper expected = new PlainObjectWrapper();
		expected.value = "te\"st";
		assertEquals(expected, result);
	}
	
	@Test public final void testEscapedBracket() {
		String input = "{\"value\" : \"te\\}st\"}";
		PlainObjectWrapper result = Decoder.decode(PlainObjectWrapper.class, input);
		PlainObjectWrapper expected = new PlainObjectWrapper();
		expected.value = "te}st";
		assertEquals(expected, result);
	}
	
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
	
}
