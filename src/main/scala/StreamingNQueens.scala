import zio.*
import zio.Schedule.WithState
import zio.stream.*

import scala.annotation.tailrec

object StreamingNQueens extends zio.ZIOAppDefault {
  type BOARD = IndexedSeq[(Int,Int)]
  def unfoldBoardPositions(li: IndexedSeq[BOARD]): ZStream[Any, Nothing, BOARD]  =
    ZStream.unfold(li) {
      case Vector() => None
      case (hd: BOARD) +: tail => Some(hd, tail)
    }
  def prettyPrint(solution: BOARD, boardSize: Int): Task[String] = {
    val solutionString = solution.map(e => e._2).mkString("[", ",", "]")
    val topEdge = (1 to boardSize).map(_ => "_").mkString(".", ".", ".")
    val rows = solution.map { pos =>
      val cellsBefore = (1 until pos._2).map(_ => "_")
      val beforeString = if (cellsBefore.isEmpty) "|" else cellsBefore.mkString("|", "|", "|")
      val cellsAfter = ((pos._2 + 1) until boardSize + 1).map(_ => "_")
      val afterString = if (cellsAfter.isEmpty) "|" else cellsAfter.mkString("|", "|", "|")

      beforeString + "x" + afterString
    }
    ZIO.attempt(s"$solutionString\n$topEdge\n${rows.mkString("\n")}\n")
  }
  def isSafeZIO(value: BOARD): Task[Boolean] = {
    @tailrec
    def isSafeTailRec(hd: (Int, Int), tl: BOARD): Boolean = {
      if (tl.isEmpty) true
      else (hd, tl) match
        case (x, y) if (x._1 - y.head._1).abs == (x._2 - y.head._2).abs => false
        case _ => isSafeTailRec(hd, tl.tail)
    }
    @tailrec
    def checkSolutionTR(solution: BOARD, safeResult: Boolean): Boolean = {
      if (solution.map((_,b)⇒ b).groupBy(identity).collect {
        case (_, ys) if ys.lengthCompare(1) > 0 => false
        case _ => true
      }.exists(_ == false)) !safeResult
      else if (solution.isEmpty || !safeResult)
        safeResult
      else
        checkSolutionTR(solution.tail, isSafeTailRec(solution.head, solution.tail))
    }
    ZIO.attempt(checkSolutionTR(value, true))
  }
  def isSafe(value: BOARD): Boolean = {
    @tailrec
    def isSafeTailRec(hd: (Int, Int), tl: BOARD): Boolean = {
      if (tl.isEmpty) true
      else (hd, tl) match
        case (x, y) if (x._1 - y.head._1).abs == (x._2 - y.head._2).abs => false
        case _ => isSafeTailRec(hd, tl.tail)
    }
    @tailrec
    def checkSolutionTR(solution: BOARD, safeResult: Boolean): Boolean = {
      if (solution.map((_,b)⇒ b).groupBy(identity).collect {
        case (_, ys) if ys.lengthCompare(1) > 0 => false
        case _ => true
      }.exists(_ == false)) !safeResult
      else if (solution.isEmpty || !safeResult)
        safeResult
      else
        checkSolutionTR(solution.tail, isSafeTailRec(solution.head, solution.tail))
    }
    checkSolutionTR(value, true)
  }
  def addNextPosition(li: BOARD): IndexedSeq[BOARD] = {
    val il = li.reverse
    val hd = il.head
    val newPositions = for {
        a <- (1 to boardSize).filterNot(n ⇒ n == hd._2 || n == hd._2 - 1 || n == hd._2 + 1)
      } yield ((hd._1+1,a) +: il).reverse
    newPositions
  }
  def createInitialPositions(n: Int): IndexedSeq[BOARD] = (for {
    a <- (1 to n)
  } yield IndexedSeq((1,a))).flatMap(n ⇒ addNextPosition(n))
  def exhaustSolutionSet(num: Ref[Int]): ZIO[Clock, Nothing, (Int, Long)] =
    num.get repeat solutionCheckPolicy
  def countDown(counter: Ref[Int]): ZIO[Console, Nothing, Int] =  for {
    reqNumber <- counter.getAndUpdate(_ - 1)
  } yield reqNumber

  val queueTheQueens: UIO[Queue[BOARD]] = Queue.unbounded[BOARD]
  val queueTheSolutions: UIO[Queue[BOARD]] = Queue.unbounded[BOARD]

  val solutionCheckPolicy: WithState[(Unit, Long), Any, Int, (Int, Long)] =
    Schedule.recurUntilEquals(0) && Schedule.spaced(200.millis)
  val nQueensSolution: Map[Int, Int] =
    Map(
      0 -> 1, 1 -> 1, 2 -> 0, 3 -> 0, 4 -> 2, 5 -> 0, 6 -> 4, 7 -> 40, 8 -> 92,
      9 -> 352,10 -> 724, 11 -> 2680, 12 -> 14200, 13 -> 73712,14 -> 365596,
      15 -> 2279184
    )


  /**
   * Algorithm: nQueens is a constraint satisfaction problem with three hard constraints.  Each queen must be on it's own row and in it's own column, as well as not sharing any diagonals with any other queen. The algorithm uses a feed forward approach similar to a breadth-first search starting with positions from the first row of the chessboard then adding positions for the next row as potential solutions.
   * The solution set is formatted for readability through the prettyPrint function.
   *
   * The algorithm runs within three activities:
   *  1. The initial bootstrap to setup the board and first batch of positions.
   *
   * 2.  The solution evaluation activity adds the next positions for a potential solution, checks for
   * non-attacking sequences with failed solutions going to the bit bucket. Then this activity partitions
   * the candidates according to completeness. Full n-length solutions go into the solution queue while
   * incomplete sequences are re-submitted to the input queue for the next round.
   *
   * 3.  The last activity is to print out all the solutions from the solution queue.  The pretty print function
   * lays out a chess board pattern on the console.
   */
  val boardSize = 8
  val run: ZIO[Scope with Clock with Console, Throwable, Unit] = for {
    numProcessors <- ZIO.succeed(java.lang.Runtime.getRuntime.availableProcessors)
    counter <- Ref.make[Int](nQueensSolution(boardSize))
    inputQueue <- queueTheQueens
    solutionQueue <- queueTheSolutions

    /*
     The bootstrapping function creates an initial sequence of one element vectors of length boardSize. Then the function adds the next positions that may form part of a solution, filtering out the same column and immediate diagonal positions. This function returns a vector of vectors, which needs to be unwrapped to pull out the individual solution vectors and fed into the input queue.
    */
    initialBootStrapForQueens = unfoldBoardPositions(createInitialPositions(boardSize)).foreach(inputQueue.offer)

/**
 The stream findNQueensSolutions is the core algorithm. Taking vectors from the input queue, the stream maps over
 the queue elements in parallel, according to the number of processors in the system. The addNextPosition looks ahead to
 the next potential position which is also fed into the stream 'unfoldBoardPositions' so each new possible solution can
 be checked by the isSafe function.  The parallel processing capability provided by 'flatMapPar' saturates all the
 available cores without fiddling around with managing fibers or other concurrency primitives.

 The next step in this stream is to partition the potential solutions into two streams, one to take solutions that are
 not yet complete and the other to accept completed solutions.  The first incomplete solutions are put back into the
 input queue, while the completed solutions are sent to the solution queue.
*/
    findNQueensSolutions = ZStream.fromQueue(inputQueue)
                .flatMapPar(numProcessors)((queens: BOARD) ⇒ unfoldBoardPositions(addNextPosition(queens).filter(isSafe)))
                .partition((queens: BOARD) ⇒ queens.length < boardSize, buffer = 20)
                .flatMap { streams ⇒
                  for {
                    out1 <- streams(0)
                      .foreach(incompleteSolution ⇒ inputQueue.offer(incompleteSolution)).fork
                    out2 <- streams(1)
                      .foreach(completedSolution ⇒ solutionQueue.offer(completedSolution)).fork
                    _ <- out1.join.zipPar(out2.join)
                  } yield ()
                }
    /**
     *
     **/
    printoutNQueensSolutions = ZStream.fromQueue(solutionQueue)
                .mapZIOParUnordered(numProcessors)(solutions ⇒ prettyPrint(solutions,solutions.length))
                .tap(solution ⇒ Console.printLine(s"Solution:\n$solution") *> countDown(counter))
                .runDrain
    /**
     *
     **/
    _ <- initialBootStrapForQueens
    _ <- findNQueensSolutions race printoutNQueensSolutions race exhaustSolutionSet(counter)
  } yield ()

}
