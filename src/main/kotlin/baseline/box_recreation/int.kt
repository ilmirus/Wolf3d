package baseline.box_recreation

import BoxCopies
import ObjectConsumer

private data class IntClass(val x: Int, val y: Int)
fun heavyActionInt(consumer: ObjectConsumer) {
    for (i in 0 until 10) {
        val o = IntClass(i, i + 1)
        repeat(BoxCopies) {
            consumer.consume(o)
        }
    }
}
