package Symbols;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MethodData {

    private String name;
    private String type;
    private boolean overriding;
    private String[][] arguments;
    private Map<String, String> variables;

    public MethodData(String classname, String classtype, String args)
    {
        this.name = classname;
        this.type = classtype;
        this.overriding = false;
        this.variables = new HashMap<>();
        if (args.equals(""))
        {
            this.arguments = null;
            return;
        }

        String[] split_args = args.split(" ");
        this.arguments = new String[split_args.length/2][2];

        int count=0;
        for (int i=1; i<split_args.length; i+=2)
        {
            this.arguments[count][0] = split_args[i-1];
            this.arguments[count][1] = split_args[i];
            count++;
        }
    }

    public String argumentsToString()
    {
        StringBuilder str = new StringBuilder();
        for (String[] argument : arguments) {
            str.append(argument[0]).append(" ");
            str.append(argument[1]).append(" ");
        }
        return str.toString();
    }

    /** adds a variable to method **/
    public void addVariable(String name, String type)
    {
        variables.put(name, type);
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

    public String[][] getArguments()
    {
        return this.arguments;
    }

    public Map<String, String> getVariables()
    {
        return variables;
    }
}
