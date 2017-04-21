// -*- coding: utf-8 -*-
package jp.co.genetec.rdseminar.nqueens;

import java.util.*;
import java.util.stream.*;
import jp.co.genetec.rdseminar.nqueens.Solver;

//
// 解法1: 単純な、深さ優先バックトラック
//
public class SolverStream extends Solver {
    public SolverStream(int size) {
	super(size);
    }

    public List<int []> solve() {
	int queens[] = new int[boardSize];
	    
	return tryNewRowStream(0, queens).collect(Collectors.toList());

    }

    public void printReport() {
	// nothing to report.
    }

    

    int [] lastRow(int c, int [] queens) {
	int ret[] = queens.clone();
	ret[boardSize - 1] = c;
	return ret;
    }

    static String queensToS(int [] queens) {
	return IntStream.of(queens).mapToObj(i -> "" + i).collect(Collectors.joining(","));
    }
    

    Stream<int []> trySub(int row, int col, int [] queens, Stream<int []> stream) {
	int q[] = queens.clone();
	q[row] = col;
	//System.err.println("truSub " + row + " col " + col + " : " + queensToS(queens));
	return Stream.concat(stream, tryNewRowStream(row + 1, q));
    }

    Stream<int []> tryNewRowStream(int row, int [] queens) {
	if (row == boardSize - 1) {
	    return IntStream.range(0, boardSize)
		.filter(c -> canPutQueen(row, c, queens))
		.mapToObj(c -> lastRow(c,queens));
	}
	else {
	    return IntStream.range(0, boardSize)
		.filter(c -> canPutQueen(row, c, queens)) /* .parallel() */
		.mapToObj(c -> new Integer(c))
		.reduce(Stream.empty(),
			(s, c) -> trySub(row, c, queens, s),
			(s1,s2) -> Stream.concat(s1,s2));
			
	}
    }

}

