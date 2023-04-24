package org.jetbrains.android_benchmark

import ObjectConsumer
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
class BoxRecreationBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()
    
    private object SimpleObjectConsumer : ObjectConsumer {
        var o: Any? = null

        override fun consume(o: Any?) {
            this.o = o
        }
    }

    @Test
    fun baselineInt() = benchmarkRule.measureRepeated {
        baseline.box_recreation.heavyActionInt(SimpleObjectConsumer)
    }

    @Test
    fun baselineFloat() = benchmarkRule.measureRepeated {
        baseline.box_recreation.heavyActionFloat(SimpleObjectConsumer)
    }

    @Test
    fun baselineLong() = benchmarkRule.measureRepeated {
        baseline.box_recreation.heavyActionLong(SimpleObjectConsumer)
    }

    @Test
    fun baselineDouble() = benchmarkRule.measureRepeated {
        baseline.box_recreation.heavyActionDouble(SimpleObjectConsumer)
    }


    @Test
    fun longPackInt() = benchmarkRule.measureRepeated {
        long_pack.box_recreation.heavyActionInt(SimpleObjectConsumer)
    }

    @Test
    fun longPackFloat() = benchmarkRule.measureRepeated {
        long_pack.box_recreation.heavyActionFloat(SimpleObjectConsumer)
    }


    @Test
    fun valueInt() = benchmarkRule.measureRepeated {
        value.box_recreation.heavyActionInt(SimpleObjectConsumer)
    }

    @Test
    fun valueFloat() = benchmarkRule.measureRepeated {
        value.box_recreation.heavyActionFloat(SimpleObjectConsumer)
    }

    @Test
    fun valueLong() = benchmarkRule.measureRepeated {
        value.box_recreation.heavyActionLong(SimpleObjectConsumer)
    }

    @Test
    fun valueDouble() = benchmarkRule.measureRepeated {
        value.box_recreation.heavyActionDouble(SimpleObjectConsumer)
    }


    @Test
    fun valuePreserveBoxInt() = benchmarkRule.measureRepeated {
        value_preserve_box.box_recreation.heavyActionInt(SimpleObjectConsumer)
    }

    @Test
    fun valuePreserveBoxFloat() = benchmarkRule.measureRepeated {
        value_preserve_box.box_recreation.heavyActionFloat(SimpleObjectConsumer)
    }

    @Test
    fun valuePreserveBoxLong() = benchmarkRule.measureRepeated {
        value_preserve_box.box_recreation.heavyActionLong(SimpleObjectConsumer)
    }

    @Test
    fun valuePreserveBoxDouble() = benchmarkRule.measureRepeated {
        value_preserve_box.box_recreation.heavyActionDouble(SimpleObjectConsumer)
    }
}
