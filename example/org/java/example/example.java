package org.java.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
		InputStream in = new FileInputStream("example/org/java/example/example.data.json");
		System.out.println(Decoder.decode(User.class, in));
	}
	
}
