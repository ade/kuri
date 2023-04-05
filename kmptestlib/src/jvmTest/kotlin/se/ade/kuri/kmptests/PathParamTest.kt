package se.ade.kuri.kmptests

import io.kotest.matchers.shouldBe
import org.junit.Test
import se.ade.kuri.kmpclient.codegenvalidation.KuriTestPathsProvider
import se.ade.kuri.kmpclient.codegenvalidation.TestPathsProvider

class PathParamTest {
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
    fun `URI builder - path param - any`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.pathParamAny(DoublingToString(2)) shouldBe "path/4/data"
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
    fun `URI builder - path param - Int`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.pathParamInt(1) shouldBe "somepath/1/static"
        impl.pathParamInt(0) shouldBe "somepath/0/static"
    }

    @Test
    fun `URI builder - path param - Any - escaping`() {
        val impl: TestPathsProvider = KuriTestPathsProvider()
        impl.pathParamAny(MakesUnsafeChars()) shouldBe "path/%2Fhello%20world%20%26%20have%20a%20nice%20day%2F/data"
    }
}

