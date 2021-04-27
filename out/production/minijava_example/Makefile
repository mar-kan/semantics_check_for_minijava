all: compile

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac Symbols/*.java Main.java

execute:
	java Main Example1.java

clean:
	rm -f *.class Symbols/*.class syntaxtree/* visitor/* *~

