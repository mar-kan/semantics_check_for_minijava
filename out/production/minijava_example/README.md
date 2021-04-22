Kanellaki Maria-Anna  -  1115201400060

Compilers Project 2

-----------------


Compile instructions: $make

Execution instructions: $java -cp ./out Main [input files]

-----------------

The program terminates at the end of the parsing (successful or not). If the parsing was successfull, it prints the 
offsets like instructed, else it prints all the errors it encountered.

The program supports many input files at the same execution.

All the output files are produced in directory <out>.

Directory <Symbols> contains the classes that were created to store and access the data of the input files. Their class 
names, their methods and fields, and the methods' variables are stored. Also the name of the main class.

-----------------

The program checks:
    
    Class names for duplicates (also with the main class name).
    When classes that are inherited by others, that the class has already been declared.
    Method names for duplicates in the same scope.
    Overriding methods to match the type and arguments (type / number) of the overrided method.
    Duplicate fields in classes.