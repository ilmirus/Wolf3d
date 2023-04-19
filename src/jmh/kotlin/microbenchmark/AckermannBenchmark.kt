@file:Suppress("unused")

package microbenchmark

import ComplexNumberConsumer
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
open class AckermannBenchmark {
    
    private class JmhComplexNumberConsumer(private val bh: Blackhole): ComplexNumberConsumer {
        override fun consume(real: Int, imaginary: Int) {
            bh.consume(real)
            bh.consume(imaginary)
        }

        override fun consume(real: Long, imaginary: Long) {
            bh.consume(real)
            bh.consume(imaginary)
        }
    }
    
    
    @Benchmark
    fun baselineInt(bh: Blackhole) = baseline.ackermann.heavyActionInt(JmhComplexNumberConsumer(bh))

    @Benchmark
    fun baselineLong(bh: Blackhole) = baseline.ackermann.heavyActionLong(JmhComplexNumberConsumer(bh))


    @Benchmark
    fun longPackInt(bh: Blackhole) = long_pack.ackermann.heavyActionInt(JmhComplexNumberConsumer(bh))

    
    @Benchmark
    fun mutableRefIntIn2Longs(bh: Blackhole) = mutable_ref.ackermann.heavyActionIntSeparate(JmhComplexNumberConsumer(bh))

    @Benchmark
    fun mutableRefIntIn1Long(bh: Blackhole) = mutable_ref.ackermann.heavyActionIntSame(JmhComplexNumberConsumer(bh))

    @Benchmark
    fun mutableRefLong(bh: Blackhole) = mutable_ref.ackermann.heavyActionLong(JmhComplexNumberConsumer(bh))

    
    @Benchmark
    fun valueInt(bh: Blackhole) = value.ackermann.heavyActionInt(JmhComplexNumberConsumer(bh))

    @Benchmark
    fun valueLong(bh: Blackhole) = value.ackermann.heavyActionLong(JmhComplexNumberConsumer(bh))

    
    @Benchmark
    fun valuePreserveBoxInt(bh: Blackhole) = value_preserve_box.ackermann.heavyActionInt(JmhComplexNumberConsumer(bh))

    @Benchmark
    fun valuePreserveBoxLong(bh: Blackhole) = value_preserve_box.ackermann.heavyActionLong(JmhComplexNumberConsumer(bh))
}
