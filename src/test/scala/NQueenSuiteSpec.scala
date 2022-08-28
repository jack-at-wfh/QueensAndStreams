import zio.*
import zio.test.*

object NQueenSuiteSpec extends ZIOSpecDefault {
  val testCorrectResult1: StreamingNQueens.BOARD = IndexedSeq((1, 3), (2, 1), (3, 4), (4, 2))
  val testCorrectResult2: StreamingNQueens.BOARD = IndexedSeq((1, 2), (2, 4), (3, 1), (4, 3))
  val testIncorrectResult1: StreamingNQueens.BOARD = IndexedSeq((1, 1), (2, 2), (3, 3), (4, 4))
  def spec = suite("Full suite of Tests for nQueens problem")(
    test("Simple correct result [3,1,4,2] for nQueens(4)") {
      assertTrue(StreamingNQueens.isSafe(testCorrectResult1))
    },
    test("Simple correct result [2,4,1,3] for nQueens(4)") {
      assertTrue(StreamingNQueens.isSafe(testCorrectResult2))
    },
    test ("Simple incorrect result [1,2,3,4] for nQueens(4)") {
      !assertTrue(StreamingNQueens.isSafe(testIncorrectResult1))
    }
  )
}