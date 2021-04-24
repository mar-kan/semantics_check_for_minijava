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

        String args = n.f11.accept(this, "main");
        n.f14.accept(this, "main");
        n.f15.accept(this, "main");

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
        parsedOk = evaluator.checkClassName(myClasses, classname, null) && parsedOk;
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
        parsedOk = evaluator.checkClassName(myClasses, classname, extend) && parsedOk;
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
        parsedOk = evaluator.evaluateMethod(newmethod, myClass) && parsedOk;
        myClass.addMethod(newmethod);

        n.f7.accept(this, myClass.getName()+"."+newmethod.getName());   // passes scope in VarDeclaration
        n.f8.accept(this, myClass.getName()+"."+newmethod.getName());

        String return_expr = n.f10.accept(this, myClass.getName()+"."+newmethod.getName());

        parsedOk = evaluator.evaluateType(return_expr, newmethod.getType(),myClass.getName()+"."+
                newmethod.getName(), myClasses);    // checks that method has the same ret type that was declared

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
                method = scope.substring(scope.indexOf(".")+1, scope.length());

                MethodData methodData = myClasses.searchClass(classname).searchMethod(method);
                parsedOk = evaluator.checkVarMethodDuplicates(id, methodData) && parsedOk;    // checks for variable duplicates
                methodData.addVariable(id, type);    // adds var in method of class
            }
            else if (scope.equals("main"))  // in main
            {
                parsedOk = evaluator.checkVarMainDuplicates(id, myClasses.getMainClass()) && parsedOk;    // checks for variable duplicates
                myClasses.getMainClass().addField(id, type, null);  // adds var in main
            }
            else // in class
            {
                ClassData aClass = myClasses.searchClass(scope);
                parsedOk = evaluator.checkFieldDuplicates(id, aClass) && parsedOk;     // checks for duplicate fields
                aClass.addField(id, type, null);       // adds new field in its class
            }
        }
        return null;
    }

    /******** statements ********

     /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public String visit(Statement n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
    }

     /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, String argu) throws Exception
    {
        String id = n.f0.accept(this, argu);
        String expression = n.f2.accept(this, argu);

        //evaluator.compareVariableTypes(id, expression);
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
        String _ret=null;
        String id = n.f0.accept(this, argu);
        String index = n.f2.accept(this, argu);
        String expressino = n.f5.accept(this, argu);

        return _ret;
    }


    /******** expressions ********

     /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
     */
    public String visit(Expression n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    public String visit(AndExpression n, String argu) throws Exception
    {
        String expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu); //prepei na tsekarw oti kai ta 2 einai boolean

        // checks that both expression results evaluate to booleans
        parsedOk = evaluator.evaluateType(expr1, "boolean") && parsedOk;
        parsedOk = evaluator.evaluateType(expr2, "boolean") && parsedOk;

        return Main.calculateResult(expr1, expr2, "&&", "boolean"); // returns result of logic and
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception
    {
        String expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);

        // checks that both expression results evaluate to ints
        parsedOk = evaluator.evaluateType(expr1, "int") && parsedOk;
        parsedOk = evaluator.evaluateType(expr2, "int") && parsedOk;

        return Main.calculateResult(expr1, expr2, "<", "int"); // returns result of comparison
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception
    {
        String expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);

        // checks that both expression results evaluate to ints
        parsedOk = evaluator.evaluateType(expr1, "int") && parsedOk;
        parsedOk = evaluator.evaluateType(expr2, "int") && parsedOk;

        return Main.calculateResult(expr1, expr2, "+", "int"); // returns result of addition
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception
    {
        String expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);

        // checks that both expression results evaluate to ints
        parsedOk = evaluator.evaluateType(expr1, "int") && parsedOk;
        parsedOk = evaluator.evaluateType(expr2, "int") && parsedOk;

        return Main.calculateResult(expr1, expr2, "-", "int"); // returns result of subtraction
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception
    {
        String expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);

        // checks that both expression results evaluate to ints
        parsedOk = evaluator.evaluateType(expr1, "int") && parsedOk;
        parsedOk = evaluator.evaluateType(expr2, "int") && parsedOk;

        return Main.calculateResult(expr1, expr2, "*", "int"); // returns result of multiplication
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception
    {
        String res = "";
        res += n.f0.accept(this, argu);    // prepei na tsekarw oti E
        res += n.f1.accept(this, argu);
        res += n.f2.accept(this, argu);    // prepei na tsekarw oti einai int
        res += n.f3.accept(this, argu);
        return res;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception
    {
        String res = "";
        res += n.f0.accept(this, argu); // prepei na tsekarw oti E k einai array
        res += n.f1.accept(this, argu);
        res += n.f2.accept(this, argu);
        return res;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, String argu) throws Exception
    {
        String res = "";
        res += n.f0.accept(this, argu); // prepei na tsekarw polla
        res += n.f1.accept(this, argu);
        res += n.f2.accept(this, argu);
        res += n.f3.accept(this, argu);
        res += n.f4.accept(this, argu);
        res += n.f5.accept(this, argu);
        return res;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception
    {
        return n.f0.accept(this, argu) + n.f1.accept(this, argu);
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, String argu) throws Exception
    {
        return n.f0.present() ? n.f0.accept(this, argu) : null;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String argu) throws Exception
    {
        n.f0.accept(this, argu);
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) throws Exception
    {
        n.f0.accept(this, argu);
        return n.f0.tokenImage;
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String argu) throws Exception
    {
        n.f0.accept(this, argu);
        return n.f0.tokenImage;
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String argu) throws Exception
    {
        n.f0.accept(this, argu);
        return n.f0.tokenImage;
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String argu) throws Exception
    {
        n.f0.accept(this, argu);
        return "this";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, String argu) throws Exception
    {
        String res = "";                                // builds string
        res += n.f0.accept(this, argu);
        res += n.f1.accept(this, argu);
        res += n.f2.accept(this, argu);
        String expr = n.f3.accept(this, argu);
        res += expr;

        parsedOk = evaluator.evaluateType(expr, "int") && parsedOk; // checks that f3 has an integer value

        return res;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String argu) throws Exception
    {
        String id = n.f1.accept(this, argu);
        parsedOk = evaluator.checkForClass(id, myClasses) && parsedOk;
        return "new"+id+"()";
    }

    /**
     * f0 -> "!"
     * f1 -> PrimaryExpression()
     */
    public String visit(NotExpression n, String argu) throws Exception
    {
        return "!"+n.f1.accept(this, argu);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception
    {
        return "("+n.f1.accept(this, argu)+")";
    }

    /******** data types ********

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
