package se.ade.kuri.kmptests

class DoublingToString(private val value: Int) {
    override fun toString(): String {
        return (value * 2).toString()
    }
}

class MakesUnsafeChars {
    override fun toString(): String {
        return "/hello world & have a nice day/"
    }
}