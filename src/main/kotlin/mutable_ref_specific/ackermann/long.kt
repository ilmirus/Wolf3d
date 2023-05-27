@file:Suppress("NOTHING_TO_INLINE")

package mutable_ref_specific.ackermann

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
            else -> "$real${if (imaginary < 0L) "" else "+"}${imaginary}i"
        }
    }

    fun plus(other: ComplexLong, wrapper: Wrapper) {
        wrapper.encode(this.real + other.real, this.imaginary + other.imaginary)
    }

    fun plus(other: Long, wrapper: Wrapper) {
        wrapper.encode(this.real + other, this.imaginary)
    }

    fun minus(other: ComplexLong, wrapper: Wrapper) {
        wrapper.encode(this.real - other.real, this.imaginary - other.imaginary)
    }

    fun minus(other: Long, wrapper: Wrapper) {
        wrapper.encode(this.real - other, this.imaginary)
    }

    class Wrapper {
        @JvmField
        var real: Long = 0
        @JvmField
        var imaginary: Long = 0

        inline fun encode(real: Long, imaginary: Long) {
            this.real = real
            this.imaginary = imaginary
        }

        inline fun decode() = ComplexLong(real, imaginary)
    }
}

private fun getI(wrapper: ComplexLong.Wrapper) {
    wrapper.encode(0L, 1L)
}

private fun Long.times(complex: ComplexLong, wrapper: ComplexLong.Wrapper) {
    wrapper.encode(this * complex.real, this * complex.imaginary)
}

private fun Long.plus(complex: ComplexLong, wrapper: ComplexLong.Wrapper) {
    wrapper.encode(this + complex.real, complex.imaginary)
}

private fun Long.getComplex(wrapper: ComplexLong.Wrapper) {
    wrapper.encode(this, 0L)
}

fun ackermann(a: ComplexLong, b: ComplexLong, n: Long, wrapper: ComplexLong.Wrapper): Unit = when {
    n == 0L -> a.plus(b, wrapper)
    b.real == 0L -> when (val newN = n - 1L) {
        0L, 1L -> newN.getComplex(wrapper)
        else -> {
            wrapper.encode(a.real, a.imaginary)
        }
    }

    else -> {
        b.minus(1L, wrapper)
        ackermann(a, wrapper.decode(), n, wrapper)
        ackermann(a, wrapper.decode(), n - 1L, wrapper)
    }
}

fun heavyActionLong(complexConsumer: ComplexNumberConsumer) {
    val wrapper = ComplexLong.Wrapper()
    with(wrapper) {
        for (n in 0L..2L) {
            for (x in 0L..5L) {
                val a = run {
                    val arg = run {
                        2L.times(run { getI(wrapper); decode() }, wrapper)
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
    heavyActionLong(ComplexNumberPrinter)
}
