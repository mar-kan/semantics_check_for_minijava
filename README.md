Compilers Project 2
-----------------
Kanellaki Maria-Anna  -  1115201400060


-----------------

Compile instructions: $ make

Execution instructions: $ java Main [input files]

-----------------

The program supports multiple input files at the same execution.

When a compiler exception is thrown, the program continues to run for the other input files.

The offsets for a program are printed only in successfully evaluated programs.

Directory <symbols> contains the classes that were created for the symbol tables. It contains:

    class AllClasses with the list of all the classes encountered, the main class and the main class' name.
    class ClassData which contains every info related to a class.
    class MethodData which contains every info related to a method.
    class Variable data which stores the name and type of a variable.

There are 2 visitors (in package myVisitors):

    The first one stores all the classes, methods and variables that are encountered in each program, in the classes 
    created in package symbols. It also evaluates all declarations. For convenience, the scope is passed as a second 
    argument of the visitor functions, to know where to store anything.

    The second one evaluates all the other expressions of the programs inputed. For convenience, most visitor functions 
    return their type.
 
Package evaluators was created to help with the evaluations of the visitors.

-----------------

