# kuri
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.ade.kuri/kuri-processor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.ade.kuri/kuri-processor)

## Kuri - Kotlin Multiplatform URI Tools.

Generates compile-time safe (escaped) URI paths from functions in Kotlin using KSP.
Only path and query parameters are supported for now.

### Example
```kotlin
@UriProvider
interface MyTestPaths {
    @UriTemplate("example/{parameter}")
    fun myUri(parameter: String, @Query extra: String): String
}

fun main() {
    println(KuriMyTestPaths.myUri("Hello World", "test"))
    // Output: example/Hello%20World?extra=test
}
```

### Usage
1. Add dependency to kuri's api and ksp processor (and ksp plugin - see instructions elsewhere)
   ```
   implementation("se.ade.kuri.api:kuri-api:0.1.2")
   ksp("se.ade.kuri.processor:kuri-processor:0.1.2")
   ```
2. Create an interface annotated with `@UriProvider`, and define functions annotated with `@UriTemplate`, where placeholders match function arguments:
    ```kotlin
    @UriProvider
    interface MyTestUris {
        @UriTemplate("path/{parameter}/example")
        fun myUri(parameter: String): String
    }
    ```
    Generates an object
    ```kotlin
    object KuriMyTestUris: MyTestUris {
        fun myUri(parameter: String): String = "... generated code ..." 
    }
    ```
3. Use the generated object:
    ```kotlin
    val uriString = KuriMyTestUris.myUri("test")
    ```
which will allow you to construct safe URIs with the supplied parameters.

## Modifiers to function parameters
- @Query: Adds a query parameter to the URI. Can be nullable. When null, it is omitted.
    ```
    @UriTemplate("path/{parameter}/example")
    fun myUri(parameter: String, @Query foo: String?): String
    ```
  Lists are supported, and will be output as multiple entries of the same key, e.g. `?foo=bar&foo=baz`.
    ```
    @UriTemplate("path/{parameter}/example")
    fun myUri(parameter: String, @Query foo: List<String>): String
    ```
- @Unescaped: For pre-escaped strings or endpoints expecting non-escaped input. Does not escape the parameter. Use with caution as reserved symbols may break the structure.
    ```
    @UriTemplate("api/{path}/example")
    fun myUri(@Unescaped path: String): String
    ```