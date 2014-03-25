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

import java.io.Serializable;

/**
 * A {@code JSONException} is thrown when an exception occures during parsing a
 * JSON string or serialization.
 * 
 * @author Jan-Willem Gmelig Meyling
 */
public class JSONException extends RuntimeException implements Serializable, JSONSerializable {

	private static final long serialVersionUID = -5453235887713621500L;
	
	@JSONAttribute private final String message;
	
	/**
	 * Construct a new {@code JSONException}
	 * @param s message
	 */
	JSONException(String s) {
		super(s);
		this.message = s;
	}
	
	/**
	 * Construct a new {@code JSONException}
	 * @param e
	 */
	JSONException(Throwable e) {
		super(e);
		this.message = e.getMessage();
	}
	
	/**
	 * Construct a new {@code JSONException}
	 * @param s
	 * @param e
	 */
	JSONException(String s, Throwable e) {
		super(s,e);
		this.message = s;
	}
}
