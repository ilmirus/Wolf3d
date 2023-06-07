package valhalla.ackermann;

import shared.ComplexNumberConsumer;
import shared.ComplexNumberPrinter;

import static valhalla.ackermann.ComplexFloat.*;
value record ComplexFloat(float real, float imaginary) {
    @Override
    public String toString() {
        return imaginary == 0.0f ? Float.toString(real)
                : real == 0.0f ? (imaginary == 1.0f ? "i" : imaginary == -1.0f ? "-i" : imaginary + "i") 
                : (imaginary == 1.0f ? real + "+i" : imaginary == -1.0f ? real + "-i" : real + (imaginary < 0.0f ? "" : "-") + imaginary + "i");
    }

    public ComplexFloat plus(ComplexFloat other) {
        return new ComplexFloat(this.real + other.real, this.imaginary + other.imaginary);
    }

    public ComplexFloat plus(float other) {
        return new ComplexFloat(this.real + other, this.imaginary);
    }

    public ComplexFloat minus(ComplexFloat other) {
        return new ComplexFloat(this.real - other.real, this.imaginary - other.imaginary);
    }

    public ComplexFloat minus(float other) {
        return new ComplexFloat(this.real - other, this.imaginary);
    }

    public static ComplexFloat i() {
        return new ComplexFloat(0.0f, 1.0f);
    }

    public static ComplexFloat times(float d, ComplexFloat complex) {
        return new ComplexFloat(d * complex.real, d * complex.imaginary);
    }

    public static ComplexFloat plus(float d, ComplexFloat complex) {
        return new ComplexFloat(d + complex.real, complex.imaginary);
    }

    public static ComplexFloat complex(float d) {
        return new ComplexFloat(d, 0.0f);
    }
}

public class FloatKt {
    static ComplexFloat ackermann(ComplexFloat a, ComplexFloat b, float n) {
        if (n == 0.0f) return a.plus(b);
        if (b.real() == 0.0f) {
            final var newN = n - 1.0f;
            return newN == 0.0f || newN == 1.0f ? complex(newN) : a;
        }
        return ackermann(a, ackermann(a, b.minus(1.0f), n), n - 1.0f);
    }
    public static void heavyActionFloat(ComplexNumberConsumer complexConsumer) {
        for(int n = 0; n <= 2; n++) {
            for(int x = 0; x <= 5; x++) {
                final var result = ackermann(plus(x, times(2.0f, i())), complex(x), n);
                complexConsumer.consume(result.real(), result.imaginary());
            }
        }
    }
    public static void main(String[] args) {
        heavyActionFloat(ComplexNumberPrinter.INSTANCE);
    }
}
