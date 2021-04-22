all: compile move

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac Symbols/*.java Main.java

move:
	@mv *.class ./out/
	@mv Symbols/*.class ./out/Symbols
	@mv ./syntaxtree/*.class ./out/syntaxtree
	@mv ./visitor/*.class ./out/visitor
	@cp ./syntaxtree/* ./out/syntaxtree
	@cp ./visitor/* ./out/visitor
	@echo "Moved all output files in dir out"

execute:
	java -cp ./out Main Example1.java

clean:
	rm -f out/*.class out/*.java out/syntaxtree/* out/visitor/* out/Symbols/* syntaxtree/* visitor/* *~

