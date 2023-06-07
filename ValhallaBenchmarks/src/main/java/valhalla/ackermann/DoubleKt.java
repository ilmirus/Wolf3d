package valhalla.ackermann;

import shared.ComplexNumberConsumer;
import shared.ComplexNumberPrinter;

import static valhalla.ackermann.ComplexDouble.complex;
import static valhalla.ackermann.ComplexDouble.plus;
import static valhalla.ackermann.ComplexDouble.times;
import static valhalla.ackermann.ComplexDouble.i;
value record ComplexDouble(double real, double imaginary) {
    @Override
    public String toString() {
        return imaginary == 0.0 ? Double.toString(real)
                : real == 0.0 ? (imaginary == 1.0 ? "i" : imaginary == -1.0 ? "-i" : imaginary + "i") 
                : (imaginary == 1.0 ? real + "+i" : imaginary == -1.0 ? real + "-i" : real + (imaginary < 0.0 ? "" : "-") + imaginary + "i");
    }

    public ComplexDouble plus(ComplexDouble other) {
        return new ComplexDouble(this.real + other.real, this.imaginary + other.imaginary);
    }

    public ComplexDouble plus(double other) {
        return new ComplexDouble(this.real + other, this.imaginary);
    }

    public ComplexDouble minus(ComplexDouble other) {
        return new ComplexDouble(this.real - other.real, this.imaginary - other.imaginary);
    }

    public ComplexDouble minus(double other) {
        return new ComplexDouble(this.real - other, this.imaginary);
    }

    public static ComplexDouble i() {
        return new ComplexDouble(0.0, 1.0);
    }

    public static ComplexDouble times(double d, ComplexDouble complex) {
        return new ComplexDouble(d * complex.real, d * complex.imaginary);
    }

    public static ComplexDouble plus(double d, ComplexDouble complex) {
        return new ComplexDouble(d + complex.real, complex.imaginary);
    }

    public static ComplexDouble complex(double d) {
        return new ComplexDouble(d, 0.0);
    }
}

public class DoubleKt {
    static ComplexDouble ackermann(ComplexDouble a, ComplexDouble b, double n) {
        if (n == 0.0) return a.plus(b);
        if (b.real() == 0.0) {
            final var newN = n - 1.0;
            return newN == 0.0 || newN == 1.0 ? complex(newN) : a;
        }
        return ackermann(a, ackermann(a, b.minus(1.0), n), n - 1.0);
    }
    public static void heavyActionDouble(ComplexNumberConsumer complexConsumer) {
        for(int n = 0; n <= 2; n++) {
            for(int x = 0; x <= 5; x++) {
                final var result = ackermann(plus(x, times(2.0, i())), complex(x), n);
                complexConsumer.consume(result.real(), result.imaginary());
            }
        }
    }
    public static void main(String[] args) {
        heavyActionDouble(ComplexNumberPrinter.INSTANCE);
    }
}
