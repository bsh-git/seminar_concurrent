// -*- coding: utf-8 -*-
package jp.co.genetec.rdseminar.nqueens;

import java.util.*;
import jp.co.genetec.rdseminar.nqueens.Solver;

//
// 解法2: 最初の行の各桁に対して一つずつスレッドを起動する
//

public class SolverParallel1 extends Solver {
    SolverProc proc [];

    public SolverParallel1(int size) {
	super(size);
	proc = new SolverProc[boardSize];
    }

    public List<int []> solve() {
	List<int []> result = new ArrayList<int []>();
	    
	for (int c=0; c < boardSize; ++c) {
	    proc[c] = new SolverProc(c);
	}

	for (SolverProc p: proc) {
	    try {
		result.addAll(p.getResult());
	    } catch (InterruptedException e) {
		System.err.println("Something wrong happend in a solver thread: " + e.toString());
	    }
	}

	return result;
    }

    class SolverProc implements Runnable {
	private int startCol;
	private List<int []> result;
	private long startTime, endTime;
	private Thread thread;
	    
	SolverProc(int col) {
	    startCol = col;
	    thread = new Thread(this);
	    thread.start();
	}

	public void run() {
	    int queens[] = new int[boardSize];

	    queens[0] = startCol;
	    startTime = System.nanoTime();
	    result = tryNewRow(1, queens);
	    endTime = System.nanoTime();
	}

	public List<int []>getResult() throws InterruptedException {
	    thread.join();
	    return result;
	}

	public long getExcutionTime() {
	    // System.out.printf("%d %d\n", startTime, endTime);
	    return endTime - startTime;
	}
    }

    public void printReport() {
	for (int i = 0; i < proc.length; ++i) {
	    System.out.printf("[%d] %f msecs\n", i, proc[i].getExcutionTime() / 1000000.0);
	}
    }

}
