// -*- coding: utf-8 -*-
package jp.co.genetec.rdseminar.nqueens;

import java.util.*;
import jp.co.genetec.rdseminar.nqueens.Solver;

//
// 解法3: スレッドをたくさん使う
//
public class SolverParallel2 extends Solver {
    int threadStartFailCount = 0;
    int threadCreatedCount = 0;
    int activeThreads = 0;
    int maxActiveThreads = 0;
	
    public SolverParallel2(int size) {
	super(size);
    }

    public List<int []> solve() {
	int queens[] = new int[boardSize];
	return tryNewRow(0, queens);
    }

    List<int []> tryNewRow(int row, int [] queens) {
	List<int []> result = new ArrayList<int []>();
	int possibleCols[] = new int[boardSize + 1];

	int ncols = 0;
	for (int c = 0; c < boardSize; ++c) {
	    if (canPutQueen(row, c, queens)) {
		possibleCols[ncols++] = c;
	    }
	}
	possibleCols[ncols] = -1;

	if (ncols == 0) {
	    // System.err.println("no availabe place for a new queen");
	}
	else if (row == boardSize -1) {
	    for (int i=0; i < ncols; ++i) {
		int answer[] = queens.clone();
		answer[row] = possibleCols[i];
		result.add(answer);
	    }
	}
	else {
	    SolverTask tasks[] = new SolverTask[boardSize];
	    int nTasks = 0;
		
	    for (int i=0; i < ncols; ++i) {
		SolverTask task = null;
		int col = possibleCols[i];
		if (i < ncols-1)
		    task = startNewTask(row, col, queens);
		if (task != null) {
		    tasks[nTasks++] = task;
		}
		else {
		    // 最後の桁、またはこれ以上スレッドを増せない時、このスレッドで続きを計算する
		    queens[row] = col;
		    result.addAll(tryNewRow(row + 1, queens));
		}
	    }

	    //System.err.printf("row%d: getting answers from %d threads (ncols=%d)\n", row, nTasks, ncols);
		
	    for (int i=0; i < nTasks; ++i) {
		try {
		    SolverTask task = tasks[i];
		    List<int []> answer = task.getResult();
		    //System.err.printf("answers from task for row%d,col%d: ", task.startRow, task.startCol);
		    //System.err.println(answer);
			
		    result.addAll(answer);
		} catch (InterruptedException e) {
		    System.err.println("thread interrupted: " + e.toString());
		}
	    }

	}
	return result;
    }

    SolverTask startNewTask(int row, int col, int queens[]) {
	SolverTask task = new SolverTask(row, col, queens);

	try {
	    task.start();
	}
	catch (java.lang.OutOfMemoryError e) {
	    synchronized (this) {
		threadStartFailCount++;
	    }
	    return null;
	}
	synchronized (this) {
	    activeThreads++;
	    if (activeThreads > maxActiveThreads)
		maxActiveThreads = activeThreads;
	}
	return task;
    }

    class SolverTask implements Runnable {
	int startCol, startRow;
	private int queens[];
	List<int []> result;
	private long startTime, endTime;
	private Thread thread;
	    
	SolverTask(int row, int col, int q[]) {
	    startRow = row;
	    startCol = col;
	    queens = q.clone();
	    queens[row] = col;
	    thread = new Thread(this);
	    synchronized (this) {
		threadCreatedCount++;
	    }
	}

	public void start() {
	    thread.start();
	}

	public void run() {
	    startTime = System.nanoTime();
	    result = tryNewRow(startRow + 1, queens);
	    endTime = System.nanoTime();
	}

	public List<int []>getResult() throws InterruptedException {
	    thread.join();
	    synchronized (this) {
		--activeThreads;
	    }
	    if (result == null) {
		System.err.println("Something wrong happend in thread");
		return (new ArrayList<int []>());
	    }
	    return result;
	}

	public long getExcutionTime() {
	    // System.out.printf("%d %d\n", startTime, endTime);
	    return endTime - startTime;
	}

    }


    //    IntStream.range(0, boardSize).filter(c -> canPutQueen(row, c, queens))
    // .mapToObj(col -> {
    // 	System.out.println(row + "," + col);

    // 	int newq[] = queens.clone();
    // 	newq[row] = col;
    // 	if (row == boardSize -1 ) {
    // 	    return newq;
    // 	}
    // 	else {
    // 	    return tryNewRow(row+1, newq);
    // 	}
    //     });



    // List<Integer> possibleCols = new ArrayList<Integer>();

    // for (int c = 0; c < boardSize; ++c) {
    // 	if (canPutQueen(row, c, queens))
    // 	    possibleCols.add(c);
    // }

    // System.err.printf("cols at row%d: ", row);
    // for (int c : possibleCols) {
    // 	System.err.printf("%d ", c);
    // }
    public void printReport() {
	System.out.printf("Thread created: %d\n", threadCreatedCount);
	System.out.printf("Thread start failed: %d\n", threadStartFailCount);
	System.out.printf("Max active threads: %d\n", maxActiveThreads);
	// for (int i = 0; i < proc.length; ++i) {
	// 	System.out.printf("[%d] %f msecs\n", i, proc[i].getExcutionTime() / 1000000.0);
	// }
    }
}

