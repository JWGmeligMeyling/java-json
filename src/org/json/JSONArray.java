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


/**
 * The {@code JSONArray} is used to (de)serialize Collections
 * 
 * @author Jan-Willem Gmelig Meyling
 */
public @interface JSONArray {
	
	/**
	 * This represents the name of the JSON attribute. Annotated fields can
	 * optionally provide the name of the element. If no name is provided then
	 * the name of the annotated field or method will be used in its place.
	 * 
	 * @return the name of the attribute this value represents
	 */
	String name() default "";

	/**
	 * Determines whether the element is required within the JSON document. When
	 * this value is undefined in the document, the object cannot be
	 * deserialized.
	 * 
	 * @return true if the element is required, false otherwise
	 */
	boolean required() default false;

	/**
	 * This represents an explicit type that should be used for the annotated
	 * field or method.
	 * 
	 * @return the explicit type to use for this
	 */
	Class<?> type() default void.class;
	
}
