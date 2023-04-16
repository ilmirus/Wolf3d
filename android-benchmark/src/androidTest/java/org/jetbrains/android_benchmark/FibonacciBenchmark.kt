package org.jetbrains.android_benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import fibonacciInputInt
import fibonacciInputLong

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class FibonacciBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun baselineInt() = benchmarkRule.measureRepeated {
        baseline.fibonacci.fibonacci(fibonacciInputInt)
    }

    @Test
    fun baselineLong() = benchmarkRule.measureRepeated {
        baseline.fibonacci.fibonacci(fibonacciInputLong)
    }


    @Test
    fun longPackInt() = benchmarkRule.measureRepeated {
        long_pack.fibonacci.fibonacci(fibonacciInputInt)
    }


    @Test
    fun mutableRefIntIn2Longs() = benchmarkRule.measureRepeated {
        mutable_ref.fibonacci.fibonacciSeparate(fibonacciInputInt)
    }

    @Test
    fun mutableRefIntIn1Long() = benchmarkRule.measureRepeated {
        mutable_ref.fibonacci.fibonacciSame(fibonacciInputInt)
    }

    @Test
    fun mutableRefLong() = benchmarkRule.measureRepeated {
        mutable_ref.fibonacci.fibonacci(fibonacciInputLong)
    }


    @Test
    fun valueInt() = benchmarkRule.measureRepeated {
        value.fibonacci.fibonacci(fibonacciInputInt)
    }

    @Test
    fun valueLong() = benchmarkRule.measureRepeated {
        value.fibonacci.fibonacci(fibonacciInputLong)
    }

    @Test
    fun valuePreserveBoxInt() = benchmarkRule.measureRepeated {
        value_preserve_box.fibonacci.fibonacci(fibonacciInputInt)
    }

    @Test
    fun valuePreserveBoxLong() = benchmarkRule.measureRepeated {
        value_preserve_box.fibonacci.fibonacci(fibonacciInputLong)
    }
}
