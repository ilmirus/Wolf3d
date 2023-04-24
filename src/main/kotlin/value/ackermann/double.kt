package value.ackermann

import ComplexNumberConsumer
import ComplexNumberPrinter

@JvmInline
value class ComplexDouble(val real: Double, val imaginary: Double) {
    override fun toString(): String = when {
        imaginary == 0.0 -> "$real"
        real == 0.0 -> when (imaginary) {
            1.0 -> "i"
            -1.0 -> "-i"
            else -> "${imaginary}i"
        }
        else -> when (imaginary) {
            1.0 -> "$real+i"
            -1.0 -> "$real-i"
            else -> "$real${if (imaginary < 0.0) "" else "+"}${imaginary}i"
        }
    }
    operator fun plus(other: ComplexDouble) = ComplexDouble(this.real + other.real, this.imaginary + other.imaginary)
    operator fun plus(other: Double) = ComplexDouble(this.real + other, this.imaginary)
    operator fun minus(other: ComplexDouble) = ComplexDouble(this.real - other.real, this.imaginary - other.imaginary)
    operator fun minus(other: Double) = ComplexDouble(this.real - other, this.imaginary)
}

private val i: ComplexDouble get() = ComplexDouble(0.0, 1.0)
private operator fun Double.times(complex: ComplexDouble) = ComplexDouble(this * complex.real, this * complex.imaginary)
private operator fun Double.plus(complex: ComplexDouble) = ComplexDouble(this + complex.real, complex.imaginary)

private val Double.complex get() = ComplexDouble(this, 0.0)
fun ackermann(a: ComplexDouble, b: ComplexDouble, n: Double): ComplexDouble = when {
    n == 0.0 -> a + b
    b.real == 0.0 -> when (val newN = n - 1.0) {
        0.0, 1.0 -> newN.complex
        else -> a
    }
    else -> ackermann(a, ackermann(a, b - 1.0, n), n - 1.0)
}

fun heavyActionDouble(complexConsumer: ComplexNumberConsumer) {
    for (n in 0L..2L) {
        for (x in 0L..5L) {
            ackermann(x.toDouble() + 2.0 * i, x.toDouble().complex, n.toDouble())
                .let { complexConsumer.consume(it.real, it.imaginary) }
        }
    }
}

fun main() {
    heavyActionDouble(ComplexNumberPrinter)
}
