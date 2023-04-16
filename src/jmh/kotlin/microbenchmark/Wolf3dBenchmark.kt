@file:Suppress("unused")

package microbenchmark

import AbstractGraphics
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
open class Wolf3dBenchmark {
    private fun makeGraphics(bh: Blackhole) = object : AbstractGraphics {
        override fun setIntColor(value: Int) {
            bh.consume(value)
        }

        override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
            bh.consume(x1)
            bh.consume(y1)
            bh.consume(x2)
            bh.consume(y2)
        }
    }
    
    
    @Benchmark
    fun baselineFloat(bh: Blackhole) = baseline.rendering.heavyActionF(makeGraphics(bh))

    @Benchmark
    fun baselineDouble(bh: Blackhole) = baseline.rendering.heavyActionD(makeGraphics(bh))

    
    @Benchmark
    fun inlineFloat(bh: Blackhole) = inline.rendering.heavyActionF(makeGraphics(bh))

    @Benchmark
    fun inlineDouble(bh: Blackhole) = inline.rendering.heavyActionD(makeGraphics(bh))


    @Benchmark
    fun longPackFloat(bh: Blackhole) = long_pack.rendering.heavyActionF(makeGraphics(bh))

    
    @Benchmark
    fun mutableRefFloatIn2Longs(bh: Blackhole) = mutable_ref.rendering.heavyActionFSeparate(makeGraphics(bh))

    @Benchmark
    fun mutableRefFloatIn1Long(bh: Blackhole) = mutable_ref.rendering.heavyActionFSame(makeGraphics(bh))

    @Benchmark
    fun mutableRefDouble(bh: Blackhole) = mutable_ref.rendering.heavyActionD(makeGraphics(bh))

    
    @Benchmark
    fun valueFloat(bh: Blackhole) = value.rendering.heavyActionF(makeGraphics(bh))

    @Benchmark
    fun valueDouble(bh: Blackhole) = value.rendering.heavyActionD(makeGraphics(bh))

    
    @Benchmark
    fun valueInlineFloat(bh: Blackhole) = value_inline.rendering.heavyActionF(makeGraphics(bh))

    @Benchmark
    fun valueInlineDouble(bh: Blackhole) = value_inline.rendering.heavyActionD(makeGraphics(bh))

    
    @Benchmark
    fun valuePreserveBoxFloat(bh: Blackhole) = value_preserve_box.rendering.heavyActionF(makeGraphics(bh))

    @Benchmark
    fun valuePreserveBoxDouble(bh: Blackhole) = value_preserve_box.rendering.heavyActionD(makeGraphics(bh))
}
