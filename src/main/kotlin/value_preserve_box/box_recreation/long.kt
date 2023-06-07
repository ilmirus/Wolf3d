package value_preserve_box.box_recreation

import shared.BoxCopies
import shared.ObjectConsumer

@JvmInline
private value class LongClass(val x: Long, val y: Long)
fun heavyActionLong(consumer: ObjectConsumer) {
    for (i in 0L until 10L) {
        val o = LongClass(i, i + 1)
        var orNull: LongClass? = null
        @Suppress("KotlinConstantConditions")
        if (orNull == null) orNull = o
        repeat(BoxCopies) {
            consumer.consume(orNull)
        }
    }
}
