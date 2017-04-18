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
	private Thread thread;
	//private long startTime, endTime;
	private long timestamp[];
	    
	SolverProc(int col) {
	    timestamp = new long[5];
	    timestamp[0] = System.nanoTime();
	    startCol = col;
	    thread = new Thread(this);
	    thread.start();
	}

	public void run() {
	    int queens[] = new int[boardSize];

	    queens[0] = startCol;
	    timestamp[1] = System.nanoTime();
	    result = tryNewRow(1, queens);
	    timestamp[2] = System.nanoTime();
	}

	public List<int []>getResult() throws InterruptedException {
	    timestamp[3] = System.nanoTime();
	    thread.join();
	    timestamp[4] = System.nanoTime();
	    return result;
	}

	public long getExcutionTime() {
	     // System.out.printf("%d %d\n", startTime, endTime);
	     return timestamp[2] - timestamp[1];
	}

	public void printReport(int no) {
	    System.out.printf("[%d] %8d %8d %8d %8d\n",
			      no,
			      timestamp[1] - timestamp[0],
			      timestamp[2] - timestamp[0],
			      timestamp[3] - timestamp[0],
			      timestamp[4] - timestamp[0]);
	}
	
    }

    public void printReport() {
	for (int i = 0; i < proc.length; ++i) {
	    proc[i].printReport(i);
	}
    }

}
