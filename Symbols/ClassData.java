package Symbols;

import java.util.Iterator;
import java.util.LinkedList;


public class ClassData {

    private String name;
    private ClassData extending;
    private LinkedList<VariableData> fields;
    private LinkedList<MethodData> methods;

    public ClassData(String class_name, ClassData extend)
    {
        this.name = class_name;
        this.extending = extend;
        this.fields = new LinkedList<VariableData>();
        this.methods = new LinkedList<MethodData>();
    }

    /** adds a field in list fields **/
    public void addField(String var_name, String var_type)
    {
        this.fields.add(new VariableData(var_name, var_type));
    }

    /** adds a method in list methods **/
    public MethodData addMethod(String method_name, String method_type, String args)
    {
        MethodData newmethod = new MethodData(method_name, method_type, args);
        this.methods.add(newmethod);
        return newmethod;
    }

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