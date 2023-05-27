@file:Suppress("NOTHING_TO_INLINE")

package long_pack.ackermann

import shared.ComplexNumberConsumer
import shared.ComplexNumberPrinter

private inline fun ComplexFloat.encodeToLong() = real.toRawBits().toLong().shl(32) or imaginary.toRawBits().toLong().and(0xFFFFFFFF)
private inline fun Long.decodeToComplexFloat(): ComplexFloat = ComplexFloat(Float.fromBits(shr(32).toInt()), Float.fromBits(toInt()))

@JvmInline
value class ComplexFloat(val real: Float, val imaginary: Float) {
    
    override fun toString(): String = when {
        imaginary == 0.0f -> "$real"
        real == 0.0f -> when (imaginary) {
            1.0f -> "i"
            -1.0f -> "-i"
            else -> "${imaginary}i"
        }
        else -> when (imaginary) {
            1.0f -> "$real+i"
            -1.0f -> "$real-i"
            else -> "$real${if (imaginary < 0.0f) "" else "+"}${imaginary}i"
        }
    }
    operator fun plus(other: ComplexFloat) = ComplexFloat(this.real + other.real, this.imaginary + other.imaginary).encodeToLong()
    operator fun plus(other: Float) = ComplexFloat(this.real + other, this.imaginary).encodeToLong()
    operator fun minus(other: ComplexFloat) = ComplexFloat(this.real - other.real, this.imaginary - other.imaginary).encodeToLong()
    operator fun minus(other: Float) = ComplexFloat(this.real - other, this.imaginary).encodeToLong()
}

private val i: Long get() = ComplexFloat(0.0f, 1.0f).encodeToLong()
private operator fun Float.times(complex: ComplexFloat) = ComplexFloat(this * complex.real, this * complex.imaginary).encodeToLong()
private operator fun Float.plus(complex: ComplexFloat) = ComplexFloat(this + complex.real, complex.imaginary).encodeToLong()

private val Float.complex get() = ComplexFloat(this, 0.0f).encodeToLong()
fun ackermann(a: ComplexFloat, b: ComplexFloat, n: Float): Long = when {
    n == 0.0f -> (a + b)
    b.real == 0.0f -> when (val newN = n - 1.0f) {
        0.0f, 1.0f -> newN.complex
        else -> a.encodeToLong()
    }
    else -> ackermann(a, ackermann(a, (b - 1.0f).decodeToComplexFloat(), n).decodeToComplexFloat(), n - 1.0f)
}

fun heavyActionFloat(complexConsumer: ComplexNumberConsumer) {
    for (n in 0..2) {
        for (x in 0..5) {
            ackermann((x.toFloat() + (2.0f * i.decodeToComplexFloat()).decodeToComplexFloat()).decodeToComplexFloat(), x.toFloat().complex.decodeToComplexFloat(), n.toFloat())
                .decodeToComplexFloat()
                .let { complexConsumer.consume(it.real, it.imaginary) }
        }
    }
}

fun main() {
    heavyActionFloat(ComplexNumberPrinter)
}
