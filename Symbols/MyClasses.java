package Symbols;

import java.util.LinkedList;


public class MyClasses {

    String main_class_name;
    private ClassData mainClass;
    private LinkedList<ClassData> classes;


    public MyClasses()
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
