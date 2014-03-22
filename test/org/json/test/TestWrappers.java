package org.json.test;
import java.util.List;
import java.util.Map;

import org.json.JSONAttribute;
import org.json.JSONSerializable;

public class TestWrappers {

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
		
		public void setPrivateMember(String privateMember) {
			this.privateMember = privateMember;
		}
		
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
	
	public static class ObjectWithArray implements JSONSerializable {
		@JSONAttribute String stringValue;
		@JSONAttribute List<String> stringList;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((stringList == null) ? 0 : stringList.hashCode());
			result = prime * result
					+ ((stringValue == null) ? 0 : stringValue.hashCode());
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
			ObjectWithArray other = (ObjectWithArray) obj;
			if (stringList == null) {
				if (other.stringList != null)
					return false;
			} else if (!stringList.equals(other.stringList))
				return false;
			if (stringValue == null) {
				if (other.stringValue != null)
					return false;
			} else if (!stringValue.equals(other.stringValue))
				return false;
			return true;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ObjectWithArray [stringValue=" + stringValue
					+ ", stringList=" + stringList + "]";
		}
		
	}
	
	public static class ObjectWithFinalArray implements JSONSerializable {
		@JSONAttribute final List<String> stringList;
		
		public ObjectWithFinalArray(@JSONAttribute(name="stringList") List<String> list ) {
			this.stringList = list;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((stringList == null) ? 0 : stringList.hashCode());
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
			ObjectWithFinalArray other = (ObjectWithFinalArray) obj;
			if (stringList == null) {
				if (other.stringList != null)
					return false;
			} else if (!stringList.equals(other.stringList))
				return false;
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ObjectWithFinalArray [list=" + stringList + "]";
		}
		
	}

	public static class ObjectWithComplexArray implements JSONSerializable {
		@JSONAttribute String stringValue;
		@JSONAttribute List<PlainObjectWrapper> complexList;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((complexList == null) ? 0 : complexList.hashCode());
			result = prime * result
					+ ((stringValue == null) ? 0 : stringValue.hashCode());
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
			ObjectWithComplexArray other = (ObjectWithComplexArray) obj;
			if (complexList == null) {
				if (other.complexList != null)
					return false;
			} else if (!complexList.equals(other.complexList))
				return false;
			if (stringValue == null) {
				if (other.stringValue != null)
					return false;
			} else if (!stringValue.equals(other.stringValue))
				return false;
			return true;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ObjectWithComplexArray [stringValue=" + stringValue
					+ ", complexList=" + complexList + "]";
		}
		
	}
	
	public static class ObjectWithMap implements JSONSerializable {
		@JSONAttribute Map<String, String> myMap;

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((myMap == null) ? 0 : myMap.hashCode());
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
			ObjectWithMap other = (ObjectWithMap) obj;
			if (myMap == null) {
				if (other.myMap != null)
					return false;
			} else if (!myMap.equals(other.myMap))
				return false;
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ObjectWithMap [myMap=" + myMap + "]";
		}
		
	}
	
	public static class ObjectWithComplexMap implements JSONSerializable {
		@JSONAttribute Map<String, PlainObjectWrapper> complexObject;

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((complexObject == null) ? 0 : complexObject.hashCode());
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
			ObjectWithComplexMap other = (ObjectWithComplexMap) obj;
			if (complexObject == null) {
				if (other.complexObject != null)
					return false;
			} else if (!complexObject.equals(other.complexObject))
				return false;
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ObjectWithComplexMap [complexObject=" + complexObject + "]";
		}
		
	}
	
}
