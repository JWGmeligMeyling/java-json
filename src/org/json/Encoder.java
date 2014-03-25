package org.json;

/**
 * Copyright 2014 Jan-Willem Gmelig Meyling. Based on the Simple Framework
 * written by Niall Gallagher and the JSON specification described by
 * Douglas Crockford.
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONAttribute;

/**
 * The encoder is used to convert JSONSerializable classes to a JSON string
 * 
 * @author Jan-Willem Gmelig Meying
 */
public class Encoder {

	private static int DEFAULT_INDENT = 0;
	private static boolean IS_INDENT = false;
	
	/**
	 * Set the indentation
	 * @param amount
	 */
	public static void setIndent(int amount) {
		if(amount >= 0) {
			DEFAULT_INDENT = amount;
			IS_INDENT = amount != 0;
		}
	}
	
	/**
	 * Encode a JSONSerializable object to a JSON String
	 * @param obj JSONSerializable object to be serialized
	 * @return JSON String
	 * @throws JSONException
	 */
	public static String encode(JSONSerializable obj) throws JSONException {
		try {
			return encode(new StringWriter(), obj, 0).toString();
		} catch ( IOException e ) {
			// Unlikely to ever happen since we're using a StringWriter
			return "";
		}
	}
	
	/**
	 * Encode a JSONSErializable object and write it to an OutputStream
	 * @param io OutputStream
	 * @param obj JSONSerializable object to be serialized
	 * @throws JSONException
	 * @throws IOException
	 */
	public static void encode(OutputStream io, JSONSerializable obj)  throws JSONException, IOException {
		encode(new OutputStreamWriter(io), obj, 0);
	}

	/**
	 * 
	 * @param w
	 * @param obj
	 * @param indent
	 * @return
	 * @throws IOException
	 */
	private static Writer encode(Writer w, JSONSerializable obj, int indent) throws JSONException, IOException {
		w.append('{');
		int newIndent = (IS_INDENT) ? indent + DEFAULT_INDENT : 0;
		boolean separate = false;
		// For every field in the class
		for (Field field : obj.getClass().getDeclaredFields()) {
			// Field should have the JSONAttribute annotation
			if (field.isAnnotationPresent(JSONAttribute.class)) {
				// Separate keys with commas
				if (separate) w.append(',');
				separate = true;
				// Set accessibility
				field.setAccessible(true);
				// Fetch the name from the annotation or inherit from the field name
				JSONAttribute annotation = field.getAnnotation(JSONAttribute.class);
				String name = annotation.name();
				if (name.equals("")) name = field.getName();
				// Append the field name to the JSON String
				if(IS_INDENT) {
					w.append('\n');
					indent(w, newIndent);
				}
				writeString(w, field.getName());
				if(IS_INDENT) w.append(' ');
				w.append(':');
				if(IS_INDENT) w.append(' ');
				// Fetch the field type
				Class<?> type = field.getType();
				try {
					Object value = field.get(obj);
					writeObject(w, type, value, indent);
				} catch ( IllegalAccessException e ) {
					throw new JSONException(e);
				}
			}
		}
		if(IS_INDENT) {
			w.append('\n');
			indent(w, indent);
		}
		w.append('}');
		return w;
	}

	private static void writeObject(Writer w, Class<?> type, Object value, int indent) throws IOException {
		int newIndent = (IS_INDENT) ? indent + DEFAULT_INDENT : 0;
		if (value == null) {
			// null values appear as null in the JSON String
			w.append("null");
		} else if (JSONSerializable.class.isAssignableFrom(type)) {
			// values of type JSONSerializable should be encoded recursively
			encode(w, (JSONSerializable) value, newIndent);
		} else if (type.equals(String.class)) {
			writeString(w, (String) value);
		} else if (Map.class.isAssignableFrom(type)) {
			// Maps are converted into a JSON object: { "key" : " value", ... }
			Map<?, ?> map = (Map<?, ?>) value;
			w.append('{');
			boolean separate = false;
			for (Entry<?, ?> entry : map.entrySet()) {
				// Commas between key/value pairs
				if(separate) w.append(',');
				separate = true;
				// New line and indentation before the keys
				if(IS_INDENT) {
					w.append('\n');
					indent(w, newIndent + DEFAULT_INDENT);
				}
				writeString(w, (String) entry.getKey());
				if(IS_INDENT) w.append(' ');
				w.append(':');
				if(IS_INDENT) w.append(' ');
				writeObject(w, entry.getValue().getClass(), entry.getValue(), newIndent);
			}
			if(IS_INDENT) {
				w.append('\n');
				indent(w, newIndent);
			}
			w.append('}');
		} else if (Collection.class.isAssignableFrom(type)) {
			// Collections are converted into a JSON Array: [ value ]
			Collection<?> list = (Collection<?>) value;
			w.append('[');
			boolean separate = false;
			for (Object o : list) {
				// Commas between values
				if(separate) w.append(',');
				if(IS_INDENT) w.append(' ');
				separate = true;
				// Write the value
				writeObject(w, o.getClass(), o, newIndent);
			}
			w.append(']');
		} else if (type.isPrimitive() || Number.class.isAssignableFrom(type)) {
			// Numbers and other literals are put directly: 5, true, 2.3
			w.append(value.toString()); // Append literal
		} else {
			// Objects that do not implement JSONSerializable cannot be serialized
			throw new JSONException(type + " could not be serialized!");
		}
	}

	/**
	 * Write an escaped String between quotes to the Writer
	 * @param w Writer instance
	 * @param s String to be escaped
	 * @throws IOException
	 */
	private static void writeString(Writer w, String s) throws IOException {
		if (s == null) {
			w.append("null");
		} else {
			w.append('"');
			char c;
			for (int i = 0, l = s.length(); i < l; i++) {
				switch (c = s.charAt(i)) {
					case '\\':
					case '"':
					case '\b':
					case '\t':
					case '\n':
					case '\f':
					case '\r':
						// Escape character
						w.append('\\').append(c);
						break;
					default:
						if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
								|| (c >= '\u2000' && c < '\u2100')) {
							w.write(String.format("\\u%04X", c));
						} else {
							w.write(c);
						}
				}
			}
			w.append('"');
		}
	}

	/**
	 * Append the given amount of spaces to the {@code Writer}
	 * @param w {@code Writer} instance
	 * @param amount Amount of spaces
	 * @throws IOException
	 */
	private static void indent(Writer w, int amount) throws IOException {
		for (int i = 0; i < amount; i++)
			w.append(' ');
	}

}
