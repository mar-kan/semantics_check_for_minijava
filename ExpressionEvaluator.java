import Symbols.ClassData;
import Symbols.MethodData;
import Symbols.AllClasses;
import Symbols.VariableData;

import java.util.LinkedList;
import java.util.ListIterator;

public class ExpressionEvaluator {
    String file_name;   // stores file name for clearer error messages
    AllClasses allClasses;

    Utilities utils = new Utilities();


    public ExpressionEvaluator(String filename, AllClasses classes)
    {
        this.file_name = filename;
        this.allClasses = classes;
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

    public boolean compareVariableTypes(String id, String expr, String type, String scope)
    {
        if (scope == null)
            return true; // expr = op[10]

        /** managing arrays **/
        String idIndex = null, exprIndex = null;
        if (id.contains("["))
        {
            idIndex = id.substring(id.indexOf("[")+1, id.indexOf("]"));
            id = id.substring(0, id.indexOf("["));
            if (!utils.isLiteralInteger(idIndex))
            {
                System.err.println(file_name+":"+" error: \n"+idIndex+" of array "+id+"[] must be an integer.");
                return false;
            }
        }
        if (expr.contains("["))
        {
            exprIndex = expr.substring(expr.indexOf("[")+1, expr.indexOf("]"));
            expr = expr.substring(0, expr.indexOf("["));
            if (!utils.isLiteralInteger(exprIndex))
            {
                System.err.println(file_name+":"+" error: "+exprIndex+" of array "+expr+"[] must be an integer.");
                return false;
            }

            /** managing array allocation **/
            if (expr.contains("new"))
            {
                expr = expr.substring(expr.indexOf(" ")+1, expr.length());
                if (expr.equals("int"))
                    return true;
                else
                {
                    System.err.println(file_name+":"+" error: Required int, got "+expr+".");
                    return false;
                }
            }
        }

        /** managing function calls **/
        if (expr.contains("."))
            return compareMethodCall(id, expr, scope);

        // finds variables
        VariableData var1 = allClasses.findVariable(id, scope);
        VariableData var2 = allClasses.findVariable(expr, scope);

        /** checks for main arguments **/
        if (scope.equals("main") && id.equals(allClasses.getMain_argument_var()))
        {
            System.err.println(file_name+":"+" error: Main Argument "+id+" is of type String[].\nShould be of type "
                    +type+".");
            return false;
        }
        if (scope.equals("main") && expr.equals(allClasses.getMain_argument_var()))
        {
            System.err.println(file_name+":"+" error: Main Argument "+expr+" is of type String[].\nShould be of type "
                    +type+".");
            return false;
        }
        /** comparing their types **/
        switch (type)
        {
            case "int":
                if (exprIndex != null)  // expr is value of array
                {
                    if (var2.getType().equals("int[]"))
                        return true;
                    else
                    {
                        System.err.println(file_name+":"+" error: Required array, but found int: "+var2.getName());
                        return false;
                    }
                }
                return compareInts(id, expr, var1, var2);

            case "boolean":
                return compareBooleans(id, expr, var1, var2);

            case "int[]":
                if (utils.isLiteralInteger(expr))
                    return true;
                if (var2 == null)
                {
                    System.err.println(file_name+":"+" error: Variable "+expr+" hasn't been declared in this scope.");
                    return false;
                }
                if (var2.getType().equals(type))
                    return true;
                else
                {
                    System.err.println(file_name+":"+" error: Required int, but found "+var2.getType()+": "+var2.getName());
                    return false;
                }

                default: /** class **

                    ** managing allocation expressions **/
                    if (expr.contains("new"))
                    {
                        expr = expr.substring(expr.indexOf("w")+1, expr.indexOf("("));
                        if (checkForClass(var1.getType()) && var1.getType().equals(expr))
                            return true;
                        else
                        {
                            System.err.println(file_name+":"+" error: Objects "+id+", "+expr+" must be of the same type.");
                            return false;
                        }
                    }

                    /** lets objects to be assigned to null**/
                    if (expr.equals("null"))
                        return true;

                    /** managing <this> **/
                    if (expr.equals("this"))
                    {
                        String class2name = scope.substring(0, scope.indexOf("."));
                        if (checkForClass(var1.getType()) == checkForClass(class2name) && var1.getType().equals(class2name))
                            return true;
                        else
                        {
                            System.err.println(file_name+":"+" error: Objects "+id+", "+expr+" must be of the same type.");
                            return false;
                        }
                    }
                    /** comparing class types **/
                    if (checkForClass(var1.getType()) == checkForClass(var2.getType()) && var1.getType().equals(var2.getType()))
                        return true;
                    else
                    {
                        System.err.println(file_name+":"+" error: Objects "+id+", "+expr+" must be of the same type.");
                        return false;
                    }
        }
    }

    /** for allocation. checks if class name exists **/
    public boolean checkForClass(String classname)
    {
        for (ClassData aClass : allClasses.getClasses())
        {
            if (aClass.getName().equals(classname))
                return true;
        }
        System.err.println(file_name+":"+" error: Class "+classname+" doesn't exist.");
        return false;
    }

    /******** expression checking ********/

    public boolean evaluateType(String id, String type, String scope)
    {
        if (scope == null)
            return true;

        // checks first if id is a literal value
        switch (type)
        {
            case "int":
                if (utils.isLiteralInteger(id))
                    return true;
                break;

            case "boolean":
                if (utils.isLiteralBoolean(id))
                    return true;
                break;

            case "int[]":
                // no literal value for this case
                break;
            default:
                break;
        }

        if (id.contains("."))   // method call
            return checkMethodCall(id, scope) != null;

        // finds variable
        if (allClasses.findVariable(id, scope) == null)
        {
            System.err.println(file_name+":"+" error: Variable "+id+" hasn't been declared in this scope.");
            return false;
        }
        else return true;
    }

    public boolean compareBooleans(String id, String expr, VariableData var1, VariableData var2)
    {
        if (!utils.isLiteralBoolean(id) && var1 == null)   // id doesnt exist
        {
            System.err.println(file_name+":"+" error: Variable "+id+" hasn't been declared in this scope.");
            return false;
        }
        if (!utils.isLiteralBoolean(expr) && var2 == null)   // expr doesnt exist
        {
            System.err.println(file_name+":"+" error: Variable "+expr+" hasn't been declared in this scope.");
            return false;
        }

        if (utils.isLiteralBoolean(id))         // id is literal
        {
            if (utils.isLiteralBoolean(expr))   // expr is literal
                return true;
            else
            {
                if (var2.getType().equals("boolean"))   // expr is bool variable
                    return true;
                else
                {
                    System.err.println(file_name+":"+" error: Required boolean, but found "+var2.getType()+": "+var2.getName());
                    return false;
                }
            }
        }
        else if (utils.isLiteralBoolean(expr))  // expr is literal
        {
            if (var1.getType().equals("boolean"))   // id ia bool variable
                return true;
            else
            {
                System.err.println(file_name+":"+" error: Required "+var1.getType()+", but found boolean: "+expr);
                return false;
            }
        }
        else
        {
            if (var2.getType().equals("boolean") && var1.getType().equals("boolean"))   // both bool variables
                return true;
            else
            {
                System.err.println(file_name+":"+" error: Required "+var1.getType()+", but found "+var2.getType()+": "+var2.getName());
                return false;
            }
        }
    }

    public boolean compareInts(String id, String expr, VariableData var1, VariableData var2)
    {
        if (!utils.isLiteralInteger(id) && var1 == null)   // id doesnt exist
        {
            System.err.println(file_name+":"+" error: Variable "+id+" hasn't been declared in this scope.");
            return false;
        }
        if (!utils.isLiteralInteger(expr) && var2 == null)   // expr doesnt exist
        {
            System.err.println(file_name+":"+" error: Variable "+expr+" hasn't been declared in this scope.");
            return false;
        }
        if (utils.isLiteralInteger(id)) // id is literal
        {
            if (utils.isLiteralInteger(expr))   // expr also literal
                return true;

            if (var2.getType().equals("int"))    // expr is int variable
                return true;
            else
            {
                System.err.println(file_name+":"+" error: Required int"+", but found "+var2.getType()+": "+var2.getName());
                return false;
            }
        }
        else if (utils.isLiteralInteger(expr))  // expr is literal
        {
            if (var1.getType().equals("int"))    // id is int variable
                return true;
            else
            {
                System.err.println(file_name+":"+" error: Required "+var1.getType()+", but found int: "+var2.getName());
                return false;
            }
        }
        else
        {
            if (var2.getType().equals("int") && var1.getType().equals("int"))    // both are int variables
                return true;
            else
            {
                System.err.println(file_name+":"+" error: Required "+var1.getType()+", but found "+var2.getType()+": "+var2.getName());
                return false;
            }
        }
    }

    public boolean compareMethodCall(String id, String expr, String scope)
    {
        String methodname = expr.substring(expr.indexOf(".")+1, expr.indexOf("("));
        MethodData exprMethod = checkMethodCall(expr, scope);
        if (exprMethod == null)
        {
            System.err.println(file_name+":"+" error: Method "+methodname+" has not been declared in this scope.");
            return false;
        }

        // finds variable id
        VariableData varId = allClasses.findVariable(id, scope);
        if (varId == null)
        {
            /** checks for main's argument **/
            if (scope.equals("main") && id.equals(allClasses.getMain_argument_var()))
            {
                System.err.println(file_name+":"+" error: Main Argument "+id+" is of type String[].\nShould be the same type"+
                        "with method "+methodname+".");
                return false;
            }
            System.err.println(file_name+":"+" error: Variable "+id+" has not been declared in this scope.");
            return false;
        }

        // checks method's return type with type of id
        if (!exprMethod.getType().equals(varId.getType()))
        {
            System.err.println(file_name+":"+" error: Method "+methodname+" and variable "+id+" should be of the same type.");
            return false;
        }

        return true;
    }

    public MethodData checkMethodCall(String id, String scope)
    {
        ClassData myClass = null;

        String objectname = id.substring(0, id.indexOf("."));
        String methodname = id.substring(id.indexOf(".")+1, id.indexOf("("));
        String arguments = id.substring(id.indexOf("(")+1, id.indexOf(")"));
/*** id = (!(current_node.GetHas_Right()));***/
        // finds object objectname
        VariableData obj = allClasses.findVariable(objectname, scope);
        if (obj == null)
        {
            if (objectname.equals("this"))
            {
                String classname = scope.substring(0, scope.indexOf("."));
                myClass = allClasses.searchClass(classname);
            }
            else
            {
                System.err.println(file_name+":"+" error: Variable "+objectname+" has not been declared in this scope.");
                return null;
            }
        }

        // finds its class
        if (myClass == null)
            myClass = allClasses.searchClass(obj.getType());
        if (myClass == null)
        {
            System.err.println(file_name+":"+" error: "+obj.getType()+" isn't a data type.");
            return null;
        }

        // finds method methodname
        MethodData myMethod = myClass.searchMethod(methodname);
        if (myMethod == null)
        {
            System.err.println(file_name+":"+" error: Method "+methodname+" has not been declared in this scope.");
            return null;
        }

        // checks the arguments
        if (!compareMethodArgs(myMethod.getArguments(), arguments, scope, methodname))
        {
            System.err.println(file_name+":"+" error: Method "+methodname+"'s arguments should match the given argument types.");
            return null;
        }

        return myMethod;
    }

    public boolean compareMethodArgs(LinkedList<VariableData> methodArgs, String callArgs, String scope, String methodname)
    {
        //b, a, c, t
        if (methodArgs == null)
        {
            if (!callArgs.equals(""))
            {
                System.err.println(file_name+":"+" error: Method "+methodname+" doesn't expect any arguments.");
                return false;
            }
            else
                return true;
        }

        if (callArgs.equals(""))
        {
            System.err.println(file_name+":"+" error: Method "+methodname+" expects "+methodArgs.size()+" arguments instead of none.");
            return false;
        }

        String[] split_args = callArgs.split(" ");
        for (int i=0; i<split_args.length; i++)
        {
            if (split_args[i].contains(","))    // removes commas
                split_args[i] = split_args[i].substring(0, split_args[i].indexOf(","));

            VariableData arg = allClasses.findVariable(split_args[i], scope);
            if (arg == null)
            {
                /** checks for literal value **/
                if (utils.isLiteralBoolean(split_args[i]) && methodArgs.get(i).getType().equals("boolean"))
                    return true;
                if (utils.isLiteralInteger(split_args[i]) && methodArgs.get(i).getType().equals("int"))
                    return true;
                if (utils.isLiteralValue(split_args[i]))    // literal value that doesn't match
                {
                    System.err.println(file_name+":"+" error: Argument "+split_args[i]+" of method "+methodname+" should be " +
                            "of type "+methodArgs.get(i).getType()+".");
                    return false;
                }
                /** checks for main's argument **/
                if (scope.equals("main") && split_args[i].equals(allClasses.getMain_argument_var()))
                {
                    System.err.println(file_name+":"+" error: Main Argument "+split_args[i]+" is of type String[].\nShould be the same type"+
                            "with method "+methodname+".");
                    return false;
                }
                /** checks for method call **/
                if (split_args[i].contains("."))
                    return compareMethodCall(methodArgs.get(i).getName(), split_args[i], scope);

                System.err.println(file_name+":"+" error: Variable "+split_args[i]+" hasn't been declared in this scope.");
                return false;
            }

            if (!arg.getType().equals(methodArgs.get(i).getType()))
            {
                System.err.println(file_name+":"+" error: Argument "+arg.getName()+" of method "+methodname+" should be " +
                        "of type "+methodArgs.get(i).getType()+".");
                return false;
            }
        }
        return true;
    }
}
