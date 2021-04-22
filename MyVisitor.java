import Symbols.ClassData;
import Symbols.MethodData;
import Symbols.MyClasses;
import visitor.*;
import syntaxtree.*;


class MyVisitor extends GJDepthFirst<String, String> {
    private Evaluator evaluator;
    private MyClasses myClasses;
    private boolean parsedOk;

    MyVisitor(String filename)
    {
        evaluator = new Evaluator(filename);
        myClasses = new MyClasses();
        parsedOk = true;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        myClasses.setMain_class_name(classname);

        n.f14.accept(this, "main");
        super.visit(n, argu);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);

        // checks class for errors
        parsedOk = parsedOk && evaluator.checkClassName(myClasses, classname, null);
        myClasses.addClass(classname, null);    // adds new class in list

        n.f3.accept(this, classname);
        n.f4.accept(this, classname);

        super.visit(n, argu);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        String extend = n.f3.accept(this, null);

        // checks class for errors
        parsedOk = parsedOk && evaluator.checkClassName(myClasses, classname, extend);
        myClasses.addClass(classname, myClasses.searchClass(extend)); // adds new class in list

        n.f5.accept(this, classname);
        n.f6.accept(this, classname);

        super.visit(n, argu);
        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, String classname) throws Exception
    {
        if (classname==null)
            return null;

        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";
        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);

        // finds its class in MyClasses' list and adds the method there
        ClassData myClass = myClasses.searchClass(classname);
        if (myClass == null)
        {
            System.out.println("Something went wrong with the classes");
            return null;
        }
        MethodData newmethod = new MethodData(myName, myType, argumentList);
        // checks method for all possible errors
        parsedOk = parsedOk && evaluator.evaluateMethod(newmethod, myClass);
        myClass.addMethod(newmethod);
        // checks if method is overriding another and for relevant errors
        //parsedOk = parsedOk && evaluator.checkMethodOverriding(newmethod, myClass);

        n.f7.accept(this, myClass.getName()+"."+newmethod.getName());

        return null;
    }


    /******** arguments ********

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, String argu) throws Exception
    {
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }


    /******** variables ********
    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, String scope) throws Exception
    {
        String type = n.f0.accept(this, null);
        String id = n.f1.accept(this, null);
        n.f2.accept(this, null);

        if (scope != null)
        {
            if (scope.contains(".")) // in method of class
            {
                String classname, method;
                classname = scope.substring(0, scope.indexOf("."));
                method = scope.substring(scope.indexOf("."), scope.length()-1);

                myClasses.searchClass(classname).searchMethod(method).addVariable(id, type);
            }
            else if (scope.equals("main"))
                myClasses.getMainClass().addField(id, type, null);
            else // in class
            {
                myClasses.searchClass(scope).addField(id, type, null);
            }
        }
        else
        {
            System.out.println("WHYYY?");
        }
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    /*public String visit(AssignmentStatement n, Void argu) throws Exception
    {
        String id = n.f0.accept(this, argu);
        String expression = n.f2.accept(this, argu);

    }*/


    /****** data types ******

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    @Override
    public String visit(Type n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(ArrayType n, String argu)
    {
        return "int[]";
    }

    @Override
    public String visit(BooleanType n, String argu)
    {
        return "boolean";
    }

    @Override
    public String visit(IntegerType n, String argu)
    {
        return "int";
    }


    /** ids **/
    @Override
    public String visit(Identifier n, String argu)
    {
        return n.f0.toString();
    }


    /****** setters / getters ******/

    public boolean isParsedOk()
    {
        return parsedOk;
    }

    public MyClasses getMyClasses()
    {
        return myClasses;
    }
}
