import java.util.*;
import gnu.getopt.Getopt;

class NQueens {
    static void usage() {
	System.err.println("Usage: NQueens [n]");
    }

    public static void main(String[] args) {
	Getopt options = new Getopt("NQueens", args, "q");
	int c;
	int boardsize = 8;
	boolean nolist = false;

	while ((c = options.getopt()) != -1) {
	    switch (c) {
	    case 'q':
		nolist = true;
		break;
	    default:
		usage();
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
	    usage();
	}

	long startTime = System.nanoTime();
	List <int []> patterns = new Solver(boardsize).run();
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
    }

    static class Solver {
	int boardSize;

	Solver(int size) {
	    boardSize = size;
	}

	List<int []> run() {
	    int queens[] = new int[boardSize];
	    
	    return tryNewRow(0, queens);
	}

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
    }


}
