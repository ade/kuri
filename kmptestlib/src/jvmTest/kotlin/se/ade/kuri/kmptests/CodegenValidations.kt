package se.ade.kuri.kmptests

import io.kotest.matchers.shouldBe
import org.junit.Test
import se.ade.kuri.kmpclient.codegenvalidation.KuriTestPathsProvider
import se.ade.kuri.kmpclient.codegenvalidation.TestPathsProvider

class CodegenValidations {
    @Test
    fun `URI builder`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.paramAtStart("foo") shouldBe "foo/end"
        impl.paramAtEnd("foo") shouldBe "start/foo"
        impl.paramInMiddle("foo") shouldBe "start/foo/end"
    }

    @Test
    fun `URI builder - percent encoding`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.paramAtStart("foo bar") shouldBe "foo%20bar/end"
        impl.paramAtEnd("foo bar") shouldBe "start/foo%20bar"
        impl.paramInMiddle("foo bar") shouldBe "start/foo%20bar/end"
    }

    @Test
    fun `URI builder - percent encoding - extended`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.paramAtStart("{foo bar?}") shouldBe "%7Bfoo%20bar%3F%7D/end"
        impl.paramAtEnd("{foobar?}") shouldBe "start/%7Bfoobar%3F%7D"
        impl.paramInMiddle("{foo bar/}") shouldBe "start/%7Bfoo%20bar%2F%7D/end"
    }

    @Test
    fun `URI builder - numbers`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.numberParamInt(0) shouldBe "number/0/data"
        impl.numberParamInt(42) shouldBe "number/42/data"
        impl.numberParamInt(-42) shouldBe "number/-42/data"
        impl.numberParamInt(Int.MIN_VALUE) shouldBe "number/-2147483648/data"
        impl.numberParamInt(Int.MAX_VALUE) shouldBe "number/2147483647/data"

        impl.numberParamLong(0) shouldBe "number/0/data"
        impl.numberParamLong(42L) shouldBe "number/42/data"
        impl.numberParamLong(-42L) shouldBe "number/-42/data"
        impl.numberParamLong(Long.MIN_VALUE) shouldBe "number/-9223372036854775808/data"
        impl.numberParamLong(Long.MAX_VALUE) shouldBe "number/9223372036854775807/data"
    }

    @Test
    fun `URI builder - any`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.anyParam(DoublingToString(2)) shouldBe "path/4/data"
    }

    @Test
    fun `URI builder - query`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.queryParamOnly("myvalue") shouldBe "somepath/static?foobar=myvalue"
        impl.queryParamOnly("value with spaces") shouldBe "somepath/static?foobar=value%20with%20spaces"
        impl.queryParamOnly("strange chars: {} % #") shouldBe "somepath/static?foobar=strange%20chars%3A%20%7B%7D%20%25%20%23"
        impl.queryParamOnly("strange chars: / ? /") shouldBe "somepath/static?foobar=strange%20chars%3A%20/%20?%20/"
        impl.queryParamOnly("https://example.com/why/examples/are/great?id=42") shouldBe "somepath/static?foobar=https%3A//example.com/why/examples/are/great?id%3D42"
    }

    @Test
    fun `URI builder - query - named`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.queryParamOnlyNamed("myvalue") shouldBe "somepath/static?namedParam=myvalue"
    }

    @Test
    fun `URI builder - query - named - nullable`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.queryParamOnlyNamedNullable("myvalue") shouldBe "somepath/static?namedParam=myvalue"
        impl.queryParamOnlyNamedNullable(null) shouldBe "somepath/static"
    }

    @Test
    fun `URI builder - query - nullable`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.queryParamOnlyNullable("myvalue") shouldBe "somepath/static?foobar=myvalue"
        impl.queryParamOnlyNullable(null) shouldBe "somepath/static"
    }

    @Test
    fun `URI builder - query - multiple nullable`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.queryParamOnlyMultiNullable("first", "second") shouldBe "somepath/static?foo=first&bar=second"
        impl.queryParamOnlyMultiNullable(null, null) shouldBe "somepath/static"
        impl.queryParamOnlyMultiNullable("first", null) shouldBe "somepath/static?foo=first"
        impl.queryParamOnlyMultiNullable(null, "second") shouldBe "somepath/static?bar=second"
    }

    @Test
    fun `URI builder - query - mixed nullability`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.queryParamOnlyMixedNullability("first", "second") shouldBe "somepath/static?foo=first&bar=second"
        impl.queryParamOnlyMixedNullability("first", null) shouldBe "somepath/static?foo=first"
    }

    @Test
    fun `URI builder - path param - non ascii`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.paramAtEnd("円") shouldBe "start/%E5%86%86"
        impl.paramAtEnd("£") shouldBe "start/%C2%A3"
        impl.paramAtEnd("円500") shouldBe "start/%E5%86%86500"
        impl.paramAtEnd("99円") shouldBe "start/99%E5%86%86"
        impl.paramAtEnd("a円b") shouldBe "start/a%E5%86%86b"
        impl.paramAtEnd("£30") shouldBe "start/%C2%A330"
        impl.paramAtEnd("99£") shouldBe "start/99%C2%A3"
        impl.paramAtEnd("a£b") shouldBe "start/a%C2%A3b"
    }

    @Test
    fun `URI builder - query param - non ascii`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.queryParamOnly("円") shouldBe "somepath/static?foobar=%E5%86%86"
        impl.queryParamOnly("£") shouldBe "somepath/static?foobar=%C2%A3"
        impl.queryParamOnly("円500") shouldBe "somepath/static?foobar=%E5%86%86500"
        impl.queryParamOnly("99円") shouldBe "somepath/static?foobar=99%E5%86%86"
        impl.queryParamOnly("a円b") shouldBe "somepath/static?foobar=a%E5%86%86b"
        impl.queryParamOnly("£30") shouldBe "somepath/static?foobar=%C2%A330"
        impl.queryParamOnly("99£") shouldBe "somepath/static?foobar=99%C2%A3"
        impl.queryParamOnly("a£b") shouldBe "somepath/static?foobar=a%C2%A3b"
    }
}

class DoublingToString(private val value: Int) {
    override fun toString(): String {
        return (value * 2).toString()
    }
}