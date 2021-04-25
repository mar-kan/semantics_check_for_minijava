package Symbols;

import java.util.LinkedList;


public class MethodData {

    private String name;
    private String type;
    private String arguments_to_string;
    private boolean overriding;
    private LinkedList<VariableData> arguments;
    private LinkedList<VariableData> variables;

    public MethodData(String classname, String classtype, String args)
    {
        this.name = classname;
        this.type = classtype;
        this.arguments_to_string = args;
        this.overriding = false;
        this.arguments = new LinkedList<>();
        this.variables = new LinkedList<>();

        if (args.equals(""))
        {
            this.arguments = null;
            return;
        }

        String[] split_args = args.split(" ");
        for (int i=1; i<split_args.length; i+=2)
        {
            this.arguments.add(new VariableData(split_args[i-1], split_args[i], null));
        }
    }

    /** searches list of variables for one named <varname> **/
    public VariableData searchVariable(String varname)
    {
        // searches in arguments
        if (arguments != null)
        {
            for (VariableData var : this.arguments)
            {
                if (var.getName().equals(varname))
                    return var;
            }
        }

        // searches in the rest variables
        for (VariableData var : this.variables)
        {
            if (var.getName().equals(varname))
                return var;
        }
        return null;
    }

    /** adds a variable to method **/
    public void addVariable(String name, String type, String value)
    {
        variables.add(new VariableData(name, type, value));
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

    public boolean isOverriding()
    {
        return overriding;
    }

    public void setOverriding(boolean overriding)
    {
        this.overriding = overriding;
    }

    public LinkedList<VariableData> getArguments()
    {
        return arguments;
    }

    public LinkedList<VariableData> getVariables()
    {
        return variables;
    }

    public String getArguments_to_string()
    {
        return arguments_to_string;
    }
}
