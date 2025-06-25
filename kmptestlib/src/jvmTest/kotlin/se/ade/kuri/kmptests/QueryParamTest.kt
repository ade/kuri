package se.ade.kuri.kmptests

import io.kotest.matchers.shouldBe
import org.junit.Test
import se.ade.kuri.kmpclient.codegenvalidation.KuriTestPathsProvider
import se.ade.kuri.kmpclient.codegenvalidation.TestPathsProvider

class QueryParamTest {

    @Test
    fun `URI builder - query`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamOnly("myvalue") shouldBe "somepath/static?foobar=myvalue"
        impl.queryParamOnly("value with spaces") shouldBe "somepath/static?foobar=value%20with%20spaces"
        impl.queryParamOnly("strange chars: {} % #") shouldBe "somepath/static?foobar=strange%20chars%3A%20%7B%7D%20%25%20%23"
        impl.queryParamOnly("strange chars: / ? /") shouldBe "somepath/static?foobar=strange%20chars%3A%20/%20?%20/"
        impl.queryParamOnly("https://example.com/why/examples/are/great?id=42") shouldBe "somepath/static?foobar=https%3A//example.com/why/examples/are/great?id%3D42"
    }

    @Test
    fun `URI builder - query - named`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamOnlyNamed("myvalue") shouldBe "somepath/static?namedParam=myvalue"
    }

    @Test
    fun `URI builder - query - named - nullable`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamOnlyNamedNullable("myvalue") shouldBe "somepath/static?namedParam=myvalue"
        impl.queryParamOnlyNamedNullable(null) shouldBe "somepath/static"
    }

    @Test
    fun `URI builder - query - nullable`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamOnlyNullable("myvalue") shouldBe "somepath/static?foobar=myvalue"
        impl.queryParamOnlyNullable(null) shouldBe "somepath/static"
    }

    @Test
    fun `URI builder - query - multiple nullable`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamOnlyMultiNullable("first", "second") shouldBe "somepath/static?foo=first&bar=second"
        impl.queryParamOnlyMultiNullable(null, null) shouldBe "somepath/static"
        impl.queryParamOnlyMultiNullable("first", null) shouldBe "somepath/static?foo=first"
        impl.queryParamOnlyMultiNullable(null, "second") shouldBe "somepath/static?bar=second"
    }

    @Test
    fun `URI builder - query - mixed nullability`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamOnlyMixedNullability("first", "second") shouldBe "somepath/static?foo=first&bar=second"
        impl.queryParamOnlyMixedNullability("first", null) shouldBe "somepath/static?foo=first"
    }


    @Test
    fun `URI builder - query param - non ascii`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamOnly("円") shouldBe "somepath/static?foobar=%E5%86%86"
        impl.queryParamOnly("£") shouldBe "somepath/static?foobar=%C2%A3"
        impl.queryParamOnly("円500") shouldBe "somepath/static?foobar=%E5%86%86500"
        impl.queryParamOnly("99円") shouldBe "somepath/static?foobar=99%E5%86%86"
        impl.queryParamOnly("a円b") shouldBe "somepath/static?foobar=a%E5%86%86b"
        impl.queryParamOnly("£30") shouldBe "somepath/static?foobar=%C2%A330"
        impl.queryParamOnly("99£") shouldBe "somepath/static?foobar=99%C2%A3"
        impl.queryParamOnly("a£b") shouldBe "somepath/static?foobar=a%C2%A3b"
    }

    @Test
    fun `URI builder - query param - boolean`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamBoolean(true) shouldBe "somepath/static?foo=1"
        impl.queryParamBoolean(false) shouldBe "somepath/static?foo=0"
    }

    @Test
    fun `URI builder - query param - Int`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamInt(1) shouldBe "somepath/static?foo=1"
        impl.queryParamInt(0) shouldBe "somepath/static?foo=0"
    }

    @Test
    fun `URI builder - query param - Any`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamAny(DoublingToString(1)) shouldBe "somepath/static?foo=2"
    }

    @Test
    fun `URI builder - query param - Any - escaping`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamAny(MakesUnsafeChars()) shouldBe "somepath/static?foo=/hello%20world%20%26%20have%20a%20nice%20day/"
    }

    @Test
    fun `URI builder - query param - List`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamList(listOf("a", "b", "c")) shouldBe "somepath/static?foo=a&foo=b&foo=c"
    }

    @Test
    fun `URI builder - query param - List - empty`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamList(emptyList()) shouldBe "somepath/static"
    }

    @Test
    fun `URI builder - query param - List - null`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamListNullable(null) shouldBe "somepath/static"
    }

    @Test
    fun `URI builder - query param - List - nullable but not null`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamListNullable(listOf("a", "b", "c")) shouldBe "somepath/static?foo=a&foo=b&foo=c"
    }

    @Test
    fun `URI builder - query param - Set`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamSet(setOf("a", "b", "c")) shouldBe "somepath/static?foo=a&foo=b&foo=c"
    }

    @Test
    fun `URI builder - query param - Set - empty`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamSet(emptySet()) shouldBe "somepath/static"
    }

    @Test
    fun `URI builder - query param - Set - null`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamSetNullable(null) shouldBe "somepath/static"
    }

    @Test
    fun `URI builder - query param - Set - nullable but not null`() {
        val impl: TestPathsProvider = KuriTestPathsProvider
        impl.queryParamSetNullable(setOf("a", "b", "c")) shouldBe "somepath/static?foo=a&foo=b&foo=c"
    }
}

