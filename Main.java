import myVisitors.*;
import syntaxtree.*;

import java.io.*;


public class Main {

    public static void main(String[] args) throws Exception {
        if(args.length < 1)
        {
            System.err.println("Usage: java Main <inputFile>");
            System.exit(-1);
        }

        FileInputStream fis = null;
        for (String arg : args)     // supports many files.
        {
            System.out.print("File "+arg+":");

            fis = new FileInputStream(arg);
            MiniJavaParser parser = new MiniJavaParser(fis);

            Goal root = parser.Goal();

            boolean exception = false;
            Visitor1 visit1;
            Visitor2 visit2 = null;

            try{
                /** 1st visitor stores all values in classes of package Symbols and checks declaration related errors **/
                visit1 = new Visitor1();    // passes filename for error messages
                root.accept(visit1, null);

                /** 2nd visitor checks all the other errors **/
                visit2 = new Visitor2(visit1.getAllClasses());
                root.accept(visit2, null);

            }
            catch(Exception ex)
            {   // after catching an exception the program continues to the next file
                System.err.println(ex.getMessage());
                System.out.println();
                exception = true;
            }
            finally
            {
                // prints offsets in correct programs only
                if (!exception)
                {
                    assert visit2 != null;
                    System.out.println();
                    visit2.getMyClasses().printClassOffsets();
                }
                System.out.println();
            }
        }

        try
        {
            if(fis != null)
                fis.close();
        }
        catch(IOException ex)
        {
            System.err.println(ex.getMessage());
        }
    }
}
