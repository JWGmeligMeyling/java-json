package org.json;

import java.lang.reflect.Field;

import org.json.JSONAttribute;

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
					if(value == null ) {
						sb.append("null");
					} else if(JSONSerializable.class.isAssignableFrom(type)) {
						sb.append(encode((JSONSerializable) value)); // Stringify recursively
					} else if ( type.equals(String.class)) {
						sb.append('"').append((String) value).append('"'); // Quote string
						// TODO Escape String
					} else {
						// TODO Handle non-primitives/non-numbers
						sb.append(value.toString()); // Append literal
					}
				}
				if(++i<l) sb.append(','); // Separate keys with commas
			}
			sb.append('}');
			return sb.toString();
		} catch (Exception e) {
			throw new JSONException(o.toString() + " could not be stringified",	e);
		}
	}
	
	public static class JSONException extends RuntimeException {
		
		private static final long serialVersionUID = -5254654328847588624L;

		JSONException(String input, Throwable cause) {
			super(input, cause);
		}
	}
	
}
