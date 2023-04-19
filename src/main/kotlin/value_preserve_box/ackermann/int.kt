@file:Suppress("KotlinConstantConditions")

package value_preserve_box.ackermann

import ComplexNumberConsumer
import ComplexNumberPrinter

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
    operator fun plus(other: ComplexInt): ComplexInt {
        var orNull: ComplexInt? = null
        if (orNull == null) orNull = ComplexInt(this.real + other.real, this.imaginary + other.imaginary)
        return orNull
    }
    operator fun plus(other: Int): ComplexInt {
        var orNull: ComplexInt? = null
        if (orNull == null) orNull = ComplexInt(this.real + other, this.imaginary)
        return orNull
    }
    operator fun minus(other: ComplexInt): ComplexInt {
        var orNull: ComplexInt? = null
        if (orNull == null) orNull = ComplexInt(this.real - other.real, this.imaginary - other.imaginary)
        return orNull
    }
    operator fun minus(other: Int): ComplexInt {
        var orNull: ComplexInt? = null
        if (orNull == null) orNull = ComplexInt(this.real - other, this.imaginary)
        return orNull
    }
}

private val i: ComplexInt get() {
    var orNull: ComplexInt? = null
    if (orNull == null) orNull = ComplexInt(0, 1)
    return orNull
}
private operator fun Int.times(complex: ComplexInt): ComplexInt {
    var orNull: ComplexInt? = null
    if (orNull == null) orNull = ComplexInt(this * complex.real, this * complex.imaginary)
    return orNull
}
private operator fun Int.plus(complex: ComplexInt): ComplexInt {
    var orNull: ComplexInt? = null
    if (orNull == null) orNull = ComplexInt(this + complex.real, complex.imaginary)
    return orNull
}

private val Int.complex: ComplexInt
    get() {
        var orNull: ComplexInt? = null
        if (orNull == null) orNull = ComplexInt(this, 0)
        return orNull
    }
fun ackermann(a: ComplexInt, b: ComplexInt, n: Int): ComplexInt = when {
    n == 0 -> a + b
    b.real == 0 -> when (val newN = n - 1) {
        0, 1 -> newN.complex
        else -> a
    }
    else -> ackermann(a, ackermann(a, b - 1, n), n - 1)
}

fun heavyActionInt(complexConsumer: ComplexNumberConsumer) {
    for (n in 0..2) {
        for (x in 0..5) {
            ackermann(x + 2 * i, x.complex, n).let { complexConsumer.consume(it.real, it.imaginary) }
        }
    }
}

fun main() {
    heavyActionInt(ComplexNumberPrinter)
}
