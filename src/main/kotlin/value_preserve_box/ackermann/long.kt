@file:Suppress("KotlinConstantConditions")

package value_preserve_box.ackermann

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

    operator fun plus(other: ComplexLong): ComplexLong {
        var orNull: ComplexLong? = null
        if (orNull == null) orNull = ComplexLong(this.real + other.real, this.imaginary + other.imaginary)
        return orNull
    }

    operator fun plus(other: Long): ComplexLong {
        var orNull: ComplexLong? = null
        if (orNull == null) orNull = ComplexLong(this.real + other, this.imaginary)
        return orNull
    }

    operator fun minus(other: ComplexLong): ComplexLong {
        var orNull: ComplexLong? = null
        if (orNull == null) orNull = ComplexLong(this.real - other.real, this.imaginary - other.imaginary)
        return orNull
    }

    operator fun minus(other: Long): ComplexLong {
        var orNull: ComplexLong? = null
        if (orNull == null) orNull = ComplexLong(this.real - other, this.imaginary)
        return orNull
    }
}

private val i: ComplexLong
    get(): ComplexLong {
        var orNull: ComplexLong? = null
        if (orNull == null) orNull = ComplexLong(0, 1)
        return orNull
    }

private operator fun Long.times(complex: ComplexLong): ComplexLong {
    var orNull: ComplexLong? = null
    if (orNull == null) orNull = ComplexLong(this * complex.real, this * complex.imaginary)
    return orNull
}

private operator fun Long.plus(complex: ComplexLong): ComplexLong {
    var orNull: ComplexLong? = null
    if (orNull == null) orNull = ComplexLong(this + complex.real, complex.imaginary)
    return orNull
}

private val Long.complex
    get(): ComplexLong {
        var orNull: ComplexLong? = null
        if (orNull == null) orNull = ComplexLong(this, 0)
        return orNull
    }

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
