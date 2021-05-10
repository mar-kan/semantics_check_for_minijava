package symbols;

import java.util.LinkedList;

public class MethodData {

    private final String name;
    private final String type;
    private final String arguments_to_string;

    private boolean overriding = false;
    private LinkedList<VariableData> arguments = new LinkedList<>();
    private LinkedList<VariableData> variables = new LinkedList<>();

    public MethodData(String methodname, String methodtype, String args, String classname) throws Exception
    {
        this.name = methodname;
        this.type = methodtype;
        this.arguments_to_string = args;

        if (args.equals(""))
        {
            this.arguments = null;
            return;
        }

        String[] split_args = args.split(" ");
        for (int i=1; i<split_args.length; i+=2)
        {
            if (split_args[i].contains(","))    // removes commas
                split_args[i] = split_args[i].substring(0, split_args[i].indexOf(","));
            if (searchArguments(split_args[i]) == null)
                this.arguments.add(new VariableData(split_args[i], split_args[i-1]));
            else
                throw new Exception(classname+"."+this.name+": error: Argument "+split_args[i]+" has already been declared.");
        }
    }

    /** searches lists of variables and arguments for one named <varname> **/
    public VariableData searchVariable(String varname)
    {
        // searches in the rest variables
        for (VariableData var : this.variables)
        {
            if (var.getName().equals(varname))
                return var;
        }

        return searchArguments(varname);
    }

    /** searches list of variables for one named <varname> **/
    public VariableData searchArguments(String varname)
    {
        // searches for variable name in arguments
        if (arguments != null)
        {
            for (VariableData var : this.arguments)
            {
                if (var.getName().equals(varname))
                    return var;
            }
        }
        return null;
    }

    /** adds a variable to method **/
    public void addVariable(String name, String type)
    {
        variables.add(new VariableData(name, type));
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
