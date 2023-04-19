package org.jetbrains.android_benchmark

import AbstractGraphics
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class Wolf3dBenchmark {
    
    private object Graphics : AbstractGraphics {
        private var color: Int=  0
        private var x1: Int = 0
        private var y1: Int = 0
        private var x2: Int = 0
        private var y2: Int = 0

        override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
            this.x1 = x1
            this.y1 = y1
            this.x2 = x2
            this.y2 = y2
        }

        override fun setIntColor(value: Int) {
            color = value
        }
    }

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun baselineFloat() = benchmarkRule.measureRepeated {
        baseline.rendering.heavyActionFloat(Graphics)
    }

    @Test
    fun baselineDouble() = benchmarkRule.measureRepeated {
        baseline.rendering.heavyActionDouble(Graphics)
    }


    @Test
    fun inlineFloat() = benchmarkRule.measureRepeated {
        inline.rendering.heavyActionFloat(Graphics)
    }

    @Test
    fun inlineDouble() = benchmarkRule.measureRepeated {
        inline.rendering.heavyActionDouble(Graphics)
    }


    @Test
    fun longPackFloat() = benchmarkRule.measureRepeated {
        long_pack.rendering.heavyActionFloat(Graphics)
    }


    @Test
    fun mutableRefFloatIn2Longs() = benchmarkRule.measureRepeated {
        mutable_ref.rendering.heavyActionFloatSeparate(Graphics)
    }

    @Test
    fun mutableRefFloatIn1Long() = benchmarkRule.measureRepeated {
        mutable_ref.rendering.heavyActionFloatSame(Graphics)
    }

    @Test
    fun mutableRefDouble() = benchmarkRule.measureRepeated {
        mutable_ref.rendering.heavyActionDouble(Graphics)
    }


    @Test
    fun valueFloat() = benchmarkRule.measureRepeated {
        value.rendering.heavyActionFloat(Graphics)
    }

    @Test
    fun valueDouble() = benchmarkRule.measureRepeated {
        value.rendering.heavyActionDouble(Graphics)
    }


    @Test
    fun valueInlineFloat() = benchmarkRule.measureRepeated {
        value_inline.rendering.heavyActionFloat(Graphics)
    }

    @Test
    fun valueInlineDouble() = benchmarkRule.measureRepeated {
        value_inline.rendering.heavyActionDouble(Graphics)
    }


    @Test
    fun valuePreserveBoxFloat() = benchmarkRule.measureRepeated {
        value_preserve_box.rendering.heavyActionFloat(Graphics)
    }

    @Test
    fun valuePreserveBoxDouble() = benchmarkRule.measureRepeated {
        value_preserve_box.rendering.heavyActionDouble(Graphics)
    }
}
