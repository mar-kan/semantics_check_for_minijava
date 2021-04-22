import Symbols.ClassData;
import Symbols.MethodData;
import Symbols.MyClasses;

import java.util.Iterator;
import java.util.Map;

public class Evaluator {
    String file_name;   // stores file name for clearer error messages


    public Evaluator(String filename)
    {
        this.file_name = filename;
    }


    /******** class checking ********/

    public boolean checkClassName(MyClasses myClasses, String newclassname, String extend)
    {
        boolean check = true;

        // checks with other classes
        if (myClasses.searchClass(newclassname) != null) //error duplicate class name
        {
            System.err.println(file_name+":"+" error: Class "+newclassname+" has already been declared.");
            check = false;
        }

        // checks with main class
        if (myClasses.getMain_class_name().equals(newclassname))
        {
            System.err.println(file_name+":"+" error: Class "+newclassname+" has the same name with the Main class.");
            check = false;
        }

        // checks if inheritance is valid
        if (extend != null)
            check = checkInheritance(myClasses, extend) && check;

        return check;
    }

    public boolean checkInheritance(MyClasses myClasses, String extend)
    {
        // checks if inherited class exists
        ClassData extendClass = myClasses.searchClass(extend);
        if (extendClass == null)
        {
            System.err.println(file_name+":"+" error: Class "+extend+" doesn't exist.");
            return false;
        }
        return true;
    }


    /******** Method checking ********/

    public boolean evaluateMethod(MethodData method, ClassData myClass)
    {
        return checkMethodName(method, myClass)  && checkMethodOverriding(method, myClass);
    }

    public boolean checkMethodName(MethodData method, ClassData myClass)
    {
        for (MethodData methodData : myClass.getMethods())
        {
            if (methodData.getName().equals(method.getName()))
            {
                System.err.println(file_name+":"+" error: Method "+method.getName()+" has already been declared.");
                return false;
            }
        }
        return true;
    }

    public boolean checkMethodOverriding(MethodData method, ClassData myClass)
    {
        if (myClass.getExtending() == null)
            return true;

        boolean check = true;
        for (MethodData ext_method : myClass.getExtending().getMethods())
        {
            if (ext_method.getName().equals(method.getName()))  // checks names
            {
                if (!ext_method.getType().equals(method.getType())) // checks types
                {
                    System.err.println(file_name+":"+" error: \n\tMethod "+method.getName()+" has already been " +
                            "declared in upperclass "+myClass.getExtending().getName()+" with type <"+ext_method.getType()
                            +">.\n\t" + "Incompatible with declaration in class "+myClass.getName()+" with type <"
                            +method.getType()+">.");
                    check = false;
                }

                check = checkOverridingArgs(ext_method, method, myClass) && check;    // checks methods' arguments

                if (check)
                    method.setOverriding(true);
                return check;
            }
        }
        return true;
    }

    public boolean checkOverridingArgs(MethodData uppermethod, MethodData submethod, ClassData myClass)
    {
        // checks first if any or both are null and returns accordingly
        if (uppermethod.getArguments() == null)
        {
            if (submethod.getArguments() == null)
                return true;
            else
            {
                System.err.println(file_name+":"+" error: \n\tMethod "+uppermethod.getName()+" has already been " +
                    "declared in upperclass "+myClass.getExtending().getName()+" without arguments.\n\t" + "Incompatible"+
                    " with declaration in class "+myClass.getName()+" with arguments: <"+
                    submethod.argumentsToString()+">.\n\tMethods must have the same number of arguments.");
                return false;
            }
        }
        if (submethod.getArguments() == null)
        {
            System.err.println(file_name+":"+" error: \n\tMethod "+uppermethod.getName()+" has already been " +
                    "declared in upperclass "+myClass.getExtending().getName()+" with arguments: <"+uppermethod.argumentsToString()
                    +">.\n\t" + "Incompatible with declaration in class "+myClass.getName()+" without arguments.\n\t" +
                    "Methods must have the same number of arguments.");
            return false;
        }

        // checks if they have the same size
        if (uppermethod.getArguments().length != submethod.getArguments().length)
        {
            System.err.println(file_name+":"+" error: \n\tMethod "+uppermethod.getName()+" has already been " +
                    "declared in upperclass "+myClass.getExtending().getName()+" with arguments: <"+uppermethod.argumentsToString()
                    +">.\n\t" + "Incompatible with declaration in class "+myClass.getName()+" with arguments: <"+
                    submethod.argumentsToString()+">.\n\tMethods must have the same number of arguments.");
            return false;
        }

        // checks their types
        for (int i=0; i<uppermethod.getArguments().length; i+=2)//checks types only
        {
            if (!uppermethod.getArguments()[i][0].equals(submethod.getArguments()[i][0]))
            {
                System.err.println(file_name+":"+" error: \n\tMethod "+uppermethod.getName()+" has already been " +
                    "declared in upperclass "+myClass.getExtending().getName()+" with arguments: "+uppermethod.argumentsToString()
                    +".\n\t" + "Incompatible with declaration in class "+myClass.getName()+" with arguments: <"+
                    submethod.argumentsToString()+">.\n\t"+uppermethod.getArguments()[i][1]+" must be of the same type.");
                return false;
            }
        }
        return true;
    }
}
