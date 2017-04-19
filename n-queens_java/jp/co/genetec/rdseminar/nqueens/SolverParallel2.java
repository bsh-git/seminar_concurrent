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
    int maxTasks = 20;
    SolverTask tasks[];
    int nTasksTotal = 0;
	
    public SolverParallel2(int size, int... options) {
	super(size);
	if (options.length >= 1) {
	    maxTasks = options[0];
	}
    }

    public List<int []> solve() {
	int queens[] = new int[boardSize];
	nTasksTotal = 0;
	tasks = new SolverTask[maxTasks];
	return tryNewRow2(0, queens);
    }

    List<int []> tryNewRow2(int row, int [] queens) {
	List<int []> result = new ArrayList<int []>();
	int taskidx[] = new int[boardSize];
	int nMyTasks = 0;
	

	for (int c = 0; c < boardSize; ++c) {
	    if (!canPutQueen(row, c, queens)) {
		continue;
	    }

	    if (row == boardSize -1) {
		int answer[] = queens.clone();
		answer[row] = c;
		result.add(answer);
	    }
	    else {
		int idx = allocateTaskSlot();
		if (idx >= 0) {
		    tasks[idx] = startNewTask(row, c, queens);
		    taskidx[nMyTasks++] = idx;
		}
		else  {
		    // これ以上スレッドを増せない時、このスレッドで続きを計算する
		    queens[row] = c;
		    result.addAll(tryNewRow(row + 1, queens));
		}
	    }
	}

	for (int i=0; i < nMyTasks; ++i) {
	    try {
		SolverTask task = tasks[taskidx[i]];
		result.addAll(task.getResult());
	    } catch (InterruptedException e) {
		System.err.println("thread interrupted: " + e.toString());
	    }
	}

	return result;
    }

    private synchronized int allocateTaskSlot() {
	if (nTasksTotal >= maxTasks)
	    return -1;
	return nTasksTotal++;
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
	// synchronized (this) {
	//     activeThreads++;
	//     if (activeThreads > maxActiveThreads)
	// 	maxActiveThreads = activeThreads;
	// }
	return task;
    }

    class SolverTask implements Runnable {
	private int startCol, startRow;
	private List<int []> result;
	private Thread thread;
	private int [] queens;
	private long timestamp[];
	    
	SolverTask(int row, int col, int q[]) {
	    timestamp = new long[5];
	    timestamp[0] = System.nanoTime();
	    startRow = row;
	    startCol = col;
	    // queens = q.clone();
	    queens = new int[boardSize];
	    for (int r=0; r < startRow; ++r)
		queens[r] = q[r];
	    thread = new Thread(this);
	}

	public void start() {
	    thread.start();
	}

	public void run() {
	    timestamp[1] = System.nanoTime();
	    queens[startRow] = startCol;
	    result = tryNewRow2(startRow + 1, queens);
	    timestamp[2] = System.nanoTime();
	}

	public List<int []>getResult() throws InterruptedException {
	    timestamp[3] = System.nanoTime();
	    thread.join();
	    timestamp[4] = System.nanoTime();
	    // synchronized (this) {
	    // 	--activeThreads;
	    // }
	    // if (result == null) {
	    // 	System.err.println("Something wrong happend in thread");
	    // 	return (new ArrayList<int []>());
	    // }
	    return result;
	}

	public long getExcutionTime() {
	     // System.out.printf("%d %d\n", startTime, endTime);
	     return timestamp[2] - timestamp[1];
	}

	public void printReport(int no) {
	    System.out.printf("[%d] (", no);
	    for (int r=0; r <= startRow; ++r) {
		System.out.printf("%d ", queens[r]);
	    }
	    System.out.printf(") %8d %8d %8d %8d %8d \t%f msecs\n",
			      timestamp[0],
			      timestamp[1],
			      timestamp[2],
			      timestamp[3],
			      timestamp[4],
			      (timestamp[2] - timestamp[1]) / 1000000.0);
	}

    }

    @Override
    public void printReport() {
	System.out.printf("Task created: %d\n", nTasksTotal);
	System.out.printf("Thread start failed: %d\n", threadStartFailCount);
	// System.out.printf("Max concurrently active threads: %d\n", maxActiveThreads);
	for (int i=0; i < nTasksTotal; ++i) {
	    if (tasks[i] != null)
		tasks[i].printReport(i);
	}
    }
}

