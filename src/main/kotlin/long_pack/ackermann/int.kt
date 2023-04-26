@file:Suppress("NOTHING_TO_INLINE")

package long_pack.ackermann

import ComplexNumberConsumer
import ComplexNumberPrinter

private inline fun ComplexInt.encodeToLong() = real.toLong().shl(32) or imaginary.toLong().and(0xFFFFFFFF)
private inline fun Long.decodeToComplexInt(): ComplexInt = ComplexInt(shr(32).toInt(), toInt())

@JvmInline
value class ComplexInt(val real: Int, val imaginary: Int) {
    
    override fun toString(): String = when {
        imaginary == 0 -> "$real"
        real == 0 -> when (imaginary) {
            1 -> "i"
            -1 -> "-i"
            else -> "${imaginary}i"
        }
        else -> when (imaginary) {
            1 -> "$real+i"
            -1 -> "$real-i"
            else -> "$real${if (imaginary < 0) "" else "+"}${imaginary}i"
        }
    }
    operator fun plus(other: ComplexInt) = ComplexInt(this.real + other.real, this.imaginary + other.imaginary).encodeToLong()
    operator fun plus(other: Int) = ComplexInt(this.real + other, this.imaginary).encodeToLong()
    operator fun minus(other: ComplexInt) = ComplexInt(this.real - other.real, this.imaginary - other.imaginary).encodeToLong()
    operator fun minus(other: Int) = ComplexInt(this.real - other, this.imaginary).encodeToLong()
}

private val i: Long get() = ComplexInt(0, 1).encodeToLong()
private operator fun Int.times(complex: ComplexInt) = ComplexInt(this * complex.real, this * complex.imaginary).encodeToLong()
private operator fun Int.plus(complex: ComplexInt) = ComplexInt(this + complex.real, complex.imaginary).encodeToLong()

private val Int.complex get() = ComplexInt(this, 0).encodeToLong()
fun ackermann(a: ComplexInt, b: ComplexInt, n: Int): Long = when {
    n == 0 -> (a + b)
    b.real == 0 -> when (val newN = n - 1) {
        0, 1 -> newN.complex
        else -> a.encodeToLong()
    }
    else -> ackermann(a, ackermann(a, (b - 1).decodeToComplexInt(), n).decodeToComplexInt(), n - 1)
}

fun heavyActionInt(complexConsumer: ComplexNumberConsumer) {
    for (n in 0..2) {
        for (x in 0..5) {
            ackermann((x.toInt() + (2 * i.decodeToComplexInt()).decodeToComplexInt()).decodeToComplexInt(), x.toInt().complex.decodeToComplexInt(), n.toInt())
                .decodeToComplexInt()
                .let { complexConsumer.consume(it.real, it.imaginary) }
        }
    }
}

fun main() {
    heavyActionInt(ComplexNumberPrinter)
}
