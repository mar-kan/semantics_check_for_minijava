package myVisitors.evaluators;

import symbols.AllClasses;
import symbols.ClassData;
import symbols.MethodData;
import symbols.VariableData;

/**** used by MyVisitors.Visitor1 to evaluate declarations ****/
public class DeclarationEvaluator {


    /******** Duplicate Variable checking ********/
    /** in a class **/
    public void checkFieldDuplicates(String fieldname, ClassData classData) throws Exception
    {
        // checks fields
        for (VariableData field : classData.getFields())
        {
            if (field.getName().equals(fieldname))
                throw new Exception(classData.getName()+": error: Field "+fieldname+" has already been declared " +
                        "in this scope.");
        }

        // doesn't check inherited class' fields. allows shadowing them.
    }

    /** in a method **/
    public void checkVarMethodDuplicates(String varname, MethodData method, String classname) throws Exception
    {
        // checks in arguments
        if (method.getArguments() != null)
        {
            for (VariableData argument : method.getArguments())
            {
                if (argument.getName().equals(varname))
                    throw new Exception(classname+"."+method.getName()+": error: Variable "+varname+" has already" +
                            " been declared in method this scope as an argument.");
            }
        }

        // checks in variables
        for (VariableData var : method.getVariables())
        {
            if (var.getName().equals(varname))
                throw new Exception(classname+"."+method.getName()+": error: Variable "+varname+" has already been " +
                        "declared in this scope.");
        }

        // doesn't check fields. allows shadowing them.
    }

    /** in main class **/
    public void checkVarMainDuplicates(String varname, ClassData main) throws Exception
    {
        for (VariableData var : main.getFields())
        {
            if (var.getName().equals(varname))
                throw new Exception("main: error: Variable " + varname + " has already been declared in this scope.");
        }
    }

    /******** class checking ********/
    /** duplicate check **/
    public void checkClassName(String newclassname, String extend, AllClasses allClasses) throws Exception
    {
        // checks with other classes
        if (allClasses.searchClass(newclassname) != null) //error duplicate class name
            throw new Exception(newclassname+": error: Class "+newclassname+" has already been declared.");

        // checks with main class
        if (allClasses.getMain_class_name().equals(newclassname))
            throw new Exception(newclassname+": error: Class "+newclassname+" has the same name with the Main class.");

        // checks if inheritance is valid
        if (extend != null)
            checkInheritance(extend, allClasses, newclassname);
    }

    /** check for existence of inherited class **/
    public void checkInheritance(String extend, AllClasses allClasses, String baseclass) throws Exception
    {
        ClassData extendClass = allClasses.searchClass(extend);
        if (extendClass == null && !extend.equals(allClasses.getMain_class_name()))
            throw new Exception(baseclass+": error: Class "+extend+" doesn't exist.");
    }


    /******** Method checking ********/
    public void evaluateMethod(MethodData method, ClassData myClass) throws Exception
    {
        checkMethodName(method, myClass);
        checkMethodOverriding(method, myClass);
    }

    /** duplicate check **/
    public void checkMethodName(MethodData method, ClassData myClass) throws Exception
    {
        for (MethodData methodData : myClass.getMethods())
        {
            if (methodData.getName().equals(method.getName()))
                throw new Exception(myClass.getName()+": error: Method "+method.getName()+" has already been declared in this scope.");
        }
    }

    /** check for the type of the overridden method **/
    public void checkMethodOverriding(MethodData method, ClassData myClass) throws Exception
    {
        if (myClass.getExtending() == null)
            return;

        ClassData temp = myClass.getExtending();
        while (temp != null)    // checks every class until the superclass
        {
            for (MethodData ext_method : temp.getMethods())
            {
                if (ext_method.getName().equals(method.getName()))  // checks names
                {
                    if (!ext_method.getType().equals(method.getType())) // checks types
                    {
                        throw new Exception(myClass.getName()+": error: \n\tMethod "+method.getName()+" has already " +
                                "been declared in upperclass "+temp.getName()+" with type <"+ext_method.getType()
                                +">.\n\t" + "Incompatible with declaration in class "+myClass.getName()+" with type <"
                                +method.getType()+">.");
                    }

                    checkOverridingArgs(ext_method, method, myClass);    // checks methods' arguments
                    method.setOverriding(true);
                }
            }
            temp = temp.getExtending();
        }
    }

    /** check for the number and types of the arguments of the overridden method **/
    public void checkOverridingArgs(MethodData uppermethod, MethodData submethod, ClassData myClass) throws Exception
    {
        // checks first if any or both are null and returns accordingly
        if (uppermethod.getArguments() == null)
        {
            if (submethod.getArguments() == null)
                return;
            else
            {
                throw new Exception(myClass.getName()+": error: \n\tMethod "+uppermethod.getName()+" has already"
                        +" been declared in upperclass "+myClass.getExtending().getName()+" without arguments.\n\t" +
                        "Incompatible with declaration in class "+myClass.getName()+" with arguments: <" +
                        submethod.getArguments_to_string()+">.\n\tMethods must have the same number of arguments.");
            }
        }
        if (submethod.getArguments() == null)
        {
            throw new Exception(myClass.getName()+": error: \n\tMethod "+uppermethod.getName()+" has already been"
                    +" declared in upperclass "+myClass.getExtending().getName()+" with arguments: <" +
                    uppermethod.getArguments_to_string()+">.\n\t" + "Incompatible with declaration in class "+
                    myClass.getName()+" without arguments.\n\tMethods must have the same number of arguments.");
        }

        // checks if they have the same size
        if (uppermethod.getArguments().size() != submethod.getArguments().size())
        {
            throw new Exception(myClass.getName()+": error: \n\tMethod "+uppermethod.getName()+" has already been"
                    +" declared in upperclass "+myClass.getExtending().getName()+" with arguments: <" +
                    uppermethod.getArguments_to_string()+">.\n\tIncompatible with declaration in class "+myClass.getName()
                    +" with arguments: <"+submethod.getArguments_to_string()+">.\n\tMethods must have the same number of " +
                    "arguments.");
        }

        // checks their types
        for (int i=0; i<uppermethod.getArguments().size(); i++)     // checks types only
        {
            if (!uppermethod.getArguments().get(i).getType().equals(submethod.getArguments().get(i).getType()))
            {
                throw new Exception(myClass.getName()+": error: \n\tMethod "+uppermethod.getName()+" has already " +
                        "been declared in upperclass "+myClass.getExtending().getName()+" with arguments: " +
                        uppermethod.getArguments_to_string()+".\n\tIncompatible with declaration in class " +
                        myClass.getName()+" with arguments: <"+submethod.getArguments_to_string()+">.\n\t" +
                        uppermethod.getArguments().get(i).getName()+" must be of the same type.");
            }
        }

        // checks return type of methods
        if (!uppermethod.getType().equals(submethod.getType()))
        {
            throw new Exception(myClass.getName()+": error: Method "+uppermethod.getName()+" has already been " +
                    "declared with type " + uppermethod.getType()+".");
        }
    }
}