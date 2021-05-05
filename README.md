Kanellaki Maria-Anna  -  1115201400060

Compilers Project 2

-----------------


Compile instructions: $ make

Execution instructions: $ java Main [input files]

-----------------

The program supports multiple input files at the same execution.

When a compiler exception is thrown, the program continues to run for the other input files.

The offsets for a program are printed only in successfully evaluated programs.

Directory <Symbols> contains the classes that were created to store and access the data of the input files. It contains:

    class AllClasses with the list of all the classes encountered, the main class and the main class' name.
    class ClassData which contains every info related to a class.
    class MethodData which contains every info related to a method.
    class Variable data which stores the name and type of a variable.

There are 2 visitors:

    The first one stores all the classes, methods and variables that are encountered in the classes created in package 
    Symbols. It also evaluates all declarations. For convenience, the scope is passed as a second argument of the 
    visitors, to know where to store anything.

    The second one evaluates all the expressions of the program inputed. For convenience, most visitor functions about
    expressions and statements return their type.

-----------------

