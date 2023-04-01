package se.ade.kuri.kmptests

import io.kotest.matchers.should
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
}

class DoublingToString(private val value: Int) {
    override fun toString(): String {
        return (value * 2).toString()
    }
}