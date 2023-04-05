
Todo


Ideas
- Annotation @BoolTrueFalue to make a boolean parameter serialize out to "true"/"false" instead of 1/0
- Interface: UriParam: can provide the String to use as path parameter. Codegen will need to check if parameter is of that type and use its method to get the param value, instead of doing toString()
- Annotation @QueryComponent. Only one allowed per function. Sets the whole query component, in cases where key/value pairs don't work.
- Annotation parameter: @Query(class) takes a class as parameter which should be an object with one specific function that takes a parameter of type X (or Any?) and can serialize it to an (escaped) query string 