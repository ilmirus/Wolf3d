@file:Suppress("unused")

package microbenchmark

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import fibonacciInputInt
import fibonacciInputLong

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
open class FibonacciBenchmark {
    @Benchmark
    fun baselineInt(bh: Blackhole) = bh.consume(baseline.fibonacci.fibonacci(fibonacciInputInt))

    @Benchmark
    fun baselineLong(bh: Blackhole) = bh.consume(baseline.fibonacci.fibonacci(fibonacciInputLong))


    @Benchmark
    fun longPackInt(bh: Blackhole) = bh.consume(long_pack.fibonacci.fibonacci(fibonacciInputInt))

    
    @Benchmark
    fun mutableRefIntIn2Longs(bh: Blackhole) = bh.consume(mutable_ref.fibonacci.fibonacciSeparate(fibonacciInputInt))

    @Benchmark
    fun mutableRefIntIn1Long(bh: Blackhole) = bh.consume(mutable_ref.fibonacci.fibonacciSame(fibonacciInputInt))

    @Benchmark
    fun mutableRefLong(bh: Blackhole) = bh.consume(mutable_ref.fibonacci.fibonacci(fibonacciInputLong))

    
    @Benchmark
    fun valueInt(bh: Blackhole) = bh.consume(value.fibonacci.fibonacci(fibonacciInputInt))

    @Benchmark
    fun valueLong(bh: Blackhole) = bh.consume(value.fibonacci.fibonacci(fibonacciInputLong))

    
    @Benchmark
    fun valuePreserveBoxInt(bh: Blackhole) = bh.consume(value_preserve_box.fibonacci.fibonacci(fibonacciInputInt))

    @Benchmark
    fun valuePreserveBoxLong(bh: Blackhole) = bh.consume(value_preserve_box.fibonacci.fibonacci(fibonacciInputLong))
}
