JAVAC=openjdk8-javac -encoding UTF-8
NQUEENS_PACKAGE_DIR=jp/co/genetec/rdseminar/nqueens
CLASSPATHOPT= -cp /u01/local/lib/java/java-getopt-1.0.14.jar:.



.SUFFIXES: .java .class


PACKAGE_SRCS = $(NQUEENS_PACKAGE_DIR)/Solver.java \
	$(NQUEENS_PACKAGE_DIR)/SolverSimple.java \
	$(NQUEENS_PACKAGE_DIR)/SolverParallel1.java \
	$(NQUEENS_PACKAGE_DIR)/SolverParallel2.java \
	$(NQUEENS_PACKAGE_DIR)/SolverParallel3.java \
	$(NQUEENS_PACKAGE_DIR)/SolverStream.java

PACKAGE_CLASSES = $(PACKAGE_SRCS:.java=.class)

NQueens.class: NQueens.java $(PACKAGE_SRCS)
	$(JAVAC) $(CLASSPATHOPT) $*.java

$(PACKAGE_CLASSES): $(PACKAGE_SRCS)
	$(JAVAC) $(CLASSPATHOPT) $(PACKAGE_SRCS)



.PHONY: check-syntax
check-syntax:
	$(JAVAC) $(CLASSPATHOPT) -Xlint $(CHK_SOURCES)
