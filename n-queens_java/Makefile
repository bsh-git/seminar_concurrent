JAVAC=openjdk8-javac
CLASSPATHOPT= -cp /u01/local/lib/java/java-getopt-1.0.14.jar


.SUFFIXES: .java .class

NQueens.class: NQueens.java
	$(JAVAC) $(CLASSPATHOPT) $*.java



.PHONY: check-syntax
check-syntax:
	$(JAVAC) $(CLASSPATHOPT) -Xlint $(CHK_SOURCES) 2>&1 | tee /tmp/fm.log
