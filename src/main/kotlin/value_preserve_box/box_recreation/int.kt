package value_preserve_box.box_recreation

import shared.BoxCopies
import shared.ObjectConsumer

@JvmInline
private value class IntClass(val x: Int, val y: Int)
fun heavyActionInt(consumer: ObjectConsumer) {
    for (i in 0 until 10) {
        val o = IntClass(i, i + 1)
        var orNull: IntClass? = null
        @Suppress("KotlinConstantConditions")
        if (orNull == null) orNull = o
        repeat(BoxCopies) {
            consumer.consume(orNull)
        }
    }
}
