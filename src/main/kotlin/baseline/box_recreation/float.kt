package baseline.box_recreation

import shared.BoxCopies
import shared.ObjectConsumer

private data class FloatClass(val x: Float, val y: Float)
fun heavyActionFloat(consumer: ObjectConsumer) {
    for (i in 0 until 10) {
        val o = FloatClass(i.toFloat(), (i + 1).toFloat())
        repeat(BoxCopies) {
            consumer.consume(o)
        }
    }
}
