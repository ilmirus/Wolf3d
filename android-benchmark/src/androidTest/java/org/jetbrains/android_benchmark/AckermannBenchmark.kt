package org.jetbrains.android_benchmark

import ComplexNumberConsumer
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
class AckermannBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()
    
    private object SimpleComplexNumberConsumer : ComplexNumberConsumer {
        var int1: Int = 0
        var int2: Int = 0
        var long1: Long = 0
        var long2: Long = 0
        var float1: Float = 0.0f
        var float2: Float = 0.0f
        var double1: Double = 0.0
        var double2: Double = 0.0
        
        override fun consume(real: Int, imaginary: Int) {
            int1 = real
            int2 = imaginary
        }

        override fun consume(real: Long, imaginary: Long) {
            long1 = real
            long2 = imaginary
        }

        override fun consume(real: Float, imaginary: Float) {
            float1 = real
            float2 = imaginary
        }

        override fun consume(real: Double, imaginary: Double) {
            double1 = real
            double2 = imaginary
        }
    }

    @Test
    fun baselineInt() = benchmarkRule.measureRepeated {
        baseline.ackermann.heavyActionInt(SimpleComplexNumberConsumer)
    }

    @Test
    fun baselineFloat() = benchmarkRule.measureRepeated {
        baseline.ackermann.heavyActionFloat(SimpleComplexNumberConsumer)
    }

    @Test
    fun baselineLong() = benchmarkRule.measureRepeated {
        baseline.ackermann.heavyActionLong(SimpleComplexNumberConsumer)
    }

    @Test
    fun baselineDouble() = benchmarkRule.measureRepeated {
        baseline.ackermann.heavyActionDouble(SimpleComplexNumberConsumer)
    }


    @Test
    fun longPackInt() = benchmarkRule.measureRepeated {
        long_pack.ackermann.heavyActionInt(SimpleComplexNumberConsumer)
    }

    @Test
    fun longPackFloat() = benchmarkRule.measureRepeated {
        long_pack.ackermann.heavyActionFloat(SimpleComplexNumberConsumer)
    }


    @Test
    fun mutableUniversalRefIntIn2Longs() = benchmarkRule.measureRepeated {
        mutable_ref_universal.ackermann.heavyActionIntSeparate(SimpleComplexNumberConsumer)
    }

    @Test
    fun mutableUniversalRefFloatIn2Longs() = benchmarkRule.measureRepeated {
        mutable_ref_universal.ackermann.heavyActionFloatSeparate(SimpleComplexNumberConsumer)
    }

    @Test
    fun mutableUniversalRefIntIn1Long() = benchmarkRule.measureRepeated {
        mutable_ref_universal.ackermann.heavyActionIntSame(SimpleComplexNumberConsumer)
    }

    @Test
    fun mutableUniversalRefFloatIn1Long() = benchmarkRule.measureRepeated {
        mutable_ref_universal.ackermann.heavyActionFloatSame(SimpleComplexNumberConsumer)
    }

    @Test
    fun mutableUniversalRefLong() = benchmarkRule.measureRepeated {
        mutable_ref_universal.ackermann.heavyActionLong(SimpleComplexNumberConsumer)
    }

    @Test
    fun mutableUniversalRefDouble() = benchmarkRule.measureRepeated {
        mutable_ref_universal.ackermann.heavyActionDouble(SimpleComplexNumberConsumer)
    }
    

    @Test
    fun mutableSpecificRefInt() = benchmarkRule.measureRepeated {
        mutable_ref_specific.ackermann.heavyActionInt(SimpleComplexNumberConsumer)
    }

    @Test
    fun mutableSpecificRefFloat() = benchmarkRule.measureRepeated {
        mutable_ref_specific.ackermann.heavyActionFloat(SimpleComplexNumberConsumer)
    }

    @Test
    fun mutableSpecificRefLong() = benchmarkRule.measureRepeated {
        mutable_ref_specific.ackermann.heavyActionLong(SimpleComplexNumberConsumer)
    }

    @Test
    fun mutableSpecificRefDouble() = benchmarkRule.measureRepeated {
        mutable_ref_specific.ackermann.heavyActionDouble(SimpleComplexNumberConsumer)
    }


    @Test
    fun valueInt() = benchmarkRule.measureRepeated {
        value.ackermann.heavyActionInt(SimpleComplexNumberConsumer)
    }

    @Test
    fun valueFloat() = benchmarkRule.measureRepeated {
        value.ackermann.heavyActionFloat(SimpleComplexNumberConsumer)
    }

    @Test
    fun valueLong() = benchmarkRule.measureRepeated {
        value.ackermann.heavyActionLong(SimpleComplexNumberConsumer)
    }

    @Test
    fun valueDouble() = benchmarkRule.measureRepeated {
        value.ackermann.heavyActionDouble(SimpleComplexNumberConsumer)
    }


    @Test
    fun valuePreserveBoxInt() = benchmarkRule.measureRepeated {
        value_preserve_box.ackermann.heavyActionInt(SimpleComplexNumberConsumer)
    }

    @Test
    fun valuePreserveBoxFloat() = benchmarkRule.measureRepeated {
        value_preserve_box.ackermann.heavyActionFloat(SimpleComplexNumberConsumer)
    }

    @Test
    fun valuePreserveBoxLong() = benchmarkRule.measureRepeated {
        value_preserve_box.ackermann.heavyActionLong(SimpleComplexNumberConsumer)
    }

    @Test
    fun valuePreserveBoxDouble() = benchmarkRule.measureRepeated {
        value_preserve_box.ackermann.heavyActionDouble(SimpleComplexNumberConsumer)
    }
}
