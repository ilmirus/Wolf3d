package mutable_ref.fibonacci

import mutable_ref.MutableMfvcWrapper

fun fibonacci(n: Long): Long {
    val sharedWrapper = MutableMfvcWrapper()
    return when {
        n < 0 -> error("Wrong n: $n")
        n == 0L -> 0
        else -> {
            fun impl(n: Long, sharedWrapper: MutableMfvcWrapper) {
                if (n == 1L) {
                    sharedWrapper.long0 = 0
                    sharedWrapper.long1 = 1
                } else {
                    impl(n - 1, sharedWrapper)
                    val x = sharedWrapper.long0
                    val y = sharedWrapper.long1
                    sharedWrapper.long0 = y
                    sharedWrapper.long1 = x + y
                }
            }
            impl(n, sharedWrapper)
            sharedWrapper.long1
        }
    }
}

fun main() {
    for (i in 0L..10L) {
        println(fibonacci(i))
    }
}
