// -*- coding: utf-8 -*-
package jp.co.genetec.rdseminar.nqueens;

import java.util.*;

//
// 解法のためのベースクラス
//
public abstract class Solver {
    int boardSize;

    Solver(int size, int...options) {
	boardSize = size;
    }

    public abstract List<int []> solve();
    public abstract void printReport();

    static boolean canPutQueen(int row, int col, int [] queens) {
	for (int r = 0; r < row; ++r) {
	    if (queens[r] == col ||
		queens[r] - col == row - r ||
		queens[r] - col == r - row) {
		// can't put a new queen here.
		return false;
	    }
	}
	return true;
    }

    List<int []> tryNewRow(int row, int [] queens) {
	List <int []> result = new ArrayList<int []>();

	for (int c = 0; c < boardSize; ++c) {
	    if (canPutQueen(row, c, queens)) {
		queens[row] = c;
		if (row == boardSize - 1) {
		    // found a pattern
		    result.add(queens.clone());
		}
		else {
		    result.addAll(tryNewRow(row+1, queens));
		}
	    }
		
	}

	return result;
    }
	
}
