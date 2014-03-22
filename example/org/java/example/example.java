package org.java.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.json.Decoder;
import org.json.Decoder.ParseException;
import org.json.JSONAttribute;
import org.json.JSONSerializable;

public class example {
	
	public static final class Name implements JSONSerializable {
		@JSONAttribute(name="firstname") private final String firstName;
		@JSONAttribute(name="lastname") private final String lastName;

		public Name(@JSONAttribute(name="firstname", required=true) String firstName,
					@JSONAttribute(name="lastname", required=true) String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}
		
		public String toString() { 
			return firstName + " " + lastName;
		}
	}
	
	public static class User implements JSONSerializable {

		@JSONAttribute private final long id;
		@JSONAttribute private Name name;
		@JSONAttribute private Map<String, String> emailAdresses;
		
		public User(@JSONAttribute(name="id") long id) {
			this.id = id;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "User [name=" + name + ", id=" + id + ", emailAdresses="
					+ emailAdresses + "]";
		}
		
	}
	
	public static void main(String[] args) throws ParseException, IOException {
		System.out.println(Decoder.decode(User.class, readFile("example/org/java/example/example.data.json", Charset.defaultCharset())));
	}

	private static String readFile(String path, Charset encoding)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
}
