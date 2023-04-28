@file:Suppress("NOTHING_TO_INLINE")

package mutable_ref_specific.ackermann

import ComplexNumberConsumer
import ComplexNumberPrinter

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

    fun plus(other: ComplexFloat, wrapper: ComplexFloat.Wrapper) {
        wrapper.encode(this.real + other.real, this.imaginary + other.imaginary)
    }

    fun plus(other: Float, wrapper: ComplexFloat.Wrapper) {
        wrapper.encode(this.real + other, this.imaginary)
    }

    fun minus(other: ComplexFloat, wrapper: ComplexFloat.Wrapper) {
        wrapper.encode(this.real - other.real, this.imaginary - other.imaginary)
    }

    fun minus(other: Float, wrapper: ComplexFloat.Wrapper) {
        wrapper.encode(this.real - other, this.imaginary)
    }

    class Wrapper() {
        @JvmField
        var real: Float = 0.0f
        @JvmField
        var imaginary: Float = 0.0f

        inline fun encode(real: Float, imaginary: Float) {
            this.real = real
            this.imaginary = imaginary
        }

        inline fun decode() = ComplexFloat(real, imaginary)
    }
}

private fun getI(wrapper: ComplexFloat.Wrapper) {
    wrapper.encode(0.0f, 1.0f)
}

private fun Float.times(complex: ComplexFloat, wrapper: ComplexFloat.Wrapper) {
    wrapper.encode(this * complex.real, this * complex.imaginary)
}

private fun Float.plus(complex: ComplexFloat, wrapper: ComplexFloat.Wrapper) {
    wrapper.encode(this + complex.real, complex.imaginary)
}

private fun Float.getComplex(wrapper: ComplexFloat.Wrapper) {
    wrapper.encode(this, 0.0f)
}

fun ackermann(a: ComplexFloat, b: ComplexFloat, n: Float, wrapper: ComplexFloat.Wrapper): Unit = when {
    n == 0.0f -> a.plus(b, wrapper)
    b.real == 0.0f -> when (val newN = n - 1.0f) {
        0.0f, 1.0f -> newN.getComplex(wrapper)
        else -> {
            wrapper.encode(a.real, a.imaginary)
        }
    }

    else -> {
        b.minus(1.0f, wrapper)
        ackermann(a, wrapper.decode(), n, wrapper)
        ackermann(a, wrapper.decode(), n - 1.0f, wrapper)
    }
}

fun heavyActionFloat(complexConsumer: ComplexNumberConsumer) {
    val wrapper = ComplexFloat.Wrapper()
    with(wrapper) {
        for (n in 0..2) {
            for (x in 0..5) {
                val a = run {
                    val arg = run {
                        2.0f.times(run { getI(wrapper); decode() }, wrapper)
                        decode()
                    }
                    x.toFloat().plus(arg, wrapper)
                    decode()
                }
                val b = run { x.toFloat().getComplex(wrapper); decode() }
                ackermann(a, b, n.toFloat(), wrapper)
                decode().let { complexConsumer.consume(it.real, it.imaginary) }
            }
        }
    }
}

fun main() {
    heavyActionFloat(ComplexNumberPrinter)
}
