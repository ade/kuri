package se.ade.kuri.kmptests

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Test
import se.ade.kuri.KuriTokenSpec
import se.ade.kuri.kmpclient.codegenvalidation.KuriTestPathsProvider

class TemplatesTest {
    @Test
    fun `Templates metadata - URI template`() {
        KuriTestPathsProvider.Templates.pathParamInt.getTemplate() shouldBe "somepath/{number}/static"
    }

    @Test
    fun `Templates metadata - URI template - toString`() {
        KuriTestPathsProvider.Templates.pathParamInt.getTemplate().toString() shouldBe "somepath/{number}/static"
    }

    @Test
    fun `Templates metadata - URI template - equals`() {
        (KuriTestPathsProvider.Templates.pathParamInt.getTemplate() == "somepath/{number}/static") shouldBe true
    }

    @Test
    fun `Templates metadata - Tokens`() {
        KuriTestPathsProvider.Templates.pathParamInt.getTokens() shouldContainExactly listOf(
            KuriTokenSpec(name = "number", type = Int::class,
                query = false, optional = false)
        )
    }

    @Test
    fun `Templates metadata - Tokens - Query - Optional`() {
        KuriTestPathsProvider.Templates.queryParamOnlyNullable.getTokens() shouldContainExactly listOf(
            KuriTokenSpec(name = "foobar", type = String::class,
                query = true, optional = true)
        )
    }

    @Test
    fun `Templates metadata - Tokens - Query - Mandatory`() {
        KuriTestPathsProvider.Templates.queryParamOnly.getTokens() shouldContainExactly listOf(
            KuriTokenSpec(name = "foobar", type = String::class,
                query = true, optional = false)
        )
    }
}