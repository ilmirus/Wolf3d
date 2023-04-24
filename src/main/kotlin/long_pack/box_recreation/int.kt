package long_pack.box_recreation

import BoxCopies
import ObjectConsumer

@Suppress("unused")
@JvmInline
private value class IntClass private constructor(private val storage: Long) {
    constructor(x: Int, y: Int) : this(x.toLong().shl(32) or y.toLong().and(0xFFFFFFFF))
    
    inline val x get() = storage.shr(32).toInt()
    inline val y get() = storage.toInt()
}

fun heavyActionInt(consumer: ObjectConsumer) {
    for (i in 0 until 10) {
        val o = IntClass(i, i + 1)
        repeat(BoxCopies) {
            consumer.consume(o)
        }
    }
}
