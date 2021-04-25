import Symbols.ClassData;
import Symbols.MethodData;
import Symbols.MyClasses;
import Symbols.VariableData;

import java.security.KeyPair;
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

    /** for allocation. checks if class name exists **/
    public boolean checkForClass(String classname, MyClasses classes)
    {
        for (ClassData aClass : classes.getClasses())
        {
            if (aClass.getName().equals(classname))
                return true;
        }
        System.err.println(file_name+":"+" error: Class "+classname+" doesn't exist.");
        return false;
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
        return true;
    }


    /******** Variable checking ********/
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
        // checks in arguments
        /*if (main.getArguments() != null)
        {
            for (String[] argument : main.getArguments())
            {
                if (argument[1].equals(varname))
                {
                    System.err.println(file_name+":"+" error: Variable "+varname+" was already defined in method "+method.getName()+" as an argument.");
                    return false;
                }
            }
        }*/

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

    public boolean compareVariableTypes(VariableData var1, VariableData var2)
    {
        if (!var1.getType().equals(var2.getType()))
        {
            System.err.println(file_name+":"+" error: Variables "+var1+", "+var2+" must be of the same type.");
            return false;
        }
        return true;
    }

    public boolean compareVariableTypes(String id, String expr, String type, String scope, MyClasses myClasses)
    {
        if (scope == null)
            return true;

        // finds variables
        VariableData var1 = null, var2 = null;
        if (scope.contains(".")) // in method of class
        {
            String classname, method;
            classname = scope.substring(0, scope.indexOf("."));
            method = scope.substring(scope.indexOf(".")+1, scope.length());

            MethodData methodData = myClasses.searchClass(classname).searchMethod(method);

            var1 = methodData.searchVariable(id);
            var2 = methodData.searchVariable(expr);
        }
        else if (scope.equals("main"))  // in main
        {
            var1 = myClasses.getMainClass().searchVariable(id);
            var2 = myClasses.getMainClass().searchVariable(expr);
        }
        else // in class
        {
            ClassData aClass = myClasses.searchClass(scope);
            var1 = aClass.searchVariable(id);
            var2 = aClass.searchVariable(expr);
        }

        // compares values
        switch (type)
        {
            case "int":
                if (isLiteralInteger(id))
                {
                    if (isLiteralInteger(expr))
                        return true;
                    else
                        return var2.getType().equals(type);
                }
                else if (isLiteralInteger(expr))
                    return var1.getType().equals(type);
                else
                    return var1.getType().equals(var2.getType());

            case "boolean":
                if (isLiteralBoolean(id))
                {
                    if (isLiteralBoolean(expr))
                        return true;
                    else
                        return var2.getType().equals(type);
                }
                else if (isLiteralBoolean(expr))
                    return var1.getType().equals(type);
                else
                    return var1.getType().equals(var2.getType());

            case "int[]":
                if (isLiteralInteger(expr) || var2.getType().equals(type))
                    return true;

            case "class":   // checks only if the class was declared
                if (checkForClass(id, myClasses) == checkForClass(expr, myClasses))
                    return true;
                else
                {
                    System.err.println(file_name+":"+" error: "+id+", "+expr+" must be of the same type.");
                    return false;
                }

            default:
                return false;
        }
    }


    /******** expression checking ********/

    public boolean evaluateType(String id, String type, String scope, MyClasses myClasses)
    {
        if (scope == null)
            return true;

        // checks first if id is a literal value
        switch (type)
        {
            case "int":
                if (isLiteralInteger(id))
                    return true;
                break;

            case "boolean":
                if (isLiteralBoolean(id))
                    return true;
                break;

            /*case "int[]":
                // no literal value for this case
                break;*/

            case "class":   // checks only if the class was declared
                if (checkForClass(id, myClasses))
                    return true;
                else
                {
                    System.err.println(file_name + ":" + " error: Class " + id + " hasn't been declared.");
                    return false;
                }

            default:
                break;
        }

        // finds variable
        if (scope.contains(".")) // in method of class
        {
            String classname, method;
            classname = scope.substring(0, scope.indexOf("."));
            method = scope.substring(scope.indexOf(".")+1, scope.length());

            MethodData methodData = myClasses.searchClass(classname).searchMethod(method);

            VariableData var = methodData.searchVariable(id);
            if (var == null)
            {
                var = myClasses.searchClass(classname).searchVariable(id);
                if (var == null)
                {
                    System.err.println(file_name + ":" + " error: Variable " + id + " has not been declared in this scope.");
                    return false;
                }
            }
            if (var.getType().equals(type))
                return true;
            else
            {
                System.err.println(file_name + ":" + " error: Variable "+id+" is of type "+var.getType()+".\n"+id+
                        " should be of type "+type+".");
                return false;
            }
        }
        else if (scope.equals("main"))  // in main
        {
            VariableData var = myClasses.getMainClass().searchVariable(id);
            if (var == null)
            {
                System.err.println(file_name + ":" + " error: Variable " + id + " has not been declared in this scope.");
                return false;
            }
            if (var.getType().equals(type))
                return true;
            else
            {
                System.err.println(file_name + ":" + " error: Variable "+id+" is of type "+var.getType()+".\n"+id+
                        " should be of type "+type+".");
                return false;
            }
        }
        else // in class
        {
            ClassData aClass = myClasses.searchClass(scope);
            VariableData var = aClass.searchVariable(id);

            if (var == null)
            {
                System.err.println(file_name + ":" + " error: Variable " + id + " has not been declared in this scope.");
                return false;
            }
            if (var.getType().equals(type))
                return true;
            else
            {
                System.err.println(file_name + ":" + " error: Variable "+id+" is of type "+var.getType()+".\n"+id+
                        " should be of type "+type+".");
                return false;
            }
        }
    }

    /** checks literal value of int **/
    public boolean isLiteralInteger(String id)
    {
        for (int i=0; i<id.length(); i++)
        {
            if (id.charAt(i) < '0' || id.charAt(i) > '9')
                return false;
        }
        return true;
    }

    public boolean isLiteralBoolean(String id)
    {
        return id.equals("true") || id.equals("false");
    }
}
