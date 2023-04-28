package mutable_ref_universal.ackermann

import mutable_ref_universal.MutableMfvcWrapper
import ComplexNumberConsumer
import ComplexNumberPrinter

@JvmInline
value class ComplexFloatSeparate(val real: Float, val imaginary: Float) {
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

    fun plus(other: ComplexFloatSeparate, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real + other.real)
        wrapper.encodeLong1(this.imaginary + other.imaginary)
    }

    fun plus(other: Float, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real + other)
        wrapper.encodeLong1(this.imaginary)
    }

    fun minus(other: ComplexFloatSeparate, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real - other.real)
        wrapper.encodeLong1(this.imaginary - other.imaginary)
    }

    fun minus(other: Float, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real - other)
        wrapper.encodeLong1(this.imaginary)
    }
}

private fun getI(wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(0.0f)
    wrapper.encodeLong1(1.0f)
}

private fun Float.times(complex: ComplexFloatSeparate, wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this * complex.real)
    wrapper.encodeLong1(this * complex.imaginary)
}

private fun Float.plus(complex: ComplexFloatSeparate, wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this + complex.real)
    wrapper.encodeLong1(complex.imaginary)
}

private fun Float.getComplex(wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this)
    wrapper.encodeLong1(0.0f)
}

private inline fun MutableMfvcWrapper.decodeComplexFloat() = ComplexFloatSeparate(decodeFloat0(), decodeFloat1())

fun ackermann(a: ComplexFloatSeparate, b: ComplexFloatSeparate, n: Float, wrapper: MutableMfvcWrapper): Unit = when {
    n == 0.0f -> a.plus(b, wrapper)
    b.real == 0.0f -> when (val newN = n - 1.0f) {
        0.0f, 1.0f -> newN.getComplex(wrapper)
        else -> {
            wrapper.encodeLong0(a.real)
            wrapper.encodeLong1(a.imaginary)
        }
    }

    else -> {
        b.minus(1.0f, wrapper)
        ackermann(a, wrapper.decodeComplexFloat(), n, wrapper)
        ackermann(a, wrapper.decodeComplexFloat(), n - 1.0f, wrapper)
    }
}

fun heavyActionFloatSeparate(complexConsumer: ComplexNumberConsumer) {
    val wrapper = MutableMfvcWrapper()
    with(wrapper) {
        for (n in 0..2) {
            for (x in 0..5) {
                val a = run {
                    val arg = run {
                        2.0f.times(run { getI(wrapper); decodeComplexFloat() }, wrapper)
                        decodeComplexFloat()
                    }
                    x.toFloat().plus(arg, wrapper)
                    decodeComplexFloat()
                }
                val b = run { x.toFloat().getComplex(wrapper); decodeComplexFloat() }
                ackermann(a, b, n.toFloat(), wrapper)
                decodeComplexFloat().let { complexConsumer.consume(it.real, it.imaginary) }
            }
        }
    }
}

fun main() {
    heavyActionFloatSeparate(ComplexNumberPrinter)
}
