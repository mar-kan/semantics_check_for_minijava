import Symbols.ClassData;
import Symbols.MethodData;
import Symbols.AllClasses;
import Symbols.VariableData;
import visitor.*;
import syntaxtree.*;


class StoreVisitor extends GJDepthFirst<String, String> {
    private DeclarationEvaluator declarationEvaluator;

    private AllClasses allClasses = new AllClasses();
    private boolean parsedOk = true;
    private Utilities utils = new Utilities();

    StoreVisitor(String filename)
    {
        this.declarationEvaluator = new DeclarationEvaluator(filename);
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
    public String visit(MainClass n, String argu) throws Exception
    {
        String classname = n.f1.accept(this, "main");
        allClasses.setMain_class_name(classname);

        if (n.f14.present())
            n.f14.accept(this, "main");

        if (n.f15.present())
            n.f15.accept(this, "main");

        super.visit(n, null);
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
    public String visit(ClassDeclaration n, String argu) throws Exception
    {
        String classname = n.f1.accept(this, null);

        // checks class for errors
        parsedOk = declarationEvaluator.checkClassName(classname, null, allClasses) && parsedOk;
        allClasses.addClass(classname, null);    // adds new class in list

        n.f3.accept(this, classname);
        n.f4.accept(this, classname);

        super.visit(n, null);
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
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception
    {
        String classname = n.f1.accept(this, null);
        String extend = n.f3.accept(this, classname);

        // checks class for errors
        parsedOk = declarationEvaluator.checkClassName(classname, extend, allClasses) && parsedOk;
        allClasses.addClass(classname, allClasses.searchClass(extend)); // adds new class in list

        n.f5.accept(this, classname);
        n.f6.accept(this, classname);

        super.visit(n, null);
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
        if (classname == null)
            return null;

        String argumentList = n.f4.present() ? n.f4.accept(this, classname) : "";
        String myType = n.f1.accept(this, classname);
        String myName = n.f2.accept(this, classname);

        // finds its class in MyClasses' list and adds the method there
        ClassData myClass = allClasses.searchClass(classname);
        if (myClass == null)
        {
            System.out.println("Something went wrong with the classes");
            return null;
        }

        // checks method for all possible errors
        MethodData newmethod = new MethodData(myName, myType, argumentList);
        parsedOk = declarationEvaluator.evaluateMethod(newmethod, myClass) && parsedOk;
        myClass.addMethod(newmethod);

        n.f7.accept(this, classname+"."+myName);   // passes scope in VarDeclaration
        n.f8.accept(this, classname+"."+myName);

        return null;
    }


    /******** arguments ********

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, String scope) throws Exception
    {
        String ret = n.f0.accept(this, scope);

        if (n.f1 != null)
            ret += n.f1.accept(this, scope);

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, String scope) throws Exception
    {
        return n.f1.accept(this, scope);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, String scope) throws Exception
    {
        StringBuilder ret = new StringBuilder();
        for ( Node node: n.f0.nodes)
        {
            ret.append(", ").append(node.accept(this, scope));
        }

        return ret.toString();
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, String scope) throws Exception
    {
        String type = n.f0.accept(this, scope);
        String name = n.f1.accept(this, scope);
        return type+" "+name;
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
        if (scope == null)
            return null;

        String type = n.f0.accept(this, scope);
        String id = n.f1.accept(this, scope);

        if (scope.contains(".")) // in method of class
        {
            String classname, method;
            classname = scope.substring(0, scope.indexOf("."));
            method = scope.substring(scope.indexOf(".")+1, scope.length());

            MethodData methodData = allClasses.searchClass(classname).searchMethod(method);
            parsedOk = declarationEvaluator.checkVarMethodDuplicates(id, methodData) && parsedOk;    // checks for variable duplicates
            methodData.addVariable(id, type);    // adds var in method of class
        }
        else if (scope.equals("main"))  // in main
        {
            parsedOk = declarationEvaluator.checkVarMainDuplicates(id, allClasses.getMainClass()) && parsedOk;    // checks for variable duplicates
            allClasses.getMainClass().addField(id, type);  // adds var in main
        }
        else // in class
        {
            ClassData aClass = allClasses.searchClass(scope);
            parsedOk = declarationEvaluator.checkFieldDuplicates(id, aClass) && parsedOk;     // checks for duplicate fields
            aClass.addField(id, type);       // adds new field in its class
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
    public String visit(AssignmentStatement n, String scope) throws Exception
    {
        if (scope == null)
            return null;

        String id = n.f0.accept(this, scope);
        String expr_value = n.f2.accept(this, scope);

        // find variable
        VariableData var = null;
        if (scope.contains(".")) // in method of class
        {
            String classname, method;
            classname = scope.substring(0, scope.indexOf("."));
            method = scope.substring(scope.indexOf(".")+1, scope.length());

            var = allClasses.searchClass(classname).searchMethod(method).searchVariable(id); // checks method's variables
            if (var == null)    // checks class' fields
                var = allClasses.searchClass(classname).searchVariable(id);
        }
        else if (scope.equals("main"))  // in main
            var = allClasses.getMainClass().searchVariable(id);
        else // in class
            var = allClasses.searchClass(scope).searchVariable(id);

        return null;
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
    public String visit(AndExpression n, String scope) throws Exception
    {
        String expr1 = n.f0.accept(this, scope);
        n.f1.accept(this, scope);
        String expr2 = n.f2.accept(this, scope);

        return expr1+"&&"+expr2;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String scope) throws Exception
    {
        String expr1 = n.f0.accept(this, scope);
        n.f1.accept(this, scope);
        String expr2 = n.f2.accept(this, scope);

        return expr1+"<"+expr2;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String scope) throws Exception
    {
        if (scope == null)
            return null;

        String expr1 = n.f0.accept(this, scope);
        n.f1.accept(this, scope);
        String expr2 = n.f2.accept(this, scope);

        return expr1+"+"+expr2;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String scope) throws Exception
    {
        String expr1 = n.f0.accept(this, scope);
        n.f1.accept(this, scope);
        String expr2 = n.f2.accept(this, scope);

        return expr1+"-"+expr2;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String scope) throws Exception
    {
        String expr1 = n.f0.accept(this, scope);
        n.f1.accept(this, scope);
        String expr2 = n.f2.accept(this, scope);

        return expr1+"*"+expr2;
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
        return n.f1.accept(this, argu);
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


    /** getters **/
    public AllClasses getAllClasses()
    {
        return allClasses;
    }

    public boolean isParsedOk()
    {
        return parsedOk;
    }
}
