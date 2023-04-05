package se.ade.kuri.kmpclient.codegenvalidation

import se.ade.kuri.Query
import se.ade.kuri.UriProvider
import se.ade.kuri.UriTemplate

@UriProvider
interface TestPathsProvider {
    @UriTemplate("{parameter}/end")
    fun paramAtStart(parameter: String): String
    @UriTemplate("start/{parameter}/end")
    fun paramInMiddle(parameter: String): String
    @UriTemplate("start/{parameter}")
    fun paramAtEnd(parameter: String): String

    @UriTemplate("number/{number}/data")
    fun numberParamInt(number: Int): String
    @UriTemplate("number/{number}/data")
    fun numberParamLong(number: Long): String

    @UriTemplate("path/{something}/data")
    fun pathParamAny(something: Any): String

    @UriTemplate("somepath/static")
    fun queryParamOnly(@Query foobar: String): String

    @UriTemplate("somepath/static")
    fun queryParamOnlyNamed(@Query("namedParam") foobar: String): String

    @UriTemplate("somepath/static")
    fun queryParamOnlyNamedNullable(@Query("namedParam") foobar: String?): String

    @UriTemplate("somepath/static")
    fun queryParamOnlyNullable(@Query foobar: String?): String

    @UriTemplate("somepath/static")
    fun queryParamOnlyMultiNullable(@Query foo: String?, @Query bar: String?): String

    @UriTemplate("somepath/static")
    fun queryParamOnlyMixedNullability(@Query foo: String, @Query bar: String?): String

    @UriTemplate("somepath/static")
    fun queryParamBoolean(@Query foo: Boolean): String

    @UriTemplate("somepath/static")
    fun queryParamInt(@Query foo: Int): String

    @UriTemplate("somepath/{number}/static")
    fun pathParamInt(number: Int): String

    @UriTemplate("somepath/static")
    fun queryParamAny(@Query foo: Any): String
}