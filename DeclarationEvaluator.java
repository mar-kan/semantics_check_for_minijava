import Symbols.AllClasses;
import Symbols.ClassData;
import Symbols.MethodData;
import Symbols.VariableData;

/**** used by Visitor1 to evaluate declarations ****/
public class DeclarationEvaluator {


    /******** Duplicate Variable checking ********/
    /** in a class **/
    public void checkFieldDuplicates(String fieldname, ClassData classData) throws CompileException
    {
        // checks fields
        for (VariableData field : classData.getFields())
        {
            if (field.getName().equals(fieldname))
                throw new CompileException(classData.getName()+": error: Field "+fieldname+" has already been declared " +
                        "in class "+classData.getName()+".");
        }

        // doesn't check inherited class' fields. allows shadowing them.
    }

    /** in a method **/
    public void checkVarMethodDuplicates(String varname, MethodData method, String classname) throws CompileException
    {
        // checks in arguments
        if (method.getArguments() != null)
        {
            for (VariableData argument : method.getArguments())
            {
                if (argument.getName().equals(varname))
                    throw new CompileException(classname+"."+method.getName()+": error: Variable "+varname+" has already" +
                            " been declared in method "+method.getName()+" as an argument.");
            }
        }

        // checks in variables
        for (VariableData var : method.getVariables())
        {
            if (var.getName().equals(varname))
                throw new CompileException(classname+"."+method.getName()+": error: Variable "+varname+" has already been " +
                        "declared in method "+method.getName()+".");
        }

        // doesn't check fields. allows shadowing them.
    }

    /** in main class **/
    public void checkVarMainDuplicates(String varname, ClassData main) throws CompileException
    {
        for (VariableData var : main.getFields())
        {
            if (var.getName().equals(varname))
                throw new CompileException("main: error: Variable " + varname + " has already been declared in main.");
        }
    }

    /******** class checking ********/
    /** duplicate check **/
    public void checkClassName(String newclassname, String extend, AllClasses allClasses) throws CompileException
    {
        // checks with other classes
        if (allClasses.searchClass(newclassname) != null) //error duplicate class name
            throw new CompileException(newclassname+": error: Class "+newclassname+" has already been declared.");

        // checks with main class
        if (allClasses.getMain_class_name().equals(newclassname))
            throw new CompileException(newclassname+": error: Class "+newclassname+" has the same name with the Main class.");

        // checks if inheritance is valid
        if (extend != null)
            checkInheritance(extend, allClasses, newclassname);
    }

    /** check for existence of inherited class **/
    public void checkInheritance(String extend, AllClasses allClasses, String baseclass) throws CompileException
    {
        ClassData extendClass = allClasses.searchClass(extend);
        if (extendClass == null && !extend.equals(allClasses.getMain_class_name()))
            throw new CompileException(baseclass+": error: Class "+extend+" doesn't exist.");
    }


    /******** Method checking ********/
    public void evaluateMethod(MethodData method, ClassData myClass) throws CompileException
    {
        checkMethodName(method, myClass);
        checkMethodOverriding(method, myClass);
    }

    /** duplicate check **/
    public void checkMethodName(MethodData method, ClassData myClass) throws CompileException
    {
        for (MethodData methodData : myClass.getMethods())
        {
            if (methodData.getName().equals(method.getName()))
                throw new CompileException(myClass.getName()+": error: Method "+method.getName()+" has already been declared in this scope.");
        }
    }

    /** check for the type of the overridden method **/
    public void checkMethodOverriding(MethodData method, ClassData myClass) throws CompileException
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
                        throw new CompileException(myClass.getName()+": error: \n\tMethod "+method.getName()+" has already " +
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
    public void checkOverridingArgs(MethodData uppermethod, MethodData submethod, ClassData myClass) throws CompileException
    {
        // checks first if any or both are null and returns accordingly
        if (uppermethod.getArguments() == null)
        {
            if (submethod.getArguments() == null)
                return;
            else
            {
                throw new CompileException(myClass.getName()+": error: \n\tMethod "+uppermethod.getName()+" has already"
                        +" been declared in upperclass "+myClass.getExtending().getName()+" without arguments.\n\t" +
                        "Incompatible with declaration in class "+myClass.getName()+" with arguments: <" +
                        submethod.getArguments_to_string()+">.\n\tMethods must have the same number of arguments.");
            }
        }
        if (submethod.getArguments() == null)
        {
            throw new CompileException(myClass.getName()+": error: \n\tMethod "+uppermethod.getName()+" has already been"
                    +" declared in upperclass "+myClass.getExtending().getName()+" with arguments: <" +
                    uppermethod.getArguments_to_string()+">.\n\t" + "Incompatible with declaration in class "+
                    myClass.getName()+" without arguments.\n\tMethods must have the same number of arguments.");
        }

        // checks if they have the same size
        if (uppermethod.getArguments().size() != submethod.getArguments().size())
        {
            throw new CompileException(myClass.getName()+": error: \n\tMethod "+uppermethod.getName()+" has already been"
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
                throw new CompileException(myClass.getName()+": error: \n\tMethod "+uppermethod.getName()+" has already " +
                        "been declared in upperclass "+myClass.getExtending().getName()+" with arguments: " +
                        uppermethod.getArguments_to_string()+".\n\tIncompatible with declaration in class " +
                        myClass.getName()+" with arguments: <"+submethod.getArguments_to_string()+">.\n\t" +
                        uppermethod.getArguments().get(i).getName()+" must be of the same type.");
            }
        }

        // checks return type of methods
        if (!uppermethod.getType().equals(submethod.getType()))
        {
            throw new CompileException(myClass.getName()+": error: Method "+uppermethod.getName()+" has already been " +
                    "declared with type " + uppermethod.getType()+".");
        }
    }
}