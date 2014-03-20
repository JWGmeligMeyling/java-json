Java-JSON
=========
Java JSON is a JSON library that (de)serializes JSON strings based on annotated wrapper classes.
A JSON string can be parsed by creating a wrapper class. This wrapper class describes what attributes should
be fetched from the JSON string to instantiate an object of this class. This decoder takes both the JSON string
and the wrapper class as arguments, and returns the instantiated object.

Below you find a few examples how this library can be used. They are taken from the test cases in the repository.

### Plain objects
A plain object may contain attributes of the primitive types, String or Number objects. Take the following Java class.
```java
public class PlainObjectWrapper implements JSONSerializable {
	@JSONAttribute public String value;
	@JSONAttribute public int value1;
	@JSONAttribute public double value2;
	@JSONAttribute public boolean value3;
}
```
Instances of the class above can be serialized to and deserialized from the JSON fragment below:
```js
{
	value : "test",
	value1 : 2342342,
	value2 : 23.2342352353,
	value3: true
}
```
This JSON fragment can be decoded using `Decoder.decode(PlainObjectWrapper.class, inputString)`

### Different attribute names in JSON
When you want attribute names to differ from the field names in the class, they can be set as name for the annotation.
```java
public class POWWithRequirements implements JSONSerializable {
	@JSONAttribute(name="anotherName") public String value;
}
```

### Required attributes
Mark required attributes with the requirement boolean in the `JSONAttribute` annotation. When a required attribute cannot be
deserialized, an `ParseException` is thrown.
```java
public class POWWithRequirements implements JSONSerializable {
	@JSONAttribute(required=true) public String required;
	@JSONAttribute public String optional;
}
```

### Final attributes set in constructor
```java
public class FinalFields implements JSONSerializable {
	@JSONAttribute public final String finalField;
	@JSONAttribute public String additionalField;
		
	public FinalFields(@JSONAttribute(name="finalField") String finalField) {
		this.finalField = finalField;
	}
}
```

### Complex structures
```java
public static class ComplexObject implements JSONSerializable {
	@JSONAttribute public String test;
	@JSONAttribute public PlainObjectWrapper innerObject;
}
```
Objects of above class can be instantiated from, for example, from the following JSON fragment:
```js
{
	"test" : "ownAttribute",
	innerObject : {
		value : "test",
		value1 : 2342342,
		value2 : 23.2342352353,
		value3 : true
	}
}
```

## Todo's
* To JSONString encoder
* Support for Arrays (`@JSONArray` annotation)
* Support for HashMaps (`@JSONMap` annotation)