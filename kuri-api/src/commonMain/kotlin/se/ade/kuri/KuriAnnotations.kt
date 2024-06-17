package se.ade.kuri

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class UriProvider()

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class UriTemplate(val template: String)

/**
 * Create a Query parameter in the key/value format, e.g. url?key=value
 * The key will be named equally to the parameter. A different name can optionally be set using the name property.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Query(val name: String = "")

/**
 * Keeps a value as-is, without url-encoding it, when used in a URL-parameter
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Unescaped