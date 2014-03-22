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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A decoder is used for JSON deserialization. A subclass of type
 * {@code JSONSerializable} should be provided as entry point.
 * 
 * @author Jan-Willem Gmelig Meyling
 */
public final class Decoder<T extends JSONSerializable> {

	private final static String NULL = "null";
	private final static String EMPTY_STRING = "";
	private final Class<T> klass;
	
	private int depth = 0;
	private boolean isKey = true;
	private boolean isArray = false;
	
	private final Reader reader;
	private int current = 0;
	private int previous = 0;
	private int next = 0;
	
	private final Map<String, String> pairs = new HashMap<String, String>();
	private final List<String> arrayContents = new ArrayList<String>();
	
	/**
	 * Construct a new Decoder, that should return an object of Type {@code T}.
	 * @param klass Type of Object that should be created
	 * @param reader the Reader
	 */
	private Decoder(Class<T> klass, Reader reader) {
		this.klass = klass;
		this.reader = reader;
		try {
			// Read the first character
			this.next = reader.read();
		} catch (IOException e) {
			throw new ParseException(e);
		}
		parse();
	}
	
	/**
	 * Construct a new Decoder, that should return an object of Type T
	 * @param klass Type of Object that should be created
	 * @param input String that should be decoded
	 */
	private Decoder(Class<T> klass, String input) {
		this(klass, new StringReader(input));
	}
	
	/**
	 * Construct a new Decoder, that should return an object of Type T
	 * @param klass Type of Object that should be created
	 * @param io InputStream
	 */
	private Decoder(Class<T> klass, InputStream io) {
		this(klass, new InputStreamReader(io));
	}
	
	/**
	 * This method parses the first level of the JSON String, and puts it's
	 * values in the pairs Map - for objects - or the contents List - for
	 * arrays. Then, the object can be instantiated later on with the
	 * {@link #decode()} method.
	 * 
	 * @see {@link #decode()}
	 */
	private void parse() {
		String key = null;
		StringBuilder sb = new StringBuilder();
		
		LOOP : while(this.hasNext()) {
			char c = next();
			SWITCH : switch(c) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				/*
				 * Spaces can be ignored at this point. Spaces within quotes are
				 * handled there.
				 */
				continue LOOP;
			case ':':
				/*
				 * Colons separate keys from values in an JSON Object. When such
				 * a comma is found, break the switch, and set the current
				 * builder value as key, and continue to look for its value.
				 * When already expecting a value, an exception is thrown. When
				 * we're in a JSONArray, an exception is thrown as well; values
				 * in a JSON Array should be separated by commas.
				 */
				if(isArray || !isKey) throw new ParseException("Unexpected value");
				break SWITCH;
			case ',': 
				/*
				 * Comma's separate values in an array and key value pairs in
				 * objects. Check if we're in array mode, and if we're at the
				 * current depth, if so, append the last value to the values
				 * list, and clear the StringBuilder for the next value. If not,
				 * break out of the switch, to complete a key or value.
				 */
				if(depth == 1 && isArray) {
					arrayContents.add(sb.toString());
					sb.setLength(0);
					continue LOOP;
				}
				else break SWITCH;
			case '"':
			case '\'':
				/*
				 * Quoted string, find the next quote, and append the substring 
				 */
				sb.append(c);
				while(hasNext()) {
					char next = next();
					sb.append(next);
					if(next == c && previous() != '\\') {
						continue LOOP;
					}
				}
				throw new ParseException("Unexpected end of input");
			case '[':
				/*
				 * This bracket marks the beginning of an array. If we're at
				 * initial depth, the object we're parsing at the current level
				 * is an array - switch to array mode. If not, the array should
				 * be a value instead. Continue to the first matching closing
				 * bracket and append all characters in between - these are
				 * parsed recursively while instantiating this object.
				 */
				depth++;
				if(depth == 1) {
					isArray = true;
					continue LOOP;
				}
				if(isKey) throw new ParseException("Expected key a key, but got a value instead");
				sb.append(c);
				while(hasNext()) {
					char next = next();
					sb.append(next);
					if(next == '[') {
						depth++;
					} else if (next == ']') {
						depth--;
						if(depth == 1) {
							continue LOOP;
						}
					}
				}
				throw new ParseException("Malformed input");
			case '{':
				/*
				 * This bracket marks the beginning of an object. If we're at
				 * initial depth, the object we're parsing at the current level
				 * is an object - stay in object mode. If we're not at initial
				 * depth, this object should be a value. Skip to the next
				 * matching closing bracket, and append all characters in
				 * between to the StringBuilder. These are parsed recursively
				 * while instantiating this object.
				 */
				if(isKey && !isArray && depth != 0) throw new ParseException("Expected key a key, but got a value instead");
				depth++;
				if(depth > 1 ) {
					sb.append(c);
					while(hasNext()) {
						char next = next();
						sb.append(next);
						if(next == '{') {
							depth++;
						} else if (next == '}') {
							depth--;
							if(depth == 1) { // At original level, continue upper loop
								continue LOOP;
							}
						}
					}
					throw new ParseException("Malformed input");
				}
				continue LOOP;
			case '}':
				/*
				 * This should be the end of the  JSON string
				 */
				if(hasNext())
					throw new ParseException("Unexpected end of input");
				break SWITCH;
			case ']':
				/*
				 * This should be the end of the Array
				 */
				if(depth == 1) {
					arrayContents.add(sb.toString());
					continue LOOP;
				}
				break SWITCH;
			default:
				// All characters should be appended to the value StringBuilder
				sb.append(c);
				continue LOOP;
			}
			// If we've got here, a key or value is complete
			if(isKey) {
				key = StringValueOf(sb.toString());
			} else {
				pairs.put(key, sb.toString());
			}
			isKey = !isKey;
			// Empty the StringBuilder
			sb.setLength(0);
		}
	}
	
	private boolean hasNext() {
		return next != -1;
	}
	
	private char next() {
		previous = current;
		current = next;
		try {
			next = reader.read();
		} catch ( IOException e ) {
			throw new ParseException(e);
		}
		return (char) current;
	}
	
	private char previous() {
		return (char) previous;
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
				Class<?>[] parameterClasses = c.getParameterTypes();
				Type[] parameterTypes = c.getGenericParameterTypes();
				Annotation[][] annotations = c.getParameterAnnotations();
				int l = parameterClasses.length;
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
							if(name.equals(EMPTY_STRING)) throw new ParseException("Attribute name should be specified for constructor parameters");
							// Check if this field can be filled based on the input
							String strvalue = pairs.get(name);
							// Skip to the next constructor if no value could be found
							if(strvalue == null && annotation.required()) continue CTORS;
							arguments[i] = strToValue(parameterClasses[i], parameterTypes[i], strvalue);
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
					if(name.equals(EMPTY_STRING)) name = f.getName();
					// Fetch the value, if no value is available and the field is required, throw an exception
					String strvalue = pairs.get(name);
					if(strvalue == null ) {
						if(annotation.required())
							throw new ParseException("Field " + name + " was required but undefined in input string");
					} else {
						f.setAccessible(true);
						f.set(obj, strToValue(f.getType(), f.getGenericType(), strvalue));
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
	 * Convert a JSON String value to an Object
	 * @param klass
	 * @param type
	 * @param strvalue
	 * @return An Object of given type for the parsed String
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <V> V strToValue(Class<V> klass, Type type, String strvalue) throws InstantiationException, IllegalAccessException {
		if(strvalue == null) {
			return null;
		} else if(klass.equals(String.class)) {
			return (V) StringValueOf(strvalue);
		} else if (klass.equals(Boolean.class) || klass.equals(boolean.class)) {
			return (V) BooleanValueOf(strvalue);
		} else if (klass.equals(Byte.class) || klass.equals(byte.class)) {
			return (V) ByteValueOf(strvalue);
		} else if ( klass.equals(Short.class) || klass.equals(short.class)) {
			return (V) ShortValueOf(strvalue);
		} else if (klass.equals(Integer.class) || klass.equals(int.class)) {
			return (V) IntegerValueOf(strvalue);
		} else if (klass.equals(Long.class) || klass.equals(long.class)) {
			return (V) LongValueOf(strvalue);
		} else if (klass.equals(Float.class) || klass.equals(float.class)) {
			return (V) FloatValueOf(strvalue);
		} else if (klass.equals(Double.class) || klass.equals(double.class)) {
			return (V) DoubleValueOf(strvalue);
		} else if( JSONSerializable.class.isAssignableFrom(klass)) {
			return (V) decode((Class<? extends JSONSerializable>) klass, strvalue);
		} else if ( Collection.class.isAssignableFrom(klass)) {
			Class<?> valueClass = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
			return (V) getArray((Class<? extends Collection>) klass, valueClass, strvalue);
		} else if ( Map.class.isAssignableFrom(klass)) {
			Class<?> valueClass = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[1];
			return (V) getMap((Class<? extends Map>) klass, valueClass, strvalue);
		} else {
			throw new ParseException(klass.getCanonicalName() + " is not serializable");
		}
	}
	
	/**
	 * Instantiate a new {@code Collection} based on the given implementation and generic type
	 * valueClass, and fill it with values parsed from the input string. 
	 * @param klass {@code Class} for the {@code Collection} implementation
	 * @param valueClass {@code Class} for the values in the collection
	 * @param input JSONString containing the array and it's values
	 * @return the newly instantiated Collection
	 * @throws InstantiationException If the collection could not be instantiated
	 * @throws IllegalAccessException If no elements can be added to the collection
	 */
	private static <T extends Collection<V>, V> T getArray(Class<T> klass, Class<V> valueClass, String input) throws InstantiationException, IllegalAccessException {
		@SuppressWarnings("unchecked") T instance = (klass.isInterface()) ? (T) new ArrayList<V>(): klass.newInstance();
		Decoder<JSONSerializable> decoder = new Decoder<JSONSerializable>(JSONSerializable.class, input);
		for(String strvalue : decoder.arrayContents ) {
			instance.add(strToValue(valueClass, null, strvalue));
		}
		return instance;
	}
	
	/**
	 * Instantiate a new {@code Map} based on the given implementation.
	 * @param klass Implementation for the map
	 * @param valueClass Implementation for the values
	 * @param input JSONString containing the map and it's key value pairs
	 * @return the newly instantiated Map
	 * @throws InstantiationException If the map could not be instantiated
	 * @throws IllegalAccessException If no key value pairs could be added
	 */
	private static <T extends Map<String, V>, V> T getMap(Class<T> klass, Class<V> valueClass, String input) throws InstantiationException, IllegalAccessException {
		@SuppressWarnings("unchecked") T instance = (klass.isInterface()) ? (T) new HashMap<String, V>() : klass.newInstance();
		Decoder<JSONSerializable> decoder = new Decoder<JSONSerializable>(JSONSerializable.class, input);
		for( Entry<String, String> entry : decoder.pairs.entrySet() ) {
			instance.put(entry.getKey(), strToValue(valueClass, null, entry.getValue()));
		}
		return instance;
	}

	/**
	 * Get an escaped String ValueOf from input
	 * @param s
	 * @return the String value of escaped String value of the characters
	 *         between quotes
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

	/**
	 * @param s JSON fragment
	 * @return Boolean value of a JSON fragment
	 */
	public static Boolean BooleanValueOf(String s) {
		if(s.equalsIgnoreCase(NULL)) return null;
		return Boolean.valueOf(s);
	}

	/**
	 * @param s JSON fragment
	 * @return Byte value of a JSON fragment
	 */
	public static Byte ByteValueOf(String s) {
		if(s.equalsIgnoreCase(NULL)) return null;
		return Byte.valueOf(s);
	}

	/**
	 * @param s JSON fragment
	 * @return Short value of a JSON fragment
	 */
	public static Short ShortValueOf(String s) {
		if(s.equalsIgnoreCase(NULL)) return null;
		return Short.valueOf(s);
	}

	/**
	 * @param s JSON fragment
	 * @return Integer value of a JSON fragment
	 */
	public static Integer IntegerValueOf(String s) {
		if(s.equalsIgnoreCase(NULL)) return null;
		return Integer.valueOf(s);
	}

	/**
	 * @param s JSON fragment
	 * @return Long value of a JSON fragment
	 */
	public static Long LongValueOf(String s) {
		if(s.equalsIgnoreCase(NULL)) return null;
		return Long.valueOf(s);
	}

	/**
	 * @param s JSON fragment
	 * @return Float value of a JSON fragment
	 */
	public static Float FloatValueOf(String s) {
		if(s.equalsIgnoreCase(NULL)) return null;
		return Float.valueOf(s);
	}

	/**
	 * @param s JSON fragment
	 * @return Double value of a JSON fragment
	 */
	public static Double DoubleValueOf(String s) {
		if(s.equalsIgnoreCase(NULL)) return null;
		return Double.valueOf(s);
	}	


	/**
	 * A ParseException is thrown when an exception occurred during JSON parsing
	 * 
	 * @author Jan-Willem Gmelig Meyling
	 */
	public static class ParseException extends RuntimeException {
		
		private static final long serialVersionUID = -5453235887713621500L;

		ParseException(Exception e) {
			super("JSON String could not be parsed", e);
		}

		ParseException(String string) {
			super(string);
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
	
	/**
	 * Decode a JSON string
	 * @param entrypoint the main wrapper class
	 * @param io InputStream 
	 * @return deserialized instance of class
	 * @throws ParseException
	 */
	public static <T extends JSONSerializable> T decode(Class<T> entrypoint, InputStream io) throws ParseException {
		return new Decoder<T>(entrypoint, io).decode();
	}
}
