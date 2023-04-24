package long_pack.box_recreation

import BoxCopies
import ObjectConsumer

@Suppress("unused")
@JvmInline
private value class FloatClass private constructor(private val storage: Long) {
    constructor(x: Float, y: Float) : this(
        x.toRawBits().toLong().shl(32) or y.toRawBits().toLong().and(0xFFFFFFFF)
    )

    inline val x get() = Float.fromBits(storage.shr(32).toInt())
    inline val y get() = Float.fromBits(storage.toInt())
}
fun heavyActionFloat(consumer: ObjectConsumer) {
    for (i in 0 until 10) {
        val o = FloatClass(i.toFloat(), (i + 1).toFloat())
        repeat(BoxCopies) {
            consumer.consume(o)
        }
    }
}
