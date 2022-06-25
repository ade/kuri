package se.ade.kuri.client

import se.ade.kuri.Uri
import se.ade.kuri.UriProvider

fun main() {
    val impl: ExamplePaths = KuriExamplePaths()
    println("example1: " + impl.example1("My 100% /unsafe/ value!"))
    println("example2: " + impl.example2("token1!", "second token"))
    println("intExample1: " + impl.intExample1(123,456))
}

@UriProvider
interface ExamplePaths {
    @Uri("start/{token}/end")
    fun example1(token: String): String

    @Uri(template = "start/{token1}/middle/{token2}/end")
    fun example2(token1: String, token2: String): String

    @Uri("group/{group}/item/{item}")
    fun intExample1(group: Int, item: Int): String
}

interface Decoy {
    fun foo(): Int
}