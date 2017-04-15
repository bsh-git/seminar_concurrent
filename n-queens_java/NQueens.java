import java.util.*;
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
	default:
	    System.err.println("Bad argument to -p");
	    usage_and_exit();
	}

	 
	long startTime = System.nanoTime();
	List <int []> patterns = solver.solve();
	long endTime = System.nanoTime();

	System.out.printf("%d-Queens solved in %f msecs\n", boardsize, (endTime - startTime)/ 1000000.0);
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

	List<int []> tryNewRow(int row, int [] queens) {
	    List <int []> result = new ArrayList<int []>();

	    for (int c = 0; c < boardSize; ++c) {
		boolean ok = true;
		for (int r = 0; r < row; ++r) {
		    if (queens[r] == c ||
			queens[r] - c == row - r ||
			queens[r] - c == r - row) {
			// can't put a new queen here.
			ok = false;
			break;
		    }
		}

		if (ok) {
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
	
    };

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

}
