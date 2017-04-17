import java.util.*;
import java.util.stream.IntStream;
import gnu.getopt.Getopt;

class NQueens {
    static void usage_and_exit() {
	System.err.println("Usage_And_Exit: NQueens [n]");
    }

    public static void main(String[] args) {
	Getopt options = new Getopt("NQueens", args, "qp:");
	int c;
	int boardsize = 8;
	boolean nolist = false;
	int howto = 0;
	Solver solver = null;
	
	while ((c = options.getopt()) != -1) {
	    switch (c) {
	    case 'q':
		nolist = true;
		break;
	    case 'p':
		howto = Integer.parseUnsignedInt(options.getOptarg());
		break;
	    default:
		usage_and_exit();
	    }
	}


	int idx = options.getOptind();
	if (idx >= args.length) {
	    // no N
	    
	}
	else if (idx == args.length -1) {
	    boardsize = Integer.parseUnsignedInt(args[idx]);
	}
	else {
	    System.err.println("Too many arguments");
	    usage_and_exit();
	}

	switch (howto) {
	case 0: solver = new SolverSimple(boardsize); break;
	case 1: solver = new SolverParallel1(boardsize); break;
	case 2: solver = new SolverParallel2(boardsize); break;
	default:
	    System.err.println("Bad argument to -p");
	    usage_and_exit();
	}

	 
	long startTime = System.nanoTime();
	List <int []> patterns = solver.solve();
	long endTime = System.nanoTime();

	System.out.printf("%d-Queens solved in %f msecs. found %d patterns\n", boardsize, (endTime - startTime)/ 1000000.0, patterns.size());
	if (!nolist) {
	    for (int[] a: patterns) {
		boolean needcomma = false;
		for (int p: a) {
		    if (needcomma)
			System.out.print(", ");
		    needcomma = true;
		    System.out.print(p);
		}
		System.out.println("");
	    }
	}

	solver.printReport();
    }

    static abstract class Solver {
	int boardSize;

	Solver(int size) {
	    boardSize = size;
	}

	abstract List<int []> solve();
	abstract void printReport();

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

    //
    // 解法1: 単純な、深さ優先バックトラック
    //
    static class SolverSimple extends Solver {
	SolverSimple(int size) {
	    super(size);
	}

	List<int []> solve() {
	    int queens[] = new int[boardSize];
	    
	    return tryNewRow(0, queens);
	}

	void printReport() {
	    // nothing to report.
	}
    }

    //
    // 解法2: 最初の行の各桁に対して一つずつスレッドを起動する
    //
    static class SolverParallel1 extends Solver {
	SolverProc proc [];

	SolverParallel1(int size) {
	    super(size);
	    proc = new SolverProc[boardSize];
	}

	List<int []> solve() {
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

	void printReport() {
	    for (int i = 0; i < proc.length; ++i) {
		System.out.printf("[%d] %f msecs\n", i, proc[i].getExcutionTime() / 1000000.0);
	    }
	}

    }

    //
    // 解法3: スレッドをたくさん使う
    //
    static class SolverParallel2 extends Solver {
	int threadStartFailCount = 0;
	int threadCreatedCount = 0;
	int activeThreads = 0;
	int maxActiveThreads = 0;
	
	SolverParallel2(int size) {
	    super(size);
	}

	List<int []> solve() {
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
	void printReport() {
	    System.out.printf("Thread created: %d\n", threadCreatedCount);
	    System.out.printf("Thread start failed: %d\n", threadStartFailCount);
	    System.out.printf("Max active threads: %d\n", maxActiveThreads);
	    // for (int i = 0; i < proc.length; ++i) {
	    // 	System.out.printf("[%d] %f msecs\n", i, proc[i].getExcutionTime() / 1000000.0);
	    // }
	}
    }


}
