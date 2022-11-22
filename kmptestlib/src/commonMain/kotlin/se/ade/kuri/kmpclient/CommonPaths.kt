package se.ade.kuri.kmpclient
import se.ade.kuri.UriTemplate
import se.ade.kuri.UriProvider

@UriProvider
interface CommonPaths {
    @UriTemplate("api/{param}/resource")
    fun helloWorld(param: String): String
}