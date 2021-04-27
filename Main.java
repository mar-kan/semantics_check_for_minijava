import Symbols.ClassData;
import syntaxtree.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }

        Utilities utils = new Utilities();
        FileInputStream fis = null;
        try{
            for (String arg : args)
            {
                fis = new FileInputStream(args[0]);
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();
                System.out.println();

                // 1st visitor stores all values in classes of package Symbols
                StoreVisitor store = new StoreVisitor(arg);    // passes filename for error messages
                root.accept(store, null);

                // 2nd visitor evaluates all variables
                EvalVisitor eval = new EvalVisitor(arg, store.getAllClasses());
                root.accept(eval, null);

                if (!store.isParsedOk() || !eval.isParsedOk())
                {
                    System.err.println("\nProgram failed to parse.");
                    System.exit(-1);
                }

                // prints offsets only if the program parsed successfully
                System.err.println("Program parsed successfully.");
                System.out.println();
                utils.printClassOffsets(eval);
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
}
