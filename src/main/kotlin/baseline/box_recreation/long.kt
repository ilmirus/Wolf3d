package baseline.box_recreation

import shared.BoxCopies
import shared.ObjectConsumer

private data class LongClass(val x: Long, val y: Long)
fun heavyActionLong(consumer: ObjectConsumer) {
    for (i in 0L until 10L) {
        val o = LongClass(i, i + 1)
        repeat(BoxCopies) {
            consumer.consume(o)
        }
    }
}
