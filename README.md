Compilers Project 2

-----------------


Compile instructions: $make

Execution instructions: $java Main [input files]

-----------------

The program terminates at the end of the parsing (successful or not). If the parsing was successfull, it prints the 
offsets like instructed, else it prints all the errors it encountered.

The program supports many input files at the same execution.

Directory <Symbols> contains the classes that were created to store and access the data of the input files. Their class 
names, their methods and fields, and the methods' variables are stored. Also the name of the main class.

There are 2 visitors. The first one stores all the classes, methods and variables in Symbols and the second one evaluates 
all the expressions of the program inputed.

-----------------

The DeclarationEvaluator checks:
    
    Class names for duplicates (also with the main class name).
    When classes that are inherited by others, that the class has already been declared.
    Method names for duplicates in the same scope.
    Overriding methods to match the type and arguments (type / number) of the overrided method (in any upperclass).
    Duplicate fields in classes.
    Duplicate variables in methods.
    Duplicate variables in main.

The ExpressionEvaluator checks:

    that every variable / method / class encountered exists.
    for matching types in any expression.
    for correct method calls (return type, arguments).
    array assignments for correct types and indexing (using ints).
    allocation expressions for correct types.
    
