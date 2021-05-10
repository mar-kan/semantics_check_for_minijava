all: compile

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac symbols/*.java myVisitors/*.java myVisitors/evaluators/*.java Main.java

execute:
	java Main Example1.java

clean:
	rm -f *.class symbols/*.class myVisitors/*.class myVisitors/evaluators/*.class syntaxtree/* visitor/* *~

