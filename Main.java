import Symbols.ClassData;
import syntaxtree.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.err.println("Usage: java Main <inputFile>");
            exit(1);
        }

        FileInputStream fis = null;
        try{
            for (String arg : args)
            {
                fis = new FileInputStream(args[0]);
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

                System.out.println();
                MyVisitor eval = new MyVisitor(arg);
                root.accept(eval, null);

                if (!eval.isParsedOk())
                {
                    System.err.println("\nProgram failed to parse.");
                    exit(-1);
                }

                System.err.println("Program parsed successfully.");
                System.out.println();
                printClassOffsets(eval);
            }
        }
        catch(ParseException ex){
            System.out.println(ex.getMessage());
        }
        catch(FileNotFoundException ex){
            System.err.println(ex.getMessage());
        }
        finally{
            try{
                if(fis != null) fis.close();
            }
            catch(IOException ex){
                System.err.println(ex.getMessage());
            }
        }
    }

    /** printing offsets function **/
    public static void printClassOffsets(MyVisitor eval)
    {
        int var_offset = 0, method_offset = 0;
        for (ClassData aClass : eval.getMyClasses().getClasses())
        {
            var_offset = aClass.printVarOffsets(aClass.getName(), var_offset);
            method_offset = aClass.printMethodOffsets(aClass.getName(), method_offset);
        }
    }
}
