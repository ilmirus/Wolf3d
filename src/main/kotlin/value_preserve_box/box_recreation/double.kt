package value_preserve_box.box_recreation

import shared.BoxCopies
import shared.ObjectConsumer

@JvmInline
private value class DoubleClass(val x: Double, val y: Double)
fun heavyActionDouble(consumer: ObjectConsumer) {
    for (i in 0 until 10) {
        val o = DoubleClass(i.toDouble(), (i + 1).toDouble())
        var orNull: DoubleClass? = null
        @Suppress("KotlinConstantConditions")
        if (orNull == null) orNull = o
        repeat(BoxCopies) {
            consumer.consume(orNull)
        }
    }
}
