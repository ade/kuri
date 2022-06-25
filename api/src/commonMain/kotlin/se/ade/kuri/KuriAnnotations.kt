package se.ade.kuri

@Target(AnnotationTarget.CLASS)
annotation class UriProvider()

@Target(AnnotationTarget.FUNCTION)
annotation class Uri(val template: String)


