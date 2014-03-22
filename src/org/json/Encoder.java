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

	/**
	 * 
	 * @param o
	 * @return JSONString of object
	 * @throws JSONException
	 */
	public static String encode(JSONSerializable o) throws JSONException {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			Field[] declaredFields = o.getClass().getDeclaredFields();
			for (int i = 0, l = declaredFields.length; i < l; ) {
				Field field = declaredFields[i]; // Get field
				field.setAccessible(true); // Get accessibility
				if(field.isAnnotationPresent(JSONAttribute.class)) {
					JSONAttribute annotation = field.getAnnotation(JSONAttribute.class);
					String name = annotation.name(); // Fetch the name from the annotation
					if(name.equals("")) name = field.getName(); // Or inherit from the field name
					sb.append('"').append(field.getName()).append("\":");
					
					Class<?> type = field.getType(); // Fetch the field type
					Object value = field.get(o);
					encode(sb, type, value);
				}
				if(++i<l) sb.append(','); // Separate keys with commas
			}
			sb.append('}');
			return sb.toString();
		} catch (Exception e) {
			throw new JSONException(o.toString() + " could not be stringified",	e);
		}
	}
	
	private static void encode(StringBuilder sb, Class<?> type, Object value) {
		if(value == null ) {
			// null values appear as null in the JSON String
			sb.append("null");
		} else if(JSONSerializable.class.isAssignableFrom(type)) {
			// values of type JSONSerializable should be encoded recursively
			sb.append(encode((JSONSerializable) value));
		} else if ( type.equals(String.class)) {
			// Strings should be escaped and wrapped between quotes
			// TODO Escape String
			sb.append('"').append((String) value).append('"');
		} else if ( Map.class.isAssignableFrom(type)) {
			// Maps are converted into a JSON object: { key : " value", ... }
			Map<?, ?> map = (Map<?, ?>) value;
			sb.append('{');
			int i = 0, s = map.size();
			for( Entry<?, ?> entry : map.entrySet()) {
				encode(sb, String.class, entry.getKey());
				sb.append(':');
				encode(sb, entry.getValue().getClass(), entry.getValue());
				if(++i < s) sb.append(',');
			}
			sb.append('}');
		} else if ( Collection.class.isAssignableFrom(type)) {
			// Collections are converted into a JSON Array: [ value ]
			Collection<?> list = (Collection<?>) value;
			sb.append('[');
			int i = 0, s = list.size();
			for( Object o : list ) {
				encode(sb, o.getClass(), o);
				if(++i < s) sb.append(',');
			}
			sb.append(']');
		} else if (type.isPrimitive() || Number.class.isAssignableFrom(type)) {
			// Numbers and other literals are put directly: 5, true, 2.3
			sb.append(value.toString()); // Append literal
		} else {
			// Objects that do not implement JSON Serializable cannot be serialized
			throw new JSONException(type + " could not be serialized!");
		}
	}
	
}
