package value.box_recreation

import BoxCopies
import ObjectConsumer

@JvmInline
private value class DoubleClass(val x: Double, val y: Double)
fun heavyActionDouble(consumer: ObjectConsumer) {
    for (i in 0 until 10) {
        val o = DoubleClass(i.toDouble(), (i + 1).toDouble())
        repeat(BoxCopies) {
            consumer.consume(o)
        }
    }
}
