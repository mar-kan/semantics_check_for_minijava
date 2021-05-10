package myVisitors;


import myVisitors.evaluators.ExpressionEvaluator;
import visitor.GJDepthFirst;
import syntaxtree.*;
import symbols.*;


public class Visitor2 extends GJDepthFirst<String, String> {

    private final ExpressionEvaluator expressionEvaluator;
    private final AllClasses allClasses;


    public Visitor2(AllClasses classes)
    {
        this.expressionEvaluator = new ExpressionEvaluator(classes);
        this.allClasses = classes;
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
        n.f0.accept(this, argu);
        String classname = n.f1.accept(this, "main");

        if (n.f14.present())
            n.f14.accept(this, "main");

        if (n.f15.present())
            n.f15.accept(this, "main");

        n.f16.accept(this, argu);
        n.f17.accept(this, argu);

        super.visit(n, "main");
        return classname;
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    @Override
    public String visit(TypeDeclaration n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
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

        n.f3.accept(this, classname);
        n.f4.accept(this, classname);

        super.visit(n, classname);
        return classname;
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

        n.f3.accept(this, classname);
        n.f5.accept(this, classname);
        n.f6.accept(this, classname);

        super.visit(n, classname);
        return classname;
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
        n.f1.accept(this, classname);
        String myName = n.f2.accept(this, classname);

        ClassData myClass = allClasses.searchClass(classname);
        if (myClass == null)
            throw new Exception(classname+"."+myName+": error: Class "+classname+" doesn't exist.");

        MethodData method = myClass.searchMethod(myName);
        if (method == null)
            throw new Exception(classname+"."+myName+": error: Method "+myName+" hasn't been declared in this scope.");

        n.f4.accept(this, classname+"."+myName);
        n.f7.accept(this, classname+"."+myName);
        n.f8.accept(this, classname+"."+myName);

        String return_expr = n.f10.accept(this, classname+"."+myName);

        // checks that method has the same ret type that was declared
        expressionEvaluator.evaluateType(return_expr, method.getType(),classname+"."+myName);

        super.visit(n, classname+"."+myName);
        return method.getType();
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, String scope) throws Exception
    {
        String type = n.f0.accept(this, scope);
        n.f1.accept(this, scope);

        return type;
    }


    /******** arguments ********/
    // they are combined in a string and returned as in Visitor1

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
    @Override
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
        for (Node node: n.f0.nodes)
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


    /******** statements ********/
    // every statement returns its type

     /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    @Override
    public String visit(Statement n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    @Override
    public String visit(Block n, String scope) throws Exception
    {
        for (Node node: n.f1.nodes)
        {
            node.accept(this, scope);
        }
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public String visit(AssignmentStatement n, String scope) throws Exception
    {
        String id = n.f0.accept(this, scope);
        String expr = n.f2.accept(this, scope);

        // checks that id exists
        VariableData var = allClasses.findVariable(id, scope);
        if (var == null)
            throw new Exception(scope+": error: Variable "+id+" hasn't been declared in this scope.");

        // checks that id and expr have the same type
        expressionEvaluator.compareVariableTypes(var.getType(), expr, var.getType(), scope);

        return var.getType();
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
    @Override
    public String visit(ArrayAssignmentStatement n, String scope) throws Exception
    {
        String id = n.f0.accept(this, scope);
        expressionEvaluator.evaluateType(id, "int[]", scope);   // checks that id exists and is an array

        String index = n.f2.accept(this, scope);
        expressionEvaluator.evaluateType(index, "int", scope);  // checks that index evaluates to int

        String expr = n.f5.accept(this, scope);
        expressionEvaluator.evaluateType(expr, "int", scope); // checks that expr evaluates to int

        return "int";
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    @Override
    public String visit(IfStatement n, String scope) throws Exception
    {
        String expr = n.f2.accept(this, scope);
        expressionEvaluator.evaluateType(expr, "boolean", scope);   // checks that expr is a logical expression

        n.f4.accept(this, scope);
        n.f6.accept(this, scope);

        return "void";
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    @Override
    public String visit(WhileStatement n, String scope) throws Exception
    {
        String expr = n.f2.accept(this, scope);
        expressionEvaluator.evaluateType(expr, "boolean", scope);   // checks that expr is a logical expression

        n.f4.accept(this, scope);
        return "void";
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    @Override
    public String visit(PrintStatement n, String scope) throws Exception
    {
        String expr = n.f2.accept(this, scope);

        // checks that expr evaluates to int
        expressionEvaluator.evaluateType(expr, "int", scope);

        return "void";
    }


    /******** expressions ********/
     // every expression returns its type

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
    @Override
    public String visit(Expression n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(AndExpression n, String scope) throws Exception
    {

        String expr1 = n.f0.accept(this, scope);
        String expr2 = n.f2.accept(this, scope);

        // checks that both expression results evaluate to booleans
        expressionEvaluator.evaluateType(expr1, "boolean", scope);
        expressionEvaluator.evaluateType(expr2, "boolean", scope);

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(CompareExpression n, String scope) throws Exception
    {
        String expr1 = n.f0.accept(this, scope);
        String expr2 = n.f2.accept(this, scope);

        // checks that both expression results evaluate to ints
        expressionEvaluator.evaluateType(expr1, "int", scope);
        expressionEvaluator.evaluateType(expr2, "int", scope);

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(PlusExpression n, String scope) throws Exception
    {
        String expr1 = n.f0.accept(this, scope);
        String expr2 = n.f2.accept(this, scope);

        // checks that both expression results evaluate to ints
        expressionEvaluator.evaluateType(expr1, "int", scope);
        expressionEvaluator.evaluateType(expr2, "int", scope);

        return "int";

    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(MinusExpression n, String scope) throws Exception
    {
        String expr1 = n.f0.accept(this, scope);
        String expr2 = n.f2.accept(this, scope);

        // checks that both expression results evaluate to ints
        expressionEvaluator.evaluateType(expr1, "int", scope);
        expressionEvaluator.evaluateType(expr2, "int", scope);

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(TimesExpression n, String scope) throws Exception
    {
        String expr1 = n.f0.accept(this, scope);
        String expr2 = n.f2.accept(this, scope);

        // checks that both expression results evaluate to ints
        expressionEvaluator.evaluateType(expr1, "int", scope);
        expressionEvaluator.evaluateType(expr2, "int", scope);

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public String visit(ArrayLookup n, String scope) throws Exception
    {
        String arrayName = n.f0.accept(this, scope);
        VariableData array = allClasses.findVariable(arrayName, scope);

        if (array == null)  // checks that array exists
            throw new Exception(scope+": error: Array "+arrayName+" hasn't been declared in this scope.");
        else if (!array.getType().equals("int[]"))  // checks that it is an array
            throw new Exception(scope+": error: Variable "+arrayName+" should be of type int[].");

        String index = n.f2.accept(this, scope);    // checks that index evaluates to int
        expressionEvaluator.evaluateType(index, "int", scope);

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public String visit(ArrayLength n, String scope) throws Exception
    {
        String arrayName = n.f0.accept(this, scope);
        if (arrayName.equals("int[]"))  // covers methods returning arrays
            return "int[]";

        VariableData array = allClasses.findVariable(arrayName, scope);

        if (array == null)  // checks that array exists
            throw new Exception(scope+": error: Array "+arrayName+" hasn't been declared in this scope.");
        else if (!array.getType().equals("int[]"))  // checks that it is an array
            throw new Exception(scope+": error: Variable "+arrayName+" should be of type int[].");

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public String visit(MessageSend n, String scope) throws Exception
    {
        ClassData myClass = null;
        VariableData object = null;

        // f0 can be object or classname
        String objectname = n.f0.accept(this, scope);

        // checks if it's a class name
        myClass = allClasses.searchClass(objectname);
        if (myClass == null)
        {
            // checks if it's an object
            object = allClasses.findVariable(objectname, scope);
            if (object == null)
                throw new Exception(scope+": error: Variable "+objectname+" hasn't been declared in this scope.");
        }

        // checking that object's class exists
        if (myClass == null)
            myClass = allClasses.searchClass(object.getType());
        if (myClass == null)
            throw new Exception(scope+": error: Class "+object.getType()+" doesn't exist.");

        // f2 can be a method of <myClass>
        String methodname = n.f2.accept(this, scope);

        // checks that method exists
        MethodData myMethod = myClass.searchMethod(methodname);
        if (myMethod == null)
            throw new Exception(scope+": error: Method "+methodname+" hasn't been declared in this scope.");

        // f4 can be any num of arguments or ""
        String method_arguments;
        if (n.f4.present())
            method_arguments = n.f4.accept(this, scope);
        else
            method_arguments = "";

        // checks argument number and types
        expressionEvaluator.compareMethodArgs(myMethod.getArguments(), method_arguments, scope, methodname);

        return myMethod.getType();
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    @Override
    public String visit(ExpressionList n, String argu) throws Exception
    {
        return n.f0.accept(this, argu) + n.f1.accept(this, argu);
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    @Override
    public String visit(ExpressionTail n, String argu) throws Exception
    {
        StringBuilder ret = new StringBuilder();
        for (Node node: n.f0.nodes)
        {
            ret.append(", ").append(node.accept(this, argu));
        }

        return ret.toString();
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public String visit(ExpressionTerm n, String argu) throws Exception
    {
        n.f0.accept(this, argu);
        return n.f1.accept(this, argu);
    }


    /******** primary expressions ********/
    // every primary expression returns its type

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
    @Override
    public String visit(PrimaryExpression n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    @Override
    public String visit(IntegerLiteral n, String argu) throws Exception
    {
        n.f0.accept(this, argu);
        return "int";
    }

    /**
     * f0 -> "true"
     */
    @Override
    public String visit(TrueLiteral n, String argu) throws Exception
    {
        n.f0.accept(this, argu);
        return "boolean";
    }

    /**
     * f0 -> "false"
     */
    @Override
    public String visit(FalseLiteral n, String argu) throws Exception
    {
        n.f0.accept(this, argu);
        return "boolean";
    }

    /**
     * f0 -> "this"
     */
    @Override
    public String visit(ThisExpression n, String scope) throws Exception
    {
        n.f0.accept(this, scope);

        // returns the classname in which this refers to
        if (scope.contains("."))
            return scope.substring(0, scope.indexOf("."));
        else if (scope.equals("main"))
            throw new Exception(scope+" error: \"this\" cannot be used in this scope.\n It has to refer to a class");
        else
            return scope;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public String visit(ArrayAllocationExpression n, String scope) throws Exception
    {
        String expr = n.f3.accept(this, scope);
        expressionEvaluator.evaluateType(expr, "int", scope); // checks that expr has an integer value

        return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public String visit(AllocationExpression n, String scope) throws Exception
    {
        String id = n.f1.accept(this, scope);

        if (allClasses.searchClass(id) == null)
            throw new Exception(scope+": error: Class "+id+" doesn't exist.");

        return id;
    }

    /**
     * f0 -> "!"
     * f1 -> PrimaryExpression()
     */
    @Override
    public String visit(NotExpression n, String scope) throws Exception
    {
        String expr = n.f1.accept(this, scope);

        // checks that expr is boolean
        expressionEvaluator.evaluateType(expr, "boolean", scope);

        return "boolean";
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    @Override
    public String visit(BracketExpression n, String argu) throws Exception
    {
        return n.f1.accept(this, argu);
    }

    /******** data types ********/
    // the data types are returned

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
    public AllClasses getMyClasses()
    {
        return allClasses;
    }
}
