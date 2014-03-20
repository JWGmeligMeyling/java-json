package org.json;
/**
 * Copyright 2014 Jan-Willem Gmelig Meyling. Based on the Simple Framework
 * written by Niall Gallagher.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A decoder is used for JSON deserialization. A subclass of type
 * {@code JSONSerializable} should be provided as entry point.
 * 
 * @author Jan-Willem Gmelig Meyling
 */
public final class Decoder<T extends JSONSerializable> {

	private final Class<T> klass;
	private final int length;
	private final String s;
	
	private int index = 0;
	private int depth = 0;
	private boolean isKey = true;
	
	private final Map<String, String> pairs = new HashMap<String, String>();

	private Decoder(Class<T> klass, String s) {
		this.klass = klass;
		this.s = s;
		this.length = s.length();
		parse();
	}
	
	private void parse() {
		if (!this.hasNext() || this.next() != '{') {
            throw new ParseException("A JSONObject text must begin with '{'");
        }
		depth++;
		if(!this.hasNext()) {
			throw new ParseException("Unexpected end of input");
		}

		String key = null;
		StringBuilder sb = new StringBuilder();
		
		LOOP : while(this.hasNext()) {
			char c = next();
			SWITCH : switch(c) {
			case ' ': // Ignore spaces
				continue LOOP;
			case ':': // After a : follows a value, switch modes and finalize method
				if(!isKey) throw new ParseException("Unexpected value");
				break SWITCH;
			case ',': //  After a , follows a key, switch modes and finalize method
				if(isKey) throw new ParseException("Unexpected key");
				break SWITCH;
			case '"':
			case '\'':  // Quoted string, find the next quote, and append the substring
				sb.append(c);
				while(hasNext()) {
					char next = next();
					sb.append(next);
					if(next == c ) {
						continue LOOP;
					}
				}
				throw new ParseException("Unexpected end of input");
			case '{': // We're entering a new JSON Object
				if(isKey) throw new ParseException("Cannot start JSON Object as key");
				depth++;
				sb.append(c);
				while(hasNext()) {
					char next = next();
					sb.append(next);
					if(next == '{') {
						depth++;
					} else if ( next == '}' ) {
						depth--;
						if(depth == 1) { // At original level, continue upper loop
							continue LOOP;
						}
					}
				}
				throw new ParseException("Malformed input");
			case '}': // This should be the end of the  JSON string
				if(hasNext())
					throw new ParseException("Unexpected end of input");
				break SWITCH;
			default: // All characters should be appended to the value StringBuilder
				sb.append(c);
				continue LOOP;
			}
			// If we've got here, a key or value is complete
			if(isKey) {
				key = Conversion.StringValueOf(sb.toString());
			} else {
				pairs.put(key, sb.toString());
			}
			isKey = !isKey;
			// Empty the StringBuilder
			sb.setLength(0);
		}
	}
	
	private boolean hasNext() {
		return index < length;
	}
	
	private char next() {
		return s.charAt(index++);
	}
	
