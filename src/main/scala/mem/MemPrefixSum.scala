// See README.md for license details.

package mem

import chisel3._
import chisel3.util._

// /**
//   * Compute GCD using subtraction method.
//   * Subtracts the smaller from the larger until register y is zero.
//   * value in register x is then the GCD
//   */
// class GCD extends Module {
//   val io = IO(new Bundle {
//     val value1        = Input(UInt(16.W))
//     val value2        = Input(UInt(16.W))
//     val loadingValues = Input(Bool())
//     val outputGCD     = Output(UInt(16.W))
//     val outputValid   = Output(Bool())
//   })

//   val x  = Reg(UInt())
//   val y  = Reg(UInt())

//   when(x > y) { x := x - y }
//     .otherwise { y := y - x }

//   when(io.loadingValues) {
//     x := io.value1
//     y := io.value2
//   }

//   io.outputGCD := x
//   io.outputValid := y === 0.U
// }


class MemPrefixSum(n: Int) extends Module {
  val io = IO(new Bundle {
    val enable = Input(Bool())
    val value = Input(UInt(16.W))
    val prefixSum = Output(UInt(16.W)) 
    val done = Output(Bool())
  })

  val mem = SyncReadMem(n, UInt(16.W)) 
  
  val state_idle :: state_input :: state_output :: state_done :: Nil = Enum(4)
  val cnt = RegInit(0.U(log2Ceil(n+1).W))
  val state = RegInit(state_idle)
  print(s"state reg has width: ${state.getWidth}\n")
  print(s"cnt ref has width: ${cnt.getWidth}\n")

  val accumReg = RegInit(0.U(16.W))
  val doneReg = RegInit(false.B)
  val rdata = WireInit(0.U(16.W))

  rdata := mem.read(cnt, state === state_output)

  io.prefixSum := accumReg + rdata
  io.done := doneReg

  switch(state) {
    is (state_idle) {
      when (io.enable) {
        state := state_input
        cnt := 0.U
      }
    }
    is (state_input) {
      mem.write(cnt, io.value)
      cnt := cnt + 1.U
      when (cnt+1.U === n.U) {
        state := state_output
        cnt := 0.U
      } .otherwise {
        state := state_input
      }
    }
    is (state_output) {
      when (cnt =/= 0.U) {
        accumReg := accumReg + rdata
      }
      cnt := cnt + 1.U
      when (cnt+1.U === n.U) {
        state := state_idle
        doneReg := true.B
        cnt := 0.U
      } otherwise {
        state := state_output
      }
    }
  }

  printf(cf"state = ${state}, cnt = ${cnt}\n")

}
