package mutable_ref_universal.ackermann

import mutable_ref_universal.MutableMfvcWrapper
import shared.ComplexNumberConsumer
import shared.ComplexNumberPrinter

@JvmInline
value class ComplexIntSame(val real: Int, val imaginary: Int) {
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

    fun plus(other: ComplexIntSame, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real + other.real, this.imaginary + other.imaginary)
    }

    fun plus(other: Int, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real + other, this.imaginary)
    }

    fun minus(other: ComplexIntSame, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real - other.real, this.imaginary - other.imaginary)
    }

    fun minus(other: Int, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real - other, this.imaginary)
    }
}

private fun getI(wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(0, 1)
}

private fun Int.times(complex: ComplexIntSame, wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this * complex.real, this * complex.imaginary)
}

private fun Int.plus(complex: ComplexIntSame, wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this + complex.real, complex.imaginary)
}

private fun Int.getComplex(wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this, 0)
}

private inline fun MutableMfvcWrapper.decodeComplexInt() = ComplexIntSame(decodeInt00(), decodeInt01())

fun ackermann(a: ComplexIntSame, b: ComplexIntSame, n: Int, wrapper: MutableMfvcWrapper): Unit = when {
    n == 0 -> a.plus(b, wrapper)
    b.real == 0 -> when (val newN = n - 1) {
        0, 1 -> newN.getComplex(wrapper)
        else -> wrapper.encodeLong0(a.real, a.imaginary)
    }

    else -> {
        b.minus(1, wrapper)
        ackermann(a, wrapper.decodeComplexInt(), n, wrapper)
        ackermann(a, wrapper.decodeComplexInt(), n - 1, wrapper)
    }
}

fun heavyActionIntSame(complexConsumer: ComplexNumberConsumer) {
    val wrapper = MutableMfvcWrapper()
    with(wrapper) {
        for (n in 0..2) {
            for (x in 0..5) {
                val a = run {
                    val arg = run {
                        2.times(run { getI(wrapper); decodeComplexInt() }, wrapper)
                        decodeComplexInt()
                    }
                    x.plus(arg, wrapper)
                    decodeComplexInt()
                }
                val b = run { x.getComplex(wrapper); decodeComplexInt() }
                ackermann(a, b, n, wrapper)
                decodeComplexInt().let { complexConsumer.consume(it.real, it.imaginary) }
            }
        }
    }
}

fun main() {
    heavyActionIntSame(ComplexNumberPrinter)
}
