import Symbols.AllClasses;
import Symbols.ClassData;
import Symbols.MethodData;
import Symbols.VariableData;

public class DeclarationEvaluator {
    private String file_name;

    DeclarationEvaluator(String filename)
    {
        this.file_name = filename;
    }


    /******** Duplicate Variable checking ********/
    public boolean checkFieldDuplicates(String fieldname, ClassData classData)
    {
        // checks fields
        for (VariableData field : classData.getFields())
        {
            if (field.getName().equals(fieldname))
            {
                System.err.println(file_name+":"+" error: Field "+fieldname+" was already defined in class "+classData.getName()+".");
                return false;
            }
        }
        return true;
    }

    public boolean checkVarMethodDuplicates(String varname, MethodData method)
    {
        // checks in arguments
        if (method.getArguments() != null)
        {
            for (VariableData argument : method.getArguments())
            {
                if (argument.getName().equals(varname))
                {
                    System.err.println(file_name+":"+" error: Variable "+varname+" was already defined in method "+method.getName()+" as an argument.");
                    return false;
                }
            }
        }

        // checks in variables
        for (VariableData var : method.getVariables())
        {
            if (var.getName().equals(varname))
            {
                System.err.println(file_name+":"+" error: Variable "+varname+" was already defined in method "+method.getName()+".");
                return false;
            }
        }

        return true;
    }

    public boolean checkVarMainDuplicates(String varname, ClassData main)
    {
        // check var name

        // checks in variables
        for (VariableData var : main.getFields())
        {
            if (var.getName().equals(varname))
            {
                System.err.println(file_name + ":" + " error: Variable " + varname + " was already defined in main.");
                return false;
            }
        }
        return true;
    }

    /******** class checking ********/

    public boolean checkClassName(String newclassname, String extend, AllClasses allClasses)
    {
        boolean check = true;

        // checks with other classes
        if (allClasses.searchClass(newclassname) != null) //error duplicate class name
        {
            System.err.println(file_name+":"+" error: Class "+newclassname+" has already been declared.");
            check = false;
        }

        // checks with main class
        if (allClasses.getMain_class_name().equals(newclassname))
        {
            System.err.println(file_name+":"+" error: Class "+newclassname+" has the same name with the Main class.");
            check = false;
        }

        // checks if inheritance is valid
        if (extend != null)
            check = checkInheritance(extend, allClasses) && check;

        return check;
    }

    public boolean checkInheritance(String extend, AllClasses allClasses)
    {
        // checks if inherited class exists
        ClassData extendClass = allClasses.searchClass(extend);
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
        ClassData temp = myClass.getExtending();
        while (temp != null)    // checks every class until the superclass
        {
            for (MethodData ext_method : temp.getMethods())
            {
                if (ext_method.getName().equals(method.getName()))  // checks names
                {
                    if (!ext_method.getType().equals(method.getType())) // checks types
                    {
                        System.err.println(file_name+":"+" error: \n\tMethod "+method.getName()+" has already been " +
                                "declared in upperclass "+temp.getName()+" with type <"+ext_method.getType()
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
            temp = temp.getExtending();
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
                        submethod.getArguments_to_string()+">.\n\tMethods must have the same number of arguments.");
                return false;
            }
        }
        if (submethod.getArguments() == null)
        {
            System.err.println(file_name+":"+" error: \n\tMethod "+uppermethod.getName()+" has already been " +
                    "declared in upperclass "+myClass.getExtending().getName()+" with arguments: <"+uppermethod.getArguments_to_string()
                    +">.\n\t" + "Incompatible with declaration in class "+myClass.getName()+" without arguments.\n\t" +
                    "Methods must have the same number of arguments.");
            return false;
        }

        // checks if they have the same size
        if (uppermethod.getArguments().size() != submethod.getArguments().size())
        {
            System.err.println(file_name+":"+" error: \n\tMethod "+uppermethod.getName()+" has already been " +
                    "declared in upperclass "+myClass.getExtending().getName()+" with arguments: <"+uppermethod.getArguments_to_string()
                    +">.\n\t" + "Incompatible with declaration in class "+myClass.getName()+" with arguments: <"+
                    submethod.getArguments_to_string()+">.\n\tMethods must have the same number of arguments.");
            return false;
        }

        // checks their types
        for (int i=0; i<uppermethod.getArguments().size(); i++)     // checks types only
        {
            if (!uppermethod.getArguments().get(i).getType().equals(submethod.getArguments().get(i).getType()))
            {
                System.err.println(file_name+":"+" error: \n\tMethod "+uppermethod.getName()+" has already been " +
                        "declared in upperclass "+myClass.getExtending().getName()+" with arguments: "+uppermethod.getArguments_to_string()
                        +".\n\t" + "Incompatible with declaration in class "+myClass.getName()+" with arguments: <"+
                        submethod.getArguments_to_string()+">.\n\t"+uppermethod.getArguments().get(i).getName()+" must be of the same type.");
                return false;
            }
        }

        // checks return type of methods
        if (!uppermethod.getType().equals(submethod.getType()))
        {
            System.err.println(file_name+":"+" error: Method "+uppermethod.getName()+" has already been declared with type " +
                    uppermethod.getType()+".");
            return false;
        }
        return true;
    }
}


