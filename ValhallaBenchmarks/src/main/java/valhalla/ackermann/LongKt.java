package valhalla.ackermann;

import shared.ComplexNumberConsumer;
import shared.ComplexNumberPrinter;

import static valhalla.ackermann.ComplexLong.*;
value record ComplexLong(long real, long imaginary) {
    @Override
    public String toString() {
        return imaginary == 0L ? Long.toString(real)
                : real == 0L ? (imaginary == 1L ? "i" : imaginary == -1L ? "-i" : imaginary + "i") 
                : (imaginary == 1L ? real + "+i" : imaginary == -1L ? real + "-i" : real + (imaginary < 0L ? "" : "-") + imaginary + "i");
    }

    public ComplexLong plus(ComplexLong other) {
        return new ComplexLong(this.real + other.real, this.imaginary + other.imaginary);
    }

    public ComplexLong plus(long other) {
        return new ComplexLong(this.real + other, this.imaginary);
    }

    public ComplexLong minus(ComplexLong other) {
        return new ComplexLong(this.real - other.real, this.imaginary - other.imaginary);
    }

    public ComplexLong minus(long other) {
        return new ComplexLong(this.real - other, this.imaginary);
    }

    public static ComplexLong i() {
        return new ComplexLong(0L, 1L);
    }

    public static ComplexLong times(long d, ComplexLong complex) {
        return new ComplexLong(d * complex.real, d * complex.imaginary);
    }

    public static ComplexLong plus(long d, ComplexLong complex) {
        return new ComplexLong(d + complex.real, complex.imaginary);
    }

    public static ComplexLong complex(long d) {
        return new ComplexLong(d, 0L);
    }
}

public class LongKt {
    static ComplexLong ackermann(ComplexLong a, ComplexLong b, long n) {
        if (n == 0L) return a.plus(b);
        if (b.real() == 0L) {
            final var newN = n - 1L;
            return newN == 0L || newN == 1L ? complex(newN) : a;
        }
        return ackermann(a, ackermann(a, b.minus(1L), n), n - 1L);
    }
    public static void heavyActionLong(ComplexNumberConsumer complexConsumer) {
        for(int n = 0; n <= 2; n++) {
            for(int x = 0; x <= 5; x++) {
                final var result = ackermann(plus(x, times(2L, i())), complex(x), n);
                complexConsumer.consume(result.real(), result.imaginary());
            }
        }
    }
    public static void main(String[] args) {
        heavyActionLong(ComplexNumberPrinter.INSTANCE);
    }
}
