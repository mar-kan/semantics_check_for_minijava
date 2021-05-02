import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import Symbols.ClassData;
import syntaxtree.*;


public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }

        FileInputStream fis = null;
        try{
            for (String arg : args)
            {
                fis = new FileInputStream(args[0]);
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();
                System.out.println();

                // 1st visitor stores all values in classes of package Symbols
                Visitor1 visit1 = new Visitor1(arg);    // passes filename for error messages
                root.accept(visit1, null);

                // 2nd visitor evaluates all variables
                Visitor2 visit2 = new Visitor2(arg, visit1.getAllClasses());
                root.accept(visit2, null);

                // prints offsets
                printClassOffsets(visit2);
            }
        }
        catch(ParseException | CompileException | FileNotFoundException ex)
        {
            System.err.println(ex.getMessage());
        }
        finally {
            try{
                if(fis != null) fis.close();
            }
            catch(IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    /** printing offsets function **/
    public static void printClassOffsets(Visitor2 eval)
    {
        int var_offset = 0, method_offset = 0;
        for (ClassData aClass : eval.getMyClasses().getClasses())
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
}
