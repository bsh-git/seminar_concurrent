import java.util.*;
import java.util.stream.IntStream;
import gnu.getopt.Getopt;
import jp.co.genetec.rdseminar.nqueens.*;

class NQueens {
    static void usage_and_exit() {
	System.err.println("Usage_And_Exit: NQueens [n]");
	System.exit(1);
    }

    public static void main(String[] args) {
	Getopt options = new Getopt("NQueens", args, "qp:t:c:");
	int c;
	int boardsize = 8;
	int howto = 0;
	Solver solver = null;
	int nthreads = 0;
	int repeat = 1;
	int verbose = 3;
	
	while ((c = options.getopt()) != -1) {
	    switch (c) {
	    case 'c':
		repeat = Integer.parseUnsignedInt(options.getOptarg());
		break;
	    case 'q':
		--verbose;
		break;
	    case 'p':
		howto = Integer.parseUnsignedInt(options.getOptarg());
		break;
	    case 't':
		nthreads = Integer.parseUnsignedInt(options.getOptarg());
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
	case 2:
	    if (nthreads > 0)
		solver = new SolverParallel2(boardsize, nthreads);
	    else
		solver = new SolverParallel2(boardsize);
	    break;
	default:
	    System.err.println("Bad argument to -p");
	    usage_and_exit();
	}

	 
	for (int rep=0; rep < repeat; ++rep) {
	    long startTime = System.nanoTime();
	    List <int []> patterns = solver.solve();
	    long endTime = System.nanoTime();

	    if (verbose > 0)
		System.out.printf("%d-Queens solved in %f msecs. found %d patterns\n",
				  boardsize, (endTime - startTime)/ 1000000.0, patterns.size());
	    if (verbose > 2) {
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

	    if (verbose > 1)
		solver.printReport();
	}
    }


}
