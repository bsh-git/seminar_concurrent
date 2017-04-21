// -*- coding: utf-8 -*-
package jp.co.genetec.rdseminar.nqueens;

import java.util.*;
import jp.co.genetec.rdseminar.nqueens.Solver;

//
// 解法4: スレッドをたくさん使う
//        解放3の修正
//
public class SolverParallel3 extends Solver {
    int activeTasksLimit = 20;
    int threadStartFailCount = 0;
    int threadCreatedCount = 0;
    int currentActiveTasks = 0;
    int maxActiveTasks = 0;
    int maxTasks = 20;
    SolverTask tasks[];
    int nTasksTotal = 0;
	
    public SolverParallel3(int size, int... options) {
	super(size, options);
	if (options.length >= 1) {
	    activeTasksLimit = options[0];
	}
    }

    public List<int []> solve() {
	int queens[] = new int[boardSize];
	nTasksTotal = 0;
	currentActiveTasks = 0;
	maxActiveTasks = 0;
	tasks = new SolverTask[200];
	return tryNewRow2(0, queens);
    }

    List<int []> tryNewRow2(int row, int [] queens) {
	List<int []> result = new ArrayList<int []>();
	int taskidx[] = new int[boardSize];
	int nMyTasks = 0;
	int cols[] = new int[boardSize];
	int nCols = 0;


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
		cols[nCols++] = c;
	    }
	}

	int colidx=0;
	while (colidx < nCols) {
	    int a[];
	    final int twozeros[] = {0,0};
	    
	    if (nCols - colidx > 1)
		a = allocateTaskSlots(nCols - colidx);  // start index and length
	    else
		a = twozeros;


	    if (a[1] > 0) {
		for (int i=0; i < a[1]; ++i) {
		    tasks[a[0] + i] = startNewTask(row, cols[colidx++], queens);
		    taskidx[nMyTasks++] = a[0] + i;
		}
	    }
	    else {
		// これ以上スレッドを増せない時、このスレッドで続きを計算する
		queens[row] = cols[colidx++];
		result.addAll(tryNewRow(row + 1, queens));
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

    private synchronized int[] allocateTaskSlots(int wanted) {
	int available = Math.max(0, tasks.length - nTasksTotal);
	int allowed = Math.max(0, activeTasksLimit - currentActiveTasks);
	int ret[] = new int [2];

	ret[0] = nTasksTotal;
	ret[1] = Math.min(Math.min(wanted, available), allowed);

	nTasksTotal += ret[1];
	currentActiveTasks += ret[1];
	if (currentActiveTasks > maxActiveTasks)
	    maxActiveTasks = currentActiveTasks;

	return ret;
    }

    SolverTask startNewTask(int row, int col, int queens[]) {
	SolverTask task = new SolverTask(row, col, queens);

	try {
	    task.start();
	}
	catch (java.lang.OutOfMemoryError e) {
	    synchronized (this) {
		threadStartFailCount++;
		currentActiveTasks--;
	    }
	    return null;
	}
	return task;
    }

    class SolverTask implements Runnable {
	private int startRow;
	private List<int []> result;
	private Thread thread;
	private int [] queens;
	private long timestamp[];
	    
	SolverTask(int row, int col, int q[]) {
	    timestamp = new long[5];
	    timestamp[0] = System.nanoTime();
	    startRow = row;
	    // queens = q.clone();
	    queens = new int[boardSize];
	    for (int r=0; r < startRow; ++r)
		queens[r] = q[r];
	    queens[startRow] = col;
	    thread = new Thread(this);
	}

	public void start() {
	    thread.start();
	}

	public void run() {
	    timestamp[1] = System.nanoTime();
	    result = tryNewRow2(startRow + 1, queens);
	    timestamp[2] = System.nanoTime();
	}

	public List<int []>getResult() throws InterruptedException {
	    timestamp[3] = System.nanoTime();
	    thread.join();
	    thread = null;
	    synchronized(this) {
		currentActiveTasks--;
	    }
	    timestamp[4] = System.nanoTime();
	    List <int []>ret = result;
	    result = null;
	    return ret;
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
	System.out.printf("Max concurrently active taskss: %d\n", maxActiveTasks);
	for (int i=0; i < nTasksTotal; ++i) {
	    if (tasks[i] != null)
		tasks[i].printReport(i);
	}
    }


}

