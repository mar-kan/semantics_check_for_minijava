package symbols;

import java.util.LinkedList;


public class ClassData {

    private final String name;
    private final ClassData extending;
    private LinkedList<VariableData> fields;
    private LinkedList<MethodData> methods;

    public ClassData(String class_name, ClassData extend)
    {
        this.name = class_name;
        this.extending = extend;
        this.fields = new LinkedList<>();
        this.methods = new LinkedList<>();
    }

    /** adds a field in list fields **/
    public void addField(String var_name, String var_type)
    {
        this.fields.add(new VariableData(var_name, var_type));
    }

    /** adds a method in list methods **/
    public void addMethod(MethodData method)
    {
        this.methods.add(method);
    }

    /** searches list of variables for one named <varname> **/
    public VariableData searchVariable(String varname)
    {
        for (VariableData var : fields)
        {
            if (var.getName().equals(varname))
                return var;
        }

        if (extending != null)  // checks superclass' fields
            return extending.searchVariable(varname);

        return null;
    }

    /** searches list of methods for one named <methodname> **/
    public MethodData searchMethod(String methodname)
    {
        for (MethodData method : methods)
        {
            if (method.getName().equals(methodname))
                return method;
        }

        if (extending != null)  // checks superclass' fields
            return extending.searchMethod(methodname);

        return null;
    }

    /** prints offsets of class' fields **/
    public int printVarOffsets(String classname, int offset)
    {
        for(VariableData var : fields)
        {
            System.out.println(classname + "." + var.getName() + " : " + offset);
            offset = updateOffset(offset, var.getType());
        }
        return offset;
    }

    /** prints offsets of class' methods **/
    public int printMethodOffsets(String classname, int offset)
    {
        for(MethodData method : methods)
        {
            if (method.isOverriding())
                continue;

            System.out.println(classname + "." + method.getName() + " : " + offset);
            offset += 8;
        }
        return offset;
    }

    /** check for object types of inherited classes **/
    public boolean checkInheritance(String classname)
    {
        ClassData ext = extending;
        while (ext != null)
        {
            if (extending.getName().equals(classname))
                return true;

            ext = ext.getExtending();
        }

        return false;
    }

    /** updates offset according to var's type **/
    public int updateOffset(int offset, String type)
    {
        if (type.equals("int"))
            return offset+4;
        else if (type.equals("boolean"))
            return ++offset;
        else
            return offset+8;
    }

    /** setters and getters **/
    public String getName()
    {
        return this.name;
    }

    public ClassData getExtending()
    {
        return extending;
    }

    public LinkedList<MethodData> getMethods()
    {
        return methods;
    }

    public LinkedList<VariableData> getFields()
    {
        return fields;
    }


}