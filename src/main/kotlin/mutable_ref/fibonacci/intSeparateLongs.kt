package mutable_ref.fibonacci

import mutable_ref.MutableMfvcWrapper

fun fibonacciSeparate(n: Int): Int {
    val sharedWrapper = MutableMfvcWrapper()
    return when {
        n < 0 -> error("Wrong n: $n")
        n == 0 -> 0
        else -> {
            fun impl(n: Int, sharedWrapper: MutableMfvcWrapper) {
                if (n == 1) {
                    sharedWrapper.long0 = 0.toLong()
                    sharedWrapper.long1 = 1.toLong()
                } else {
                    impl(n - 1, sharedWrapper)
                    val x = sharedWrapper.long0.toInt()
                    val y = sharedWrapper.long1.toInt()
                    sharedWrapper.long0 = y.toLong()
                    sharedWrapper.long1 = (x + y).toLong()
                }
            }
            impl(n, sharedWrapper)
            sharedWrapper.long1.toInt()
        }
    }
}

fun main() {
    for (i in 0..10) {
        println(fibonacciSeparate(i))
    }
}
