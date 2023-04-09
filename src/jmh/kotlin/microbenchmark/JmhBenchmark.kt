@file:Suppress("unused")

package microbenchmark

import AbstractGraphics
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.awt.Color
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
open class JmhBenchmark {
    private fun makeGraphics(bh: Blackhole) = object : AbstractGraphics {
        override var color: Any
            get() = error("Stub color")
            set(value) {
                bh.consume(value)
            }

        override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
            bh.consume(x1)
            bh.consume(y1)
            bh.consume(x2)
            bh.consume(y2)
        }

        override fun makeColor(value: Int): Any = Color(value)
    }
    
    
    @Benchmark
    fun baselineFloat(bh: Blackhole) = baseline.heavyActionF(makeGraphics(bh))

    @Benchmark
    fun baselineDouble(bh: Blackhole) = baseline.heavyActionD(makeGraphics(bh))

    
    @Benchmark
    fun inlineFloat(bh: Blackhole) = inline.heavyActionF(makeGraphics(bh))

    @Benchmark
    fun inlineDouble(bh: Blackhole) = inline.heavyActionD(makeGraphics(bh))


    @Benchmark
    fun longPackFloat(bh: Blackhole) = long_pack.heavyActionF(makeGraphics(bh))

    
    @Benchmark
    fun mutableRefFloatIn2Longs(bh: Blackhole) = mutable_ref.heavyActionF1(makeGraphics(bh))

    @Benchmark
    fun mutableRefFloatIn1Long(bh: Blackhole) = mutable_ref.heavyActionF2(makeGraphics(bh))

    @Benchmark
    fun mutableRefDouble(bh: Blackhole) = mutable_ref.heavyActionD(makeGraphics(bh))

    
    @Benchmark
    fun valueFloat(bh: Blackhole) = value.heavyActionF(makeGraphics(bh))

    @Benchmark
    fun valueDouble(bh: Blackhole) = value.heavyActionD(makeGraphics(bh))

    
    @Benchmark
    fun valueInlineFloat(bh: Blackhole) = value_inline.heavyActionF(makeGraphics(bh))

    @Benchmark
    fun valueInlineDouble(bh: Blackhole) = value_inline.heavyActionD(makeGraphics(bh))
}
