package Symbols;


public class VariableData {

    private String name;
    private String type;
    private String value;


    public VariableData(String varname, String vartype, String varvalue)
    {
        this.name = varname;
        this.type = vartype;
        this.value = varvalue;
    }


    /** setters / getters **/

    public void changeValue(String var1)
    {
        this.value = var1;
    }

    public String getType()
    {
        return this.type;
    }

    public String getName()
    {
        return this.name;
    }
}
