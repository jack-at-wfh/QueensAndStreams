# QueensAndStreams
Algorithm: nQueens is a constraint satisfaction problem with three hard constraints.  Each queen must be on it's own row and in it's own column, 
as well as not sharing any diagonals with any other queen. The algorithm uses a feed forward approach similar to a breadth-first search starting 
with positions from the first row of the chessboard then adding positions for the next row as potential solutions. The solution set is formatted 
for readability through the prettyPrint function. 
The algorithm runs within three activities:

1. The initial bootstrap to setup the board and first batch of positions.

2.  The solution evaluation activity adds the next positions for a potential solution, checks for non-attacking sequences with failed solutions
going to the bit bucket. Then this activity partitions the candidates according to completeness. Full n-length solutions go into the solution queue while
incomplete sequences are re-submitted to the input queue for the next round.

3.  The last activity is to print out all the solutions from the solution queue.  The pretty print function
lays out a chess board pattern on the console.
   
