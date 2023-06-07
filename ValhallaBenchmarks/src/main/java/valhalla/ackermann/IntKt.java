package valhalla.ackermann;

import shared.ComplexNumberConsumer;
import shared.ComplexNumberPrinter;

import static valhalla.ackermann.ComplexInt.*;
value record ComplexInt(int real, int imaginary) {
    @Override
    public String toString() {
        return imaginary == 0 ? Integer.toString(real)
                : real == 0 ? (imaginary == 1 ? "i" : imaginary == -1 ? "-i" : imaginary + "i") 
                : (imaginary == 1 ? real + "+i" : imaginary == -1 ? real + "-i" : real + (imaginary < 0 ? "" : "-") + imaginary + "i");
    }

    public ComplexInt plus(ComplexInt other) {
        return new ComplexInt(this.real + other.real, this.imaginary + other.imaginary);
    }

    public ComplexInt plus(int other) {
        return new ComplexInt(this.real + other, this.imaginary);
    }

    public ComplexInt minus(ComplexInt other) {
        return new ComplexInt(this.real - other.real, this.imaginary - other.imaginary);
    }

    public ComplexInt minus(int other) {
        return new ComplexInt(this.real - other, this.imaginary);
    }

    public static ComplexInt i() {
        return new ComplexInt(0, 1);
    }

    public static ComplexInt times(int d, ComplexInt complex) {
        return new ComplexInt(d * complex.real, d * complex.imaginary);
    }

    public static ComplexInt plus(int d, ComplexInt complex) {
        return new ComplexInt(d + complex.real, complex.imaginary);
    }

    public static ComplexInt complex(int d) {
        return new ComplexInt(d, 0);
    }
}

public class IntKt {
    static ComplexInt ackermann(ComplexInt a, ComplexInt b, int n) {
        if (n == 0) return a.plus(b);
        if (b.real() == 0) {
            final var newN = n - 1;
            return newN == 0 || newN == 1 ? complex(newN) : a;
        }
        return ackermann(a, ackermann(a, b.minus(1), n), n - 1);
    }
    public static void heavyActionInt(ComplexNumberConsumer complexConsumer) {
        for(int n = 0; n <= 2; n++) {
            for(int x = 0; x <= 5; x++) {
                final var result = ackermann(plus(x, times(2, i())), complex(x), n);
                complexConsumer.consume(result.real(), result.imaginary());
            }
        }
    }
    public static void main(String[] args) {
        heavyActionInt(ComplexNumberPrinter.INSTANCE);
    }
}
