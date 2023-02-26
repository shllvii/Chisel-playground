// See README.md for license details.

package mem

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import chisel3.experimental.BundleLiterals._

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly gcd.GcdDecoupledTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly gcd.GcdDecoupledTester'
  * }}}
  */
class MemPrefixSumSpec extends AnyFreeSpec with ChiselScalatestTester {

  "MemPrefixSum should print a sequence of prefix sums" in {
    val n = 4
    test(new MemPrefixSum(n)) { dut =>
      val testValues = for (x <- 0 until n) yield x

      dut.io.enable.poke(true)
      dut.clock.step()

      for (i <- 0 until n) {
        dut.io.value.poke(i+1)
        dut.clock.step()
      }

      dut.clock.step()
      print(s"MemPrefixSum.mem = ${dut.mem.toString()}\n")

      var sum = 0
      for (i <- 0 until n) {
        sum = sum + i + 1
        dut.io.prefixSum.expect(sum)
        dut.clock.step()
      }

      dut.io.done.expect(true)


    }
  }
}
