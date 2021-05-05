import Symbols.AllClasses;
import Symbols.ClassData;
import Symbols.VariableData;

import java.util.LinkedList;

public class ExpressionEvaluator {

    AllClasses allClasses;


    public ExpressionEvaluator(AllClasses classes)
    {
        this.allClasses = classes;
    }

    /** evaluates both expressions with given type **/
    public void compareVariableTypes(String id, String expr, String type, String scope) throws CompileException
    {
        evaluateType(id, type, scope);
        evaluateType(expr, type, scope);
    }

    /** evaluates an expression with given type in given scope **/
    public void evaluateType(String id, String type, String scope) throws CompileException
    {
        if (id.equals(type))
            return;

        if (id.equals("int") || id.equals("boolean") || id.equals("int[]"))
            throw new CompileException(scope+": error: Required "+type+", but found "+id+".");

        // finds variable in its scope
        VariableData var = allClasses.findVariable(id, scope);
        if (var == null)
        {
            if ((id.equals("this") || id.equals("null")) && !type.equals("int") && !type.equals("boolean") && !type.equals("int[]"))   // type = class
                return;

            // checking if id is a classname
            if (allClasses.searchClass(id) != null)
            {
                if (allClasses.searchClass(id).checkInheritance(type))
                    return;

                throw new CompileException(scope+": error: Required "+type+", but found "+id+".");
            }

            //didn't find id
            throw new CompileException(scope+": error: Variable "+id+" hasn't been declared in this scope.");
        }

        if (!var.getType().equals(type))
        {
            // checks inheritance
            ClassData aClass = allClasses.searchClass(type);
            while (aClass != null)
            {
                if (aClass.getName().equals(var.getType()))
                    return;

                aClass = aClass.getExtending();
            }

            throw new CompileException(scope+": error: Required "+type+", but found "+var.getType()+".");
        }
    }

    /** compares argument types of a call of a method with the declaration of the method **/
    public void compareMethodArgs(LinkedList<VariableData> methodArgs, String callArgs, String scope, String methodname) throws CompileException
    {
        if (methodArgs == null)
        {
            if (!callArgs.equals(""))
                throw new CompileException(scope+": error: Method "+methodname+" doesn't expect any arguments.");
            else
                return;
        }

        if (callArgs.equals(""))
            throw new CompileException(scope+": error: Method "+methodname+" expects "+methodArgs.size()+" arguments instead of none.");

        String[] split_args = callArgs.split(" ");
        if (split_args.length != methodArgs.size())
            throw new CompileException(scope+": error: Method "+methodname+" expects "+methodArgs.size()+" arguments instead of "+split_args.length+".");

        for (int i=0; i<split_args.length; i++)
        {
            if (split_args[i].contains(","))    // removes commas
                split_args[i] = split_args[i].substring(0, split_args[i].indexOf(","));

            if (methodArgs.get(i).getType().equals(split_args[i]))
                continue;

            if (split_args[i].equals("this"))
            {
                if (scope.equals("main"))
                    throw new CompileException(scope+": error: \"this\" isn't valid in main program.");
                String classname;
                if (scope.contains("."))
                    classname = scope.substring(0, scope.indexOf("."));
                else
                    classname = scope;

                // checks for class or upperclass type
                ClassData aClass = allClasses.searchClass(classname);
                while (aClass != null)
                {
                    if (aClass.getName().equals(methodArgs.get(i).getType()))
                        return;
                    aClass = aClass.getExtending();
                }
                throw new CompileException(scope+": error: Argument "+split_args[i]+" of method "+methodname+" should be " +
                            "of type "+methodArgs.get(i).getType()+".");
            }

            VariableData arg = allClasses.findVariable(split_args[i], scope);
            if (arg == null)
            {
                // checks for class or upperclass type
                ClassData aClass = allClasses.searchClass(split_args[i]);
                while (aClass != null)
                {
                    if (aClass.getName().equals(methodArgs.get(i).getType()))
                        return;
                    aClass = aClass.getExtending();
                }
                throw new CompileException(scope+": error: Variable "+split_args[i]+" hasn't been declared in this scope.");
            }

            if (!arg.getType().equals(methodArgs.get(i).getType()))
            {
                ClassData myClass = allClasses.searchClass(arg.getType());
                while (myClass.getExtending() != null)                      // checks for upperclass types
                {
                    if (myClass.getExtending().getName().equals(methodArgs.get(i).getType()))
                        return;
                    myClass = myClass.getExtending();
                }
                throw new CompileException(scope+": error: Argument "+arg.getName()+" of method "+methodname+" should be " +
                        "of type "+methodArgs.get(i).getType()+".");
            }
        }
    }
}
