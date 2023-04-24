package mutable_ref.ackermann

import mutable_ref.MutableMfvcWrapper
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

    fun plus(other: ComplexDouble, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real + other.real)
        wrapper.encodeLong1(this.imaginary + other.imaginary)
    }

    fun plus(other: Double, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real + other)
        wrapper.encodeLong1(this.imaginary)
    }

    fun minus(other: ComplexDouble, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real - other.real)
        wrapper.encodeLong1(this.imaginary - other.imaginary)
    }

    fun minus(other: Double, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real - other)
        wrapper.encodeLong1(this.imaginary)
    }
}

private fun getI(wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(0.0)
    wrapper.encodeLong1(1.0)
}

private fun Double.times(complex: ComplexDouble, wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this * complex.real)
    wrapper.encodeLong1(this * complex.imaginary)
}

private fun Double.plus(complex: ComplexDouble, wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this + complex.real)
    wrapper.encodeLong1(complex.imaginary)
}

private fun Double.getComplex(wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this)
    wrapper.encodeLong1(0.0)
}

private inline fun MutableMfvcWrapper.decodeComplexDouble() = ComplexDouble(decodeDouble0(), decodeDouble1())

fun ackermann(a: ComplexDouble, b: ComplexDouble, n: Double, wrapper: MutableMfvcWrapper): Unit = when {
    n == 0.0 -> a.plus(b, wrapper)
    b.real == 0.0 -> when (val newN = n - 1.0) {
        0.0, 1.0 -> newN.getComplex(wrapper)
        else -> {
            wrapper.encodeLong0(a.real)
            wrapper.encodeLong1(a.imaginary)
        }
    }

    else -> {
        b.minus(1.0, wrapper)
        ackermann(a, wrapper.decodeComplexDouble(), n, wrapper)
        ackermann(a, wrapper.decodeComplexDouble(), n - 1.0, wrapper)
    }
}

fun heavyActionDouble(complexConsumer: ComplexNumberConsumer) {
    val wrapper = MutableMfvcWrapper()
    with(wrapper) {
        for (n in 0L..2L) {
            for (x in 0L..5L) {
                val a = run {
                    val arg = run {
                        2.0.times(run { getI(wrapper); decodeComplexDouble() }, wrapper)
                        decodeComplexDouble()
                    }
                    x.toDouble().plus(arg, wrapper)
                    decodeComplexDouble()
                }
                val b = run { x.toDouble().getComplex(wrapper); decodeComplexDouble() }
                ackermann(a, b, n.toDouble(), wrapper)
                decodeComplexDouble().let { complexConsumer.consume(it.real, it.imaginary) }
            }
        }
    }
}

fun main() {
    heavyActionDouble(ComplexNumberPrinter)
}
