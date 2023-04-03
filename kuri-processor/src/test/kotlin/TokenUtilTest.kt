import io.kotest.matchers.shouldBe
import org.junit.Test
import se.ade.kuri.processor.TokenUtil

class TokenUtilTest {
    @Test
    fun testStartOnly() {
        val start = "%"
        TokenUtil.getTokensFromTemplate("mypath/%param/hello", start) shouldBe listOf("param")
        TokenUtil.getTokensFromTemplate("%param/hello", start) shouldBe listOf("param")
        TokenUtil.getTokensFromTemplate("mypath/%param1/hello/%param2", start) shouldBe listOf("param1", "param2")
        TokenUtil.getTokensFromTemplate("mypath/no/params", start) shouldBe listOf()
    }

    @Test
    fun testStartOnlyString() {
        val start = "!@"
        TokenUtil.getTokensFromTemplate("mypath/!@param/hello", start) shouldBe listOf("param")
        TokenUtil.getTokensFromTemplate("!@param/hello", start) shouldBe listOf("param")
        TokenUtil.getTokensFromTemplate("mypath/!@param1/hello/!@param2", start) shouldBe listOf("param1", "param2")
        TokenUtil.getTokensFromTemplate("mypath/no/params", start) shouldBe listOf()
    }

    @Test
    fun testStartAndEnd() {
        val start = "{"
        val end = "}"
        TokenUtil.getTokensFromTemplate("mypath/{param}/hello", start, end) shouldBe listOf("param")
        TokenUtil.getTokensFromTemplate("{param1}/{param2}/hello", start, end) shouldBe listOf("param1", "param2")
        TokenUtil.getTokensFromTemplate("mypath/{param1}/hello/{param2}", start, end) shouldBe listOf("param1", "param2")
        TokenUtil.getTokensFromTemplate("mypath/no/params", start, end) shouldBe listOf()
    }

    @Test
    fun testStartAndEndSameChar() {
        val start = "%"
        val end = "%"
        TokenUtil.getTokensFromTemplate("mypath/%param%/hello", start, end) shouldBe listOf("param")
        TokenUtil.getTokensFromTemplate("%param1%/%param2%/hello", start, end) shouldBe listOf("param1", "param2")
        TokenUtil.getTokensFromTemplate("mypath/%param1%/hello/%param2%", start, end) shouldBe listOf("param1", "param2")
        TokenUtil.getTokensFromTemplate("mypath/no/params", start, end) shouldBe listOf()
    }

    @Test
    fun testStartAndEndDifferentString() {
        val start = "%%"
        val end = "%%"
        TokenUtil.getTokensFromTemplate("mypath/%%param%%/hello", start, end) shouldBe listOf("param")
        TokenUtil.getTokensFromTemplate("%%param1%%/%%param2%%/hello", start, end) shouldBe listOf("param1", "param2")
        TokenUtil.getTokensFromTemplate("mypath/%%param1%%/hello/%%param2%%", start, end) shouldBe listOf("param1", "param2")
        TokenUtil.getTokensFromTemplate("mypath/no/params", start, end) shouldBe listOf()
    }

    @Test
    fun testStartAndEndSameStrings() {
        val start = "%%"
        val end = "%%"
        TokenUtil.getTokensFromTemplate("mypath/%%param%%/hello", start, end) shouldBe listOf("param")
        TokenUtil.getTokensFromTemplate("%%param1%%/%%param2%%/hello", start, end) shouldBe listOf("param1", "param2")
        TokenUtil.getTokensFromTemplate("mypath/%%param1%%/hello/%%param2%%", start, end) shouldBe listOf("param1", "param2")
        TokenUtil.getTokensFromTemplate("mypath/no/params", start, end) shouldBe listOf()
    }
}