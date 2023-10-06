package se.ade.kuri

import kotlin.reflect.KClass

interface KuriTemplateSpec {
    public fun getTokens(): List<KuriTokenSpec>
    public fun getTemplate(): String
}

data class KuriTokenSpec(
    val name: String,
    val type: KClass<*>,
    val query: Boolean,
    val optional: Boolean
) {
    override fun toString() = name
}

object KuriMyThingSpec {
    object MyFirstTemplate {
        val param1 = KuriTokenSpec(name = "param1", type = String::class, query = false, optional = false)
        fun getTokens(): List<KuriTokenSpec> = listOf(param1)
        fun getTemplate() = "abc/def"
        override fun toString() = getTemplate()
    }
}

