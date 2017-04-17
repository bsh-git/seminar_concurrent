// -*- coding: utf-8 -*-
package jp.co.genetec.rdseminar.nqueens;

import java.util.*;
import jp.co.genetec.rdseminar.nqueens.Solver;

//
// 解法1: 単純な、深さ優先バックトラック
//
public class SolverSimple extends Solver {
    public SolverSimple(int size) {
	super(size);
    }

    public List<int []> solve() {
	int queens[] = new int[boardSize];
	    
	return tryNewRow(0, queens);
    }

    public void printReport() {
	// nothing to report.
    }
}

