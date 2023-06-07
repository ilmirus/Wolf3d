@file:Suppress("NOTHING_TO_INLINE")

package mutable_ref_specific.ackermann

import shared.ComplexNumberConsumer
import shared.ComplexNumberPrinter

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

    fun plus(other: ComplexInt, wrapper: Wrapper) {
        wrapper.encode(this.real + other.real, this.imaginary + other.imaginary)
    }

    fun plus(other: Int, wrapper: Wrapper) {
        wrapper.encode(this.real + other, this.imaginary)
    }

    fun minus(other: ComplexInt, wrapper: Wrapper) {
        wrapper.encode(this.real - other.real, this.imaginary - other.imaginary)
    }

    fun minus(other: Int, wrapper: Wrapper) {
        wrapper.encode(this.real - other, this.imaginary)
    }
    
    class Wrapper {
        @JvmField
        var real: Int = 0
        @JvmField
        var imaginary: Int = 0

        inline fun encode(real: Int, imaginary: Int) {
            this.real = real
            this.imaginary = imaginary
        }

        inline fun decode() = ComplexInt(real, imaginary)
    }
}

private fun getI(wrapper: ComplexInt.Wrapper) {
    wrapper.encode(0, 1)
}

private fun Int.times(complex: ComplexInt, wrapper: ComplexInt.Wrapper) {
    wrapper.encode(this * complex.real, this * complex.imaginary)
}

private fun Int.plus(complex: ComplexInt, wrapper: ComplexInt.Wrapper) {
    wrapper.encode(this + complex.real, complex.imaginary)
}

private fun Int.getComplex(wrapper: ComplexInt.Wrapper) {
    wrapper.encode(this, 0)
}

fun ackermann(a: ComplexInt, b: ComplexInt, n: Int, wrapper: ComplexInt.Wrapper): Unit = when {
    n == 0 -> a.plus(b, wrapper)
    b.real == 0 -> when (val newN = n - 1) {
        0, 1 -> newN.getComplex(wrapper)
        else -> {
            wrapper.encode(a.real, a.imaginary)
        }
    }

    else -> {
        b.minus(1, wrapper)
        ackermann(a, wrapper.decode(), n, wrapper)
        ackermann(a, wrapper.decode(), n - 1, wrapper)
    }
}

fun heavyActionInt(complexConsumer: ComplexNumberConsumer) {
    val wrapper = ComplexInt.Wrapper()
    with(wrapper) {
        for (n in 0..2) {
            for (x in 0..5) {
                val a = run {
                    val arg = run {
                        2.times(run { getI(wrapper); decode() }, wrapper)
                        decode()
                    }
                    x.plus(arg, wrapper)
                    decode()
                }
                val b = run { x.getComplex(wrapper); decode() }
                ackermann(a, b, n, wrapper)
                decode().let { complexConsumer.consume(it.real, it.imaginary) }
            }
        }
    }
}

fun main() {
    heavyActionInt(ComplexNumberPrinter)
}
