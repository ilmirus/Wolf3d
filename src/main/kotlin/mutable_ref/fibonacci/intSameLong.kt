package mutable_ref.fibonacci

import mutable_ref.MutableMfvcWrapper

fun fibonacciSame(n: Int): Int {
    val sharedWrapper = MutableMfvcWrapper()
    return when {
        n < 0 -> error("Wrong n: $n")
        n == 0 -> 0
        else -> {
            fun impl(n: Int, sharedWrapper: MutableMfvcWrapper) {
                if (n == 1) {
                    sharedWrapper.long0 = 0.toLong().shl(32) or 1.toLong().and(0xFFFFFFFF)
                } else {
                    impl(n - 1, sharedWrapper)
                    val x = sharedWrapper.long0.shr(32).toInt()
                    val y = sharedWrapper.long0.toInt()
                    sharedWrapper.long0 = y.toLong().shl(32) or (x + y).toLong().and(0xFFFFFFFF)
                }
            }
            impl(n, sharedWrapper)
            sharedWrapper.long0.toInt()
        }
    }
}

fun main() {
    for (i in 0..10) {
        println(fibonacciSame(i))
    }
}
