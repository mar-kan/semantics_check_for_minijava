import Symbols.ClassData;

public class Utilities {


    /** printing offsets function **/
    public void printClassOffsets(EvalVisitor eval)
    {
        int var_offset = 0, method_offset = 0;
        for (ClassData aClass : eval.getMyClasses().getClasses())
        {
            var_offset = aClass.printVarOffsets(aClass.getName(), var_offset);
            method_offset = aClass.printMethodOffsets(aClass.getName(), method_offset);
        }
    }


    /** checks if id is a literal value **/
    public boolean isLiteralValue(String id)
    {
        return isLiteralBoolean(id) || isLiteralInteger(id);
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

    /** checks literal value of bool **/
    public boolean isLiteralBoolean(String id)
    {
        return id.equals("true") || id.equals("false");
    }
}