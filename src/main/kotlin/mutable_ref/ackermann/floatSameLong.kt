package mutable_ref.ackermann

import mutable_ref.MutableMfvcWrapper
import ComplexNumberConsumer
import ComplexNumberPrinter

@JvmInline
value class ComplexFloatSame(val real: Float, val imaginary: Float) {
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

    fun plus(other: ComplexFloatSame, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real + other.real, this.imaginary + other.imaginary)
    }

    fun plus(other: Float, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real + other, this.imaginary)
    }

    fun minus(other: ComplexFloatSame, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real - other.real, this.imaginary - other.imaginary)
    }

    fun minus(other: Float, wrapper: MutableMfvcWrapper) {
        wrapper.encodeLong0(this.real - other, this.imaginary)
    }
}

private fun getI(wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(0.0f, 1.0f)
}

private fun Float.times(complex: ComplexFloatSame, wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this * complex.real, this * complex.imaginary)
}

private fun Float.plus(complex: ComplexFloatSame, wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this + complex.real, complex.imaginary)
}

private fun Float.getComplex(wrapper: MutableMfvcWrapper) {
    wrapper.encodeLong0(this, 0.0f)
}

private inline fun MutableMfvcWrapper.decodeComplexFloat() = ComplexFloatSame(decodeFloat00(), decodeFloat01())

fun ackermann(a: ComplexFloatSame, b: ComplexFloatSame, n: Float, wrapper: MutableMfvcWrapper): Unit = when {
    n == 0.0f -> a.plus(b, wrapper)
    b.real == 0.0f -> when (val newN = n - 1.0f) {
        0.0f, 1.0f -> newN.getComplex(wrapper)
        else -> wrapper.encodeLong0(a.real, a.imaginary)
    }

    else -> {
        b.minus(1.0f, wrapper)
        ackermann(a, wrapper.decodeComplexFloat(), n, wrapper)
        ackermann(a, wrapper.decodeComplexFloat(), n - 1.0f, wrapper)
    }
}

fun heavyActionFloatSame(complexConsumer: ComplexNumberConsumer) {
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
    heavyActionFloatSame(ComplexNumberPrinter)
}
