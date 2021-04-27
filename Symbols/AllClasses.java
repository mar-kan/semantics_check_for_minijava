package Symbols;

import java.util.LinkedList;


public class AllClasses {

    String main_class_name;
    private ClassData mainClass;
    private LinkedList<ClassData> classes;
    private String main_argument_var;


    public AllClasses()
    {
        this.main_class_name = "";
        this.mainClass = new ClassData("main", null);
        this.classes = new LinkedList<ClassData>();
    }

    /** adds a class in classes list **/
    public ClassData addClass(String classname, ClassData classtype)
    {
        ClassData newclass = new ClassData(classname, classtype);
        this.classes.add(newclass);
        return new ClassData(classname, classtype);
    }

    /** searches list class for a ClassData with name = classname **/
    public ClassData searchClass(String classname)
    {

        for (ClassData aClass : classes)
        {
            if (aClass.getName().equals(classname))
                return aClass;
        }
        return null;
    }

    /** finds variable anywhere in the classes according to <scope> **/
    public VariableData findVariable(String id, String scope)
    {
        VariableData var;

        if (scope.contains(".")) // in method of class
        {
            String classname, method;
            classname = scope.substring(0, scope.indexOf("."));
            method = scope.substring(scope.indexOf(".")+1, scope.length());

            MethodData methodData = searchClass(classname).searchMethod(method);

            var = methodData.searchVariable(id);
            if (var == null)
                var = searchClass(classname).searchVariable(id);
        }
        else if (scope.equals("main"))  // in main
            var = mainClass.searchVariable(id);
        else // in class
        {
            ClassData aClass = searchClass(scope);
            var = aClass.searchVariable(id);
        }
        return var;
    }

    /** setters and getters **/
    public void setMain_class_name(String mainname)
    {
        this.main_class_name = mainname;
    }

    public String getMain_class_name()
    {
        return this.main_class_name;
    }

    public LinkedList<ClassData> getClasses()
    {
        return this.classes;
    }

    public ClassData getMainClass()
    {
        return mainClass;
    }

    public void setMain_argument_var(String main_argument_var)
    {
        this.main_argument_var = main_argument_var;
    }

    public String getMain_argument_var()
    {
        return main_argument_var;
    }
}
