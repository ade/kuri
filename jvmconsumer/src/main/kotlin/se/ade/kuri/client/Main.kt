package se.ade.kuri.client

import se.ade.kuri.UriTemplate
import se.ade.kuri.UriProvider
import se.ade.kuri.kmpclient.KmpCommonPathsConsumer

fun main() {
    val impl: ExamplePaths = KuriExamplePaths()
    println("example1: " + impl.example1("My 100% /unsafe/ value!"))
    println("example2: " + impl.example2("token1!", "second token"))
    println("intExample1: " + impl.intExample1(123,456))

    println("KMP test result: ${KmpCommonPathsConsumer.test()}")
}

@UriProvider
interface ExamplePaths {
    @UriTemplate("start/{token}/end")
    fun example1(token: String): String

    @UriTemplate(template = "start/{token1}/middle/{token2}/end")
    fun example2(token1: String, token2: String): String

    @UriTemplate("group/{group}/item/{item}")
    fun intExample1(group: Int, item: Int): String
}

interface Decoy {
    fun foo(): Int
}