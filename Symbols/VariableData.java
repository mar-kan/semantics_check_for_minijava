package Symbols;


public class VariableData {

    private String name;
    private String type;


    public VariableData(String varname, String vartype)
    {
        this.name = varname;
        this.type = vartype;
    }


    /** setters / getters **/

    public String getType()
    {
        return this.type;
    }

    public String getName()
    {
        return this.name;
    }
}
