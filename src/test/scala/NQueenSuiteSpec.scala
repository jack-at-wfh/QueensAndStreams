import zio.*
import zio.test.{assert, *}

object NQueenSuiteSpec extends ZIOSpecDefault {
  implicit val chessBoard: Int = 4
  val testCorrectResult1: StreamingNQueens.BOARD = IndexedSeq((1, 3), (2, 1), (3, 4), (4, 2))
  val testCorrectResult2: StreamingNQueens.BOARD = IndexedSeq((1, 2), (2, 4), (3, 1), (4, 3))
  val testIncorrectResult1: StreamingNQueens.BOARD = IndexedSeq((1, 1), (2, 2), (3, 3), (4, 4))
  val testInitialPositions: IndexedSeq[StreamingNQueens.BOARD] = IndexedSeq(
    IndexedSeq((1, 1), (2, 3)),
    IndexedSeq((1, 1), (2, 4)),
    IndexedSeq((1, 2), (2, 4)),
    IndexedSeq((1, 3), (2, 1)),
    IndexedSeq((1, 4), (2, 1)),
    IndexedSeq((1, 4), (2, 2))
  )
  val testNextPosition1: StreamingNQueens.BOARD = IndexedSeq((1, 1))
  val nextPositionResult1: IndexedSeq[StreamingNQueens.BOARD] = IndexedSeq(
    IndexedSeq((1, 1), (2, 3)),
    IndexedSeq((1, 1), (2, 4))
  )
  val testResultsOutput: String = s"[2,4,1,3]\n._._._._.\n|_|x|_|_|\n|_|_|_|x|\n|x|_|_|_|\n|_|_|x|_|\n"
  def spec = suite("Full suite of Tests for nQueens problem")(
    test("Simple correct result [3,1,4,2] for nQueens(4)") {
      assertTrue(StreamingNQueens.isSafe(testCorrectResult1))
    },
    test("Simple correct result [2,4,1,3] for nQueens(4)") {
      assertTrue(StreamingNQueens.isSafe(testCorrectResult2))
    },
    test("Simple incorrect result [1,2,3,4] for nQueens(4)") {
      !assertTrue(StreamingNQueens.isSafe(testIncorrectResult1))
    },
    test("Check for generation of initial candidates") {
      assert(StreamingNQueens.createInitialPositions)(
        Assertion.isNonEmpty && Assertion.hasSameElements(testInitialPositions)
      )
    },
    test("Check for candidates (1,1) for next position equals ((1,1),(2,3)) and ((1,1),(2,4))") {
      assert(StreamingNQueens.addNextPosition(testNextPosition1))(
        Assertion.isNonEmpty && Assertion.hasSameElements(nextPositionResult1)
      )
    },
    test("Check match with output requirements") {
      assertZIO(StreamingNQueens.prettyPrint(testCorrectResult2))(
        Assertion.assertion("Should be equal")(_ == testResultsOutput)
      )
    }
  )
}
