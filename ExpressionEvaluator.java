import Symbols.ClassData;
import Symbols.AllClasses;
import Symbols.VariableData;

import java.util.LinkedList;

public class ExpressionEvaluator {

    String file_name;   // stores file name for clearer error messages
    AllClasses allClasses;


    public ExpressionEvaluator(String filename, AllClasses classes)
    {
        this.file_name = filename;
        this.allClasses = classes;
    }

    public void compareVariableTypes(String id, String expr, String type, String scope) throws CompileException
    {
        evaluateType(id, type, scope);
        evaluateType(expr, type, scope);
    }


    /******** expression checking ********/

    public void evaluateType(String id, String type, String scope) throws CompileException
    {
        if (id.equals(type))
            return;

        if (id.equals("int") || id.equals("boolean") || id.equals("int[]"))
            throw new CompileException(file_name+":"+" error: Required "+type+", but found "+id+".");

        // finds variable
        VariableData var = allClasses.findVariable(id, scope);
        if (var == null)
        {
            if ((id.equals("this") || id.equals("null")) && !type.equals("int") && !type.equals("boolean") && !type.equals("int[]"))   // type = class
                return;

            // checking if id is a classname
            if (allClasses.searchClass(id) != null)
                throw new CompileException(file_name+":"+" error: Required "+type+", but found "+id+".");

            throw new CompileException(file_name+":"+" error: Variable "+id+" hasn't been declared in this scope.");
        }

        if (!var.getType().equals(type))
            throw new CompileException(file_name+":"+" error: Required "+type+", but found "+var.getType()+".");
    }


    public void compareMethodArgs(LinkedList<VariableData> methodArgs, String callArgs, String scope, String methodname) throws CompileException
    {
        //b, a, c, t
        if (methodArgs == null)
        {
            if (!callArgs.equals(""))
                throw new CompileException(file_name+":"+" error: Method "+methodname+" doesn't expect any arguments.");
            else
                return;
        }

        if (callArgs.equals(""))
            throw new CompileException(file_name+":"+" error: Method "+methodname+" expects "+methodArgs.size()+" arguments instead of none.");

        String[] split_args = callArgs.split(" ");
        if (split_args.length != methodArgs.size())
            throw new CompileException(file_name+":"+" error: Method "+methodname+" expects "+methodArgs.size()+" arguments instead of "+split_args.length+".");

        for (int i=0; i<split_args.length; i++)
        {
            if (split_args[i].contains(","))    // removes commas
                split_args[i] = split_args[i].substring(0, split_args[i].indexOf(","));

            if (methodArgs.get(i).getType().equals(split_args[i]))
                continue;

            if (split_args[i].equals("this"))
            {
                if (scope.equals("main"))
                    throw new CompileException(file_name+": error: \"this\" isn't valid in main program.");
                String classname;
                if (scope.contains("."))
                    classname = scope.substring(0, scope.indexOf("."));
                else
                    classname = scope;

                if (classname.equals(methodArgs.get(i).getType()))
                    return;
                else
                    throw new CompileException(file_name+":"+" error: Argument "+split_args[i]+" of method "+methodname+" should be " +
                            "of type "+methodArgs.get(i).getType()+".");
            }

            VariableData arg = allClasses.findVariable(split_args[i], scope);
            if (arg == null)
                throw new CompileException(file_name+":"+" error: Variable "+split_args[i]+" hasn't been declared in this scope.");

            if (!arg.getType().equals(methodArgs.get(i).getType()))
            {
                ClassData myClass = allClasses.searchClass(arg.getType());
                while (myClass.getExtending() != null)                      // checks for upperclass types
                {
                    if (myClass.getExtending().getName().equals(methodArgs.get(i).getType()))
                        return;
                    myClass = myClass.getExtending();
                }
                throw new CompileException(file_name+":"+" error: Argument "+arg.getName()+" of method "+methodname+" should be " +
                        "of type "+methodArgs.get(i).getType()+".");
            }
        }
    }
}
