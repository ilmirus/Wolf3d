@file:Suppress("KotlinConstantConditions")

package value_preserve_box.ackermann

import shared.ComplexNumberConsumer
import shared.ComplexNumberPrinter

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
    operator fun plus(other: ComplexFloat): ComplexFloat {
        var orNull: ComplexFloat? = null
        if (orNull == null) orNull = ComplexFloat(this.real + other.real, this.imaginary + other.imaginary)
        return orNull
    }
    operator fun plus(other: Float): ComplexFloat {
        var orNull: ComplexFloat? = null
        if (orNull == null) orNull = ComplexFloat(this.real + other, this.imaginary)
        return orNull
    }
    operator fun minus(other: ComplexFloat): ComplexFloat {
        var orNull: ComplexFloat? = null
        if (orNull == null) orNull = ComplexFloat(this.real - other.real, this.imaginary - other.imaginary)
        return orNull
    }
    operator fun minus(other: Float): ComplexFloat {
        var orNull: ComplexFloat? = null
        if (orNull == null) orNull = ComplexFloat(this.real - other, this.imaginary)
        return orNull
    }
}

private val i: ComplexFloat get() {
    var orNull: ComplexFloat? = null
    if (orNull == null) orNull = ComplexFloat(0.0f, 1.0f)
    return orNull
}
private operator fun Float.times(complex: ComplexFloat): ComplexFloat {
    var orNull: ComplexFloat? = null
    if (orNull == null) orNull = ComplexFloat(this * complex.real, this * complex.imaginary)
    return orNull
}
private operator fun Float.plus(complex: ComplexFloat): ComplexFloat {
    var orNull: ComplexFloat? = null
    if (orNull == null) orNull = ComplexFloat(this + complex.real, complex.imaginary)
    return orNull
}

private val Float.complex: ComplexFloat
    get() {
        var orNull: ComplexFloat? = null
        if (orNull == null) orNull = ComplexFloat(this, 0.0f)
        return orNull
    }
fun ackermann(a: ComplexFloat, b: ComplexFloat, n: Float): ComplexFloat = when {
    n == 0.0f -> a + b
    b.real == 0.0f -> when (val newN = n - 1.0f) {
        0.0f, 1.0f -> newN.complex
        else -> a
    }
    else -> ackermann(a, ackermann(a, b - 1.0f, n), n - 1.0f)
}

fun heavyActionFloat(complexConsumer: ComplexNumberConsumer) {
    for (n in 0..2) {
        for (x in 0..5) {
            ackermann(x.toFloat() + 2.0f * i, x.toFloat().complex, n.toFloat())
                .let { complexConsumer.consume(it.real, it.imaginary) }
        }
    }
}

fun main() {
    heavyActionFloat(ComplexNumberPrinter)
}
