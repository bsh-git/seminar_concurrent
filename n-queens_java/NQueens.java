import java.util.*;
import java.util.stream.IntStream;
import gnu.getopt.Getopt;
import jp.co.genetec.rdseminar.nqueens.*;

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


}
