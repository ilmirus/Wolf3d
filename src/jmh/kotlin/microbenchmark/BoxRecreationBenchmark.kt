@file:Suppress("unused")

package microbenchmark

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
open class BoxRecreationBenchmark {
    @Benchmark
    fun baselineInt(bh: Blackhole) = baseline.box_recreation.heavyActionInt(bh::consume)

    @Benchmark
    fun baselineFloat(bh: Blackhole) = baseline.box_recreation.heavyActionFloat(bh::consume)

    @Benchmark
    fun baselineLong(bh: Blackhole) = baseline.box_recreation.heavyActionLong(bh::consume)

    @Benchmark
    fun baselineDouble(bh: Blackhole) = baseline.box_recreation.heavyActionDouble(bh::consume)


    @Benchmark
    fun longPackInt(bh: Blackhole) = long_pack.box_recreation.heavyActionInt(bh::consume)

    @Benchmark
    fun longPackFloat(bh: Blackhole) = long_pack.box_recreation.heavyActionFloat(bh::consume)

    
    @Benchmark
    fun valueInt(bh: Blackhole) = value.box_recreation.heavyActionInt(bh::consume)

    @Benchmark
    fun valueFloat(bh: Blackhole) = value.box_recreation.heavyActionFloat(bh::consume)

    @Benchmark
    fun valueLong(bh: Blackhole) = value.box_recreation.heavyActionLong(bh::consume)

    @Benchmark
    fun valueDouble(bh: Blackhole) = value.box_recreation.heavyActionDouble(bh::consume)

    
    @Benchmark
    fun valuePreserveBoxInt(bh: Blackhole) = value_preserve_box.box_recreation.heavyActionInt(bh::consume)

    @Benchmark
    fun valuePreserveBoxFloat(bh: Blackhole) = value_preserve_box.box_recreation.heavyActionFloat(bh::consume)

    @Benchmark
    fun valuePreserveBoxLong(bh: Blackhole) = value_preserve_box.box_recreation.heavyActionLong(bh::consume)

    @Benchmark
    fun valuePreserveBoxDouble(bh: Blackhole) = value_preserve_box.box_recreation.heavyActionDouble(bh::consume)
}
