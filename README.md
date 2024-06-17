# kuri
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.ade.kuri/kuri-processor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.ade.kuri/kuri-processor)

Kuri - Kotlin Multiplatform URI Tools.

Code-generates safe URIs from type-safe arguments in Kotlin using KSP.

Currently in an experimental/exploratory phase.

Usage
1. add dependency to kuri's api and ksp processor (and ksp plugin - see instructions elsewhere)
   ```
   implementation("se.ade.kuri.api:kuri-api:0.0.7")
   ksp("se.ade.kuri.processor:kuri-processor:0.0.7")
   ```
2. specify interface:
    ```
    @UriProvider
    interface MyTestUris {
        @UriTemplate("path/{parameter}/example")
        fun myUri(parameter: String): String
    }
    ```
    Generates an object
    ```
    object KuriMyTestUris: MyTestUris {
        fun myUri(parameter: String): String = { ... generated code ... } 
    }
    ```
which will allow you to construct safe URIs with the supplied parameters.

## Modifiers to function parameters
- @Query: Adds a query parameter to the URI. When null, it is omitted.
    ```
    @UriTemplate("path/{parameter}/example")
    fun myUri(parameter: String, @Query foo: String?): String
    ```
- @Unescaped: Does not escape the parameter. Use with caution.
    ```
    @UriTemplate("api/{path}/example")
    fun myUri(@Unescaped path: String): String
    ```