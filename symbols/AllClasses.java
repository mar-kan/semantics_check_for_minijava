package symbols;

import java.util.LinkedList;


public class AllClasses {

    String main_class_name;
    private ClassData mainClass;
    private LinkedList<ClassData> classes;


    public AllClasses()
    {
        this.main_class_name = "";
        this.mainClass = new ClassData("main", null);
        this.classes = new LinkedList<>();
    }

    /** adds a class in classes list **/
    public void addClass(String classname, ClassData classtype)
    {
        ClassData newclass = new ClassData(classname, classtype);
        this.classes.add(newclass);
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
        if (scope == null)
            return null;

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

    /** printing offsets function **/
    public void printClassOffsets()
    {
        int var_offset = 0, method_offset = 0;
        for (ClassData aClass : classes)
        {
            System.out.println("-----------Class "+aClass.getName()+"-----------");

            System.out.println("--Variables---");
            if (aClass.getExtending() == null)
                var_offset = aClass.printVarOffsets(aClass.getName(), 0);
            else
                var_offset = aClass.printVarOffsets(aClass.getName(), var_offset);

            System.out.println("---Methods---");
            if (aClass.getExtending() == null)
                method_offset = aClass.printMethodOffsets(aClass.getName(), 0);
            else
                method_offset = aClass.printMethodOffsets(aClass.getName(), method_offset);

            System.out.println();
        }
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
}
