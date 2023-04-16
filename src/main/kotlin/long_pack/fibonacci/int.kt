package long_pack.fibonacci

@JvmInline
private value class IntPair private constructor(private val storage: Long) {
    constructor(x: Int, y: Int) : this(x.toLong().shl(32) or y.toLong().and(0xFFFFFFFF))
    inline val x get() = storage.shr(32).toInt()
    inline val y get() = storage.toInt()
}
fun fibonacci(n: Int): Int = when {
    n < 0 -> error("Wrong n: $n")
    n == 0 -> 0
    else -> {
        fun impl(n: Int): IntPair =
            if (n == 1) IntPair(0, 1) else impl(n - 1).let { IntPair(it.y, it.x + it.y) }
        impl(n).y
    }
}

fun main() {
    for (i in 0..10) {
        println(fibonacci(i))
    }
}
