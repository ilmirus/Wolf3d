package baseline.fibonacci

private data class LongPair(val x: Long, val y: Long)
fun fibonacci(n: Long): Long = when {
    n < 0 -> error("Wrong n: $n")
    n == 0L -> 0
    else -> {
        fun impl(n: Long): LongPair =
            if (n == 1L) LongPair(0, 1) else impl(n - 1).let { LongPair(it.y, it.x + it.y) }
        impl(n).y
    }
}

fun main() {
    for (i in 0L..10L) {
        println(fibonacci(i))
    }
}