	@SuppressWarnings("unchecked")
	private static Object strToValue(Class<?> type, String strvalue) {
		if(strvalue == null) {
			return null;
		} else if(type.equals(String.class)) {
			return Conversion.StringValueOf(strvalue);
		} else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
			return Conversion.BooleanValueOf(strvalue);
		} else if (type.equals(Byte.class) || type.equals(byte.class)) {
			return Conversion.ByteValueOf(strvalue);
		} else if ( type.equals(Short.class) || type.equals(short.class)) {
			return Conversion.ShortValueOf(strvalue);
		} else if (type.equals(Integer.class) || type.equals(int.class)) {
			return Conversion.IntegerValueOf(strvalue);
		} else if (type.equals(Long.class) || type.equals(long.class)) {
			return Conversion.LongValueOf(strvalue);
		} else if (type.equals(Float.class) || type.equals(float.class)) {
			return Conversion.FloatValueOf(strvalue);
		} else if (type.equals(Double.class) || type.equals(double.class)) {
			return Conversion.DoubleValueOf(strvalue);
		} else if( JSONSerializable.class.isAssignableFrom(type)) {
			return decode((Class<? extends JSONSerializable>) type, strvalue);
		} else {
			throw new ParseException(type.getCanonicalName() + " is not serializable");
		}
	}

	private static class Conversion {

		private final static String NULL = "null";
		
		/**
		 * Get an escaped String ValueOf from input
		 * @param s
		 * @return
		 */
		public static String StringValueOf(String s) {
			if(s.equalsIgnoreCase(NULL))
				return null;
			int l = s.length();
			char start = 0;
			StringBuilder sb = new StringBuilder(l);
			CHARS : for(int i = 0; i < l; i++ ) {
				char c = s.charAt(i);
				switch(c) {
				case ' ':
					if(start == 0) {
						// Skip if begin of String, return if end
						if(sb.length() > 0) {
							break CHARS;
						}
					} else {
						// Append spaces only if wrapped between quotes
						sb.append(c);
					}
					break;
				case '"':
				case '\'':
					if(sb.length() == 0 ) {
						// This quote marks the start of the String,
						// set the start char to the current char
						start = c;
					} else if ( c == start ) {
						// If equal to the start char, the end of the String
						// is reached
						break CHARS;
					} else {
						// If not equal to the start char, append
						// it to the String
						sb.append(c);
					}
					break;
				case '\\':
					if(i < l) {
						// Append escaped character
						char next = s.charAt(++i);
						sb.append(next);
					} else {
						sb.append(c);
					}
					break;
				default:
					// Append the character
					sb.append(c);
					break;
				}
			}
			return sb.toString();
		}
		
		public static boolean booleanValueOf(String s) {
			return Boolean.parseBoolean(s);
		}
		
		public static Boolean BooleanValueOf(String s) {
			if(s.equalsIgnoreCase(NULL)) return null;
			return Boolean.valueOf(booleanValueOf(s));
		}
		
		public static byte byteValueOf(String s) {
			try {
				return Byte.parseByte(s);
			} catch ( NumberFormatException e ) {
				throw new ParseException(e);
			}
		}
		
		public static Byte ByteValueOf(String s) {
			if(s.equalsIgnoreCase(NULL)) return null;
			return Byte.valueOf(byteValueOf(s));
		}
		
		public static short shortValueOf(String s) {
			try {
				return Short.parseShort(s);
			} catch ( NumberFormatException e ) {
				throw new ParseException(e);
			}
		}
		
		public static Short ShortValueOf(String s) {
			if(s.equalsIgnoreCase(NULL)) return null;
			return Short.valueOf(shortValueOf(s));
		}
		
		public static int intValueOf(String s) {
			try {
				return Integer.parseInt(s);
			} catch ( NumberFormatException e ) {
				throw new ParseException(e);
			}
		}
		
		public static Integer IntegerValueOf(String s) {
			if(s.equalsIgnoreCase(NULL)) return null;
			return Integer.valueOf(intValueOf(s));
		}
		
		public static long longValueOf(String s) {
			try {
				return Long.parseLong(s);
			} catch ( NumberFormatException e ) {
				throw new ParseException(e);
			}
		}
		
		public static Long LongValueOf(String s) {
			if(s.equalsIgnoreCase(NULL)) return null;
			return Long.valueOf(longValueOf(s));
		}
		
		public static float floatValueOf(String s) {
			try {
				return Float.parseFloat(s);
			} catch ( NumberFormatException e ) {
				throw new ParseException(e);
			}
		}
		
		public static Float FloatValueOf(String s) {
			if(s.equalsIgnoreCase(NULL)) return null;
			return Float.valueOf(floatValueOf(s));
		}
		
		public static double doubleValueOf(String s) {
			try {
				return Double.parseDouble(s);
			} catch ( NumberFormatException e ) {
				throw new ParseException(e);
			}
		}
		
		public static Double DoubleValueOf(String s) {
			if(s.equalsIgnoreCase(NULL)) return null;
			return Double.valueOf(doubleValueOf(s));
		}
		
		// public static Date DateValueOf(String s) {
		//	if(s.equalsIgnoreCase(NULL)) return null;
		//	return new Date(longValueOf(s));
		//}
	}
	
	
	public static class ParseException extends RuntimeException {
		
		private static final long serialVersionUID = -5453235887713621500L;

		ParseException(Exception e) {
			super("JSON String could not be parsed", e);
		}

		ParseException(String string) {
			super(string);
		}
		
	}
	
	private T decode() throws ParseException {
		try {
			T obj = null;
			// Get the constructors for the wrapper
			@SuppressWarnings("unchecked") Constructor<T>[] constructors = (Constructor<T>[]) klass.getConstructors();
			// Sort the constructors, the one with the most variables first
			Arrays.sort(constructors, new Comparator<Constructor<?>>() {
				@Override
				public int compare(Constructor<?> o1, Constructor<?> o2) {
					return o2.getParameterTypes().length - o1.getParameterTypes().length;
				}
			});
			// Iterate through the constructors and look for a constructor that matches our data
			CTORS : for ( Constructor<T> c : constructors ) {
				Class<?>[] types = c.getParameterTypes();
				Annotation[][] annotations = c.getParameterAnnotations();
				int l = types.length;
				// Create an array that will contains the argument with which the constructor will be invoked
				Object[] arguments = new Object[l];
				// Iterate over the parameters
				PARAMS : for(int i = 0; i < l; i++ ) {
					String name = null;					
					// Fetch the annotations for the constructor parameters
					// We can only use CTORS with annotated parameters
					int m = annotations[i].length;
					if ( m == 0 ) continue CTORS;
					for(int j = 0; j < m; j++ ) {
						Annotation a = annotations[i][j];
						if(a instanceof JSONAttribute ) {
							JSONAttribute annotation = (JSONAttribute) a;
							// Because constructor parameter names are lost after compilation,
							// constructor parameters need to specify their attribute name in the annotation
							name = ((JSONAttribute) a).name();
							if(name.equals("")) throw new ParseException("Attribute name should be specified for constructor parameters");
							// Check if this field can be filled based on the input
							String strvalue = pairs.get(name);
							// Skip to the next constructor if no value could be found
							if(strvalue == null && annotation.required()) continue CTORS;
							arguments[i] = strToValue(types[i], strvalue);
							// The annotation is found, skip to the next parameter
							continue PARAMS;
						}
					}
					// If we get here the JSONAttribute was not set
					continue CTORS;
				}
				// If we get here, all required parameters could be filled
				c.setAccessible(true);
				obj = c.newInstance(arguments);
				break;
			}
			// Use the default constructor if no other constructor was available
			if(constructors.length == 0 ) obj = klass.newInstance();
			// If the object is still null, throw an exception
			if(obj == null) throw new ParseException(klass.getCanonicalName() + " could not be instantiated");
			
			for (Field f : klass.getDeclaredFields()) {
				// The field should have the JSONAttribute annotation and not have the final modifier
				if (f.isAnnotationPresent(JSONAttribute.class) && !Modifier.isFinal(f.getModifiers())) {
					JSONAttribute annotation = f.getAnnotation(JSONAttribute.class);
					// Fetch the attribute name at which the value can be found in the JSON input
					// If no value is defined in the annotation, use the field name in the class
					String name = annotation.name();
					if(name.equals("")) name = f.getName();
					// Fetch the value, if no value is available and the field is required, throw an exception
					String strvalue = pairs.get(name);
					if(strvalue == null ) {
						if(annotation.required())
							throw new ParseException("Field " + name + " was required but undefined in input string");
					} else {
						f.setAccessible(true);
						f.set(obj, strToValue(f.getType(), strvalue));
					}
				}
			}

			return obj;
		} catch ( ParseException e ) {
			throw e; // Forward parse exceptions
		} catch ( Exception e ) {
			throw new ParseException(e); // Wrap all other exceptions (reflection exceptions basically)
		}
	}
	
	/**
	 * Decode a JSON string
	 * @param entrypoint the main wrapper class
	 * @param input the JSON input string
	 * @return deserialized instance of class
	 * @throws ParseException
	 */
	public static <T extends JSONSerializable> T decode(Class<T> entrypoint, String input) throws ParseException {
		return new Decoder<T>(entrypoint, input).decode();
	}
}
