@file:Suppress("NOTHING_TO_INLINE")

package mutable_ref_specific.ackermann

import shared.ComplexNumberConsumer
import shared.ComplexNumberPrinter

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

    fun plus(other: ComplexDouble, wrapper: Wrapper) {
        wrapper.encode(this.real + other.real, this.imaginary + other.imaginary)
    }

    fun plus(other: Double, wrapper: Wrapper) {
        wrapper.encode(this.real + other, this.imaginary)
    }

    fun minus(other: ComplexDouble, wrapper: Wrapper) {
        wrapper.encode(this.real - other.real, this.imaginary - other.imaginary)
    }

    fun minus(other: Double, wrapper: Wrapper) {
        wrapper.encode(this.real - other, this.imaginary)
    }
    
    class Wrapper {
        @JvmField
        var real: Double = 0.0
        @JvmField
        var imaginary: Double = 0.0
        
        inline fun encode(real: Double, imaginary: Double) {
            this.real = real
            this.imaginary = imaginary
        }

        inline fun decode() = ComplexDouble(real, imaginary)
    }
}

private fun getI(wrapper: ComplexDouble.Wrapper) {
    wrapper.encode(0.0, 1.0)
}

private fun Double.times(complex: ComplexDouble, wrapper: ComplexDouble.Wrapper) {
    wrapper.encode(this * complex.real, this * complex.imaginary)
}

private fun Double.plus(complex: ComplexDouble, wrapper: ComplexDouble.Wrapper) {
    wrapper.encode(this + complex.real, complex.imaginary)
}

private fun Double.getComplex(wrapper: ComplexDouble.Wrapper) {
    wrapper.encode(this, 0.0)
}

fun ackermann(a: ComplexDouble, b: ComplexDouble, n: Double, wrapper: ComplexDouble.Wrapper): Unit = when {
    n == 0.0 -> a.plus(b, wrapper)
    b.real == 0.0 -> when (val newN = n - 1.0) {
        0.0, 1.0 -> newN.getComplex(wrapper)
        else -> {
            wrapper.encode(a.real, a.imaginary)
        }
    }

    else -> {
        b.minus(1.0, wrapper)
        ackermann(a, wrapper.decode(), n, wrapper)
        ackermann(a, wrapper.decode(), n - 1.0, wrapper)
    }
}

fun heavyActionDouble(complexConsumer: ComplexNumberConsumer) {
    val wrapper = ComplexDouble.Wrapper()
    with(wrapper) {
        for (n in 0L..2L) {
            for (x in 0L..5L) {
                val a = run {
                    val arg = run {
                        2.0.times(run { getI(wrapper); decode() }, wrapper)
                        decode()
                    }
                    x.toDouble().plus(arg, wrapper)
                    decode()
                }
                val b = run { x.toDouble().getComplex(wrapper); decode() }
                ackermann(a, b, n.toDouble(), wrapper)
                decode().let { complexConsumer.consume(it.real, it.imaginary) }
            }
        }
    }
}

fun main() {
    heavyActionDouble(ComplexNumberPrinter)
}
