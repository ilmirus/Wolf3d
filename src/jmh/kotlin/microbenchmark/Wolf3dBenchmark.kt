@file:Suppress("unused")

package microbenchmark

import shared.AbstractGraphics
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
    fun baselineFloat(bh: Blackhole) = baseline.rendering.heavyActionFloat(makeGraphics(bh))

    @Benchmark
    fun baselineDouble(bh: Blackhole) = baseline.rendering.heavyActionDouble(makeGraphics(bh))

    
    @Benchmark
    fun inlineFloat(bh: Blackhole) = inline.rendering.heavyActionFloat(makeGraphics(bh))

    @Benchmark
    fun inlineDouble(bh: Blackhole) = inline.rendering.heavyActionDouble(makeGraphics(bh))


    @Benchmark
    fun longPackFloat(bh: Blackhole) = long_pack.rendering.heavyActionFloat(makeGraphics(bh))

    
    @Benchmark
    fun mutableUniversalRefFloatIn2Longs(bh: Blackhole) = mutable_ref_universal.rendering.heavyActionFloatSeparate(makeGraphics(bh))

    @Benchmark
    fun mutableUniversalRefFloatIn1Long(bh: Blackhole) = mutable_ref_universal.rendering.heavyActionFloatSame(makeGraphics(bh))

    @Benchmark
    fun mutableUniversalRefDouble(bh: Blackhole) = mutable_ref_universal.rendering.heavyActionDouble(makeGraphics(bh))
    

    @Benchmark
    fun mutableSpecificRefFloat(bh: Blackhole) = mutable_ref_specific.rendering.heavyActionFloat(makeGraphics(bh))

    @Benchmark
    fun mutableSpecificRefDouble(bh: Blackhole) = mutable_ref_specific.rendering.heavyActionDouble(makeGraphics(bh))

    
    @Benchmark
    fun valueFloat(bh: Blackhole) = value.rendering.heavyActionFloat(makeGraphics(bh))

    @Benchmark
    fun valueDouble(bh: Blackhole) = value.rendering.heavyActionDouble(makeGraphics(bh))

    
    @Benchmark
    fun valueInlineFloat(bh: Blackhole) = value_inline.rendering.heavyActionFloat(makeGraphics(bh))

    @Benchmark
    fun valueInlineDouble(bh: Blackhole) = value_inline.rendering.heavyActionDouble(makeGraphics(bh))

    
    @Benchmark
    fun valuePreserveBoxFloat(bh: Blackhole) = value_preserve_box.rendering.heavyActionFloat(makeGraphics(bh))

    @Benchmark
    fun valuePreserveBoxDouble(bh: Blackhole) = value_preserve_box.rendering.heavyActionDouble(makeGraphics(bh))
}
