package value_preserve_box.box_recreation

import shared.BoxCopies
import shared.ObjectConsumer

@JvmInline
private value class FloatClass(val x: Float, val y: Float)
fun heavyActionFloat(consumer: ObjectConsumer) {
    for (i in 0 until 10) {
        val o = FloatClass(i.toFloat(), (i + 1).toFloat())
        var orNull: FloatClass? = null
        @Suppress("KotlinConstantConditions")
        if (orNull == null) orNull = o
        repeat(BoxCopies) {
            consumer.consume(orNull)
        }
    }
}
