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
    int taskCount = 0;
    int threadLimit = Integer.MAX_VALUE;
	
    public SolverParallel2(int size, int... options) {
	super(size);
	if (options.length >= 1) {
	    threadLimit = options[0];
	}
    }

    public List<int []> solve() {
	int queens[] = new int[boardSize];
	return tryNewRow2(0, queens);
    }

    List<int []> tryNewRow2(int row, int [] queens) {
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
		if (taskCount < threadLimit /*&& i < ncols-1*/) {
		    ++taskCount;
		    task = startNewTask(row, col, queens);
		}
		if (task != null) {
		    tasks[nTasks++] = task;
		}
		else {
		    // 最後の桁、またはこれ以上スレッドを増せない時、このスレッドで続きを計算する
		    queens[row] = col;
		    result.addAll(tryNewRow2(row + 1, queens));
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
	    //	    synchronized (this) {
	    //		threadStartFailCount++;
	    //}
	    return null;
	}
	// synchronized (this) {
	//     activeThreads++;
	//     if (activeThreads > maxActiveThreads)
	// 	maxActiveThreads = activeThreads;
	// }
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
	    // synchronized (this) {
	    // 	threadCreatedCount++;
	    // }
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
	    // synchronized (this) {
	    // 	--activeThreads;
	    // }
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

    @Override
    public void printReport() {
	System.out.printf("Thread created: %d\n", threadCreatedCount);
	System.out.printf("Thread start failed: %d\n", threadStartFailCount);
	System.out.printf("Max concurrently active threads: %d\n", maxActiveThreads);
	// for (int i = 0; i < proc.length; ++i) {
	// 	System.out.printf("[%d] %f msecs\n", i, proc[i].getExcutionTime() / 1000000.0);
	// }
    }
}

