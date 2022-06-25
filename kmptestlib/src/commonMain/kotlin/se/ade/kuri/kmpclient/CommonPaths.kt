package se.ade.kuri.kmpclient
import se.ade.kuri.Uri
import se.ade.kuri.UriProvider

@UriProvider
interface CommonPaths {
    @Uri("hello/{param}/world")
    fun helloWorld(param: String): String
}