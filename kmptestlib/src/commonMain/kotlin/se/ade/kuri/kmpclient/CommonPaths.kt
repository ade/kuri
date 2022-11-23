package se.ade.kuri.kmpclient
import se.ade.kuri.UriTemplate
import se.ade.kuri.UriProvider

@UriProvider
internal interface CommonPaths {
    @UriTemplate("api/{param}/resource")
    fun helloWorld(param: String): String

    @UriTemplate("root/{something}/path")
    fun test(something: Int): String
}