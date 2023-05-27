package value.ackermann

import shared.ComplexNumberConsumer
import shared.ComplexNumberPrinter

@JvmInline
value class ComplexLong(val real: Long, val imaginary: Long) {
    override fun toString(): String = when {
        imaginary == 0L -> "$real"
        real == 0L -> when (imaginary) {
            1L -> "i"
            -1L -> "-i"
            else -> "${imaginary}i"
        }
        else -> when (imaginary) {
            1L -> "$real+i"
            -1L -> "$real-i"
            else -> "$real${if (imaginary < 0) "" else "+"}${imaginary}i"
        }
    }
    operator fun plus(other: ComplexLong) = ComplexLong(this.real + other.real, this.imaginary + other.imaginary)
    operator fun plus(other: Long) = ComplexLong(this.real + other, this.imaginary)
    operator fun minus(other: ComplexLong) = ComplexLong(this.real - other.real, this.imaginary - other.imaginary)
    operator fun minus(other: Long) = ComplexLong(this.real - other, this.imaginary)
}

private val i: ComplexLong get() = ComplexLong(0, 1)
private operator fun Long.times(complex: ComplexLong) = ComplexLong(this * complex.real, this * complex.imaginary)
private operator fun Long.plus(complex: ComplexLong) = ComplexLong(this + complex.real, complex.imaginary)

private val Long.complex get() = ComplexLong(this, 0)
fun ackermann(a: ComplexLong, b: ComplexLong, n: Long): ComplexLong = when {
    n == 0L -> a + b
    b.real == 0L -> when (val newN = n - 1) {
        0L, 1L -> newN.complex
        else -> a
    }
    else -> ackermann(a, ackermann(a, b - 1, n), n - 1)
}

fun heavyActionLong(complexConsumer: ComplexNumberConsumer) {
    for (n in 0L..2L) {
        for (x in 0L..5L) {
            ackermann(x + 2L * i, x.complex, n).let { complexConsumer.consume(it.real, it.imaginary) }
        }
    }
}

fun main() {
    heavyActionLong(ComplexNumberPrinter)
}
