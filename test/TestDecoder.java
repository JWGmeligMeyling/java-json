import static org.junit.Assert.*;

import org.json.Decoder;
import org.json.JSONAttribute;
import org.json.JSONSerializable;
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
		expected.privateMember = "privateValue";
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


	public static class EmptyObjectWrapper implements JSONSerializable {}

	public static class PlainObjectWrapper implements JSONSerializable {
		
		@JSONAttribute public String value;
		@JSONAttribute public int value1;
		@JSONAttribute public double value2;
		@JSONAttribute public boolean value3;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			result = prime * result + value1;
			long temp;
			temp = Double.doubleToLongBits(value2);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + (value3 ? 1231 : 1237);
			return result;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PlainObjectWrapper other = (PlainObjectWrapper) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			if (value1 != other.value1)
				return false;
			if (Double.doubleToLongBits(value2) != Double
					.doubleToLongBits(other.value2))
				return false;
			if (value3 != other.value3)
				return false;
			return true;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "PlainObjectWrapper [value=" + value + ", value1=" + value1
					+ ", value2=" + value2 + ", value3=" + value3 + "]";
		}
		
	};
	
	public static class POWWithRequirements implements JSONSerializable {
		
		@JSONAttribute(required=true) public String required;
		@JSONAttribute public String optional;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((optional == null) ? 0 : optional.hashCode());
			result = prime * result
					+ ((required == null) ? 0 : required.hashCode());
			return result;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			POWWithRequirements other = (POWWithRequirements) obj;
			if (optional == null) {
				if (other.optional != null)
					return false;
			} else if (!optional.equals(other.optional))
				return false;
			if (required == null) {
				if (other.required != null)
					return false;
			} else if (!required.equals(other.required))
				return false;
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "POWWithRequirements [required=" + required + ", optional="
					+ optional + "]";
		}
		
		
	}
	
	public static class FinalFields implements JSONSerializable {
		
		@JSONAttribute public final String finalField;
		@JSONAttribute public String additionalField;
		
		public FinalFields(@JSONAttribute(name="finalField") String finalField) {
			this.finalField = finalField;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((additionalField == null) ? 0 : additionalField
							.hashCode());
			result = prime * result
					+ ((finalField == null) ? 0 : finalField.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FinalFields other = (FinalFields) obj;
			if (additionalField == null) {
				if (other.additionalField != null)
					return false;
			} else if (!additionalField.equals(other.additionalField))
				return false;
			if (finalField == null) {
				if (other.finalField != null)
					return false;
			} else if (!finalField.equals(other.finalField))
				return false;
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "FinalFields [finalField=" + finalField
					+ ", additionalField=" + additionalField + "]";
		}
		
	}
	
	public static class TestPrivateAccess implements JSONSerializable {
		@JSONAttribute public String publicMember;
		@JSONAttribute protected String protectedMember;
		@JSONAttribute String packageMember;
		@JSONAttribute private String privateMember;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((packageMember == null) ? 0 : packageMember.hashCode());
			result = prime * result
					+ ((privateMember == null) ? 0 : privateMember.hashCode());
			result = prime
					* result
					+ ((protectedMember == null) ? 0 : protectedMember
							.hashCode());
			result = prime * result
					+ ((publicMember == null) ? 0 : publicMember.hashCode());
			return result;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestPrivateAccess other = (TestPrivateAccess) obj;
			if (packageMember == null) {
				if (other.packageMember != null)
					return false;
			} else if (!packageMember.equals(other.packageMember))
				return false;
			if (privateMember == null) {
				if (other.privateMember != null)
					return false;
			} else if (!privateMember.equals(other.privateMember))
				return false;
			if (protectedMember == null) {
				if (other.protectedMember != null)
					return false;
			} else if (!protectedMember.equals(other.protectedMember))
				return false;
			if (publicMember == null) {
				if (other.publicMember != null)
					return false;
			} else if (!publicMember.equals(other.publicMember))
				return false;
			return true;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "TestPrivateAccess [publicMember=" + publicMember
					+ ", protectedMember=" + protectedMember
					+ ", packageMember=" + packageMember + ", privateMember="
					+ privateMember + "]";
		}
		
		
	}
	
	public static class ComplexObject implements JSONSerializable {
		
		@JSONAttribute public String test;
		@JSONAttribute public PlainObjectWrapper innerObject;
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((innerObject == null) ? 0 : innerObject.hashCode());
			result = prime * result + ((test == null) ? 0 : test.hashCode());
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ComplexObject other = (ComplexObject) obj;
			if (innerObject == null) {
				if (other.innerObject != null)
					return false;
			} else if (!innerObject.equals(other.innerObject))
				return false;
			if (test == null) {
				if (other.test != null)
					return false;
			} else if (!test.equals(other.test))
				return false;
			return true;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ComplexObject [test=" + test + ", innerObject="
					+ innerObject + "]";
		}
		
	}

}
