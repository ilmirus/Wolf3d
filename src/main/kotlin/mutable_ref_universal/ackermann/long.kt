package mutable_ref_universal.ackermann

import mutable_ref_universal.MutableMfvcWrapper
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

    fun plus(other: ComplexLong, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real + other.real)
        wrapper.encodeLong1(this.imaginary + other.imaginary)
    }

    fun plus(other: Long, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real + other)
        wrapper.encodeLong1(this.imaginary)
    }

    fun minus(other: ComplexLong, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real - other.real)
        wrapper.encodeLong1(this.imaginary - other.imaginary)
    }

    fun minus(other: Long, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real - other)
        wrapper.encodeLong1(this.imaginary)
    }
}

private fun getI(wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(0L)
    wrapper.encodeLong1(1L)
}

private fun Long.times(complex: ComplexLong, wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this * complex.real)
    wrapper.encodeLong1(this * complex.imaginary)
}

private fun Long.plus(complex: ComplexLong, wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this + complex.real)
    wrapper.encodeLong1(complex.imaginary)
}

private fun Long.getComplex(wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this)
    wrapper.encodeLong1(0L)
}

private inline fun MutableMfvcWrapper.decodeComplexLong() = ComplexLong(decodeLong0(), decodeLong1())

fun ackermann(a: ComplexLong, b: ComplexLong, n: Long, wrapper: MutableMfvcWrapper): Unit = when {
    n == 0L -> a.plus(b, wrapper)
    b.real == 0L -> when (val newN = n - 1) {
        0L, 1L -> newN.getComplex(wrapper)
        else -> {
            wrapper.encodeLong0(a.real)
            wrapper.encodeLong1(a.imaginary)
        }
    }

    else -> {
        b.minus(1, wrapper)
        ackermann(a, wrapper.decodeComplexLong(), n, wrapper)
        ackermann(a, wrapper.decodeComplexLong(), n - 1, wrapper)
    }
}

fun heavyActionLong(complexConsumer: ComplexNumberConsumer) {
    val wrapper = MutableMfvcWrapper()
    with(wrapper) {
        for (n in 0L..2L) {
            for (x in 0L..5L) {
                val a = run {
                    val arg = run {
                        2L.times(run { getI(wrapper); decodeComplexLong() }, wrapper)
                        decodeComplexLong()
                    }
                    x.plus(arg, wrapper)
                    decodeComplexLong()
                }
                val b = run { x.getComplex(wrapper); decodeComplexLong() }
                ackermann(a, b, n, wrapper)
                decodeComplexLong().let { complexConsumer.consume(it.real, it.imaginary) }
            }
        }
    }
}

fun main() {
    heavyActionLong(ComplexNumberPrinter)
}
