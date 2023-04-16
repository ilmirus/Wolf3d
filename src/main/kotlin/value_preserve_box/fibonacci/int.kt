@file:Suppress("KotlinConstantConditions")

package value_preserve_box.fibonacci

@JvmInline
private value class IntPair(val x: Int, val y: Int)
fun fibonacci(n: Int): Int = when {
    n < 0 -> error("Wrong n: $n")
    n == 0 -> 0
    else -> {
        fun impl(n: Int): IntPair = if (n == 1) {
            var orNull: IntPair? = null
            if (orNull == null) orNull = IntPair(0, 1)
            orNull
        } else {
            var orNull: IntPair? = null
            if (orNull == null) orNull = impl(n - 1).let { IntPair(it.y, it.x + it.y) }
            orNull
        }
        impl(n).y
    }
}

fun main() {
    for (i in 0..10) {
        println(fibonacci(i))
    }
}
