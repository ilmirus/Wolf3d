@file:Suppress("KotlinConstantConditions")

package value_preserve_box.fibonacci

@JvmInline
private value class LongPair(val x: Long, val y: Long)
fun fibonacci(n: Long): Long = when {
    n < 0 -> error("Wrong n: $n")
    n == 0L -> 0
    else -> {
        fun impl(n: Long): LongPair = if (n == 1L) {
            var orNull: LongPair? = null
            if (orNull == null) orNull = LongPair(0, 1)
            orNull
        } else {
            var orNull: LongPair? = null
            if (orNull == null) orNull = impl(n - 1).let { LongPair(it.y, it.x + it.y) }
            orNull
        }
        impl(n).y
    }
}

fun main() {
    for (i in 0L..10L) {
        println(fibonacci(i))
    }
}
