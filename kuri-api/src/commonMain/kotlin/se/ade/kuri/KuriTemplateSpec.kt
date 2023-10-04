package se.ade.kuri

import kotlin.reflect.KClass

data class KuriTemplateSpec(
    /** The raw (unprocessed) template string with placeholders */
    val template: String,
    /** Parameter (token) names with corresponding type */
    val tokens: Map<String, KClass<*>> = mapOf()
)