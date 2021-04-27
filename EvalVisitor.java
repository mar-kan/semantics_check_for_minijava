import Symbols.AllClasses;
import Symbols.ClassData;
import Symbols.MethodData;
import Symbols.VariableData;
import syntaxtree.*;
import visitor.GJDepthFirst;

public class EvalVisitor extends GJDepthFirst<String, String> {

    private final ExpressionEvaluator expressionEvaluator;
    private final AllClasses allClasses;
    private final String file_name;

    private boolean parsedOk = true;
    private Utilities utils = new Utilities();


    EvalVisitor(String filename, AllClasses classes)
    {
        this.expressionEvaluator = new ExpressionEvaluator(filename, classes);
        this.allClasses = classes;
        this.file_name = filename;
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

        //String args = n.f11.accept(this, "main");   // arguments

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
        n.f3.accept(this, classname);
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

        String myName = n.f2.accept(this, classname);

        ClassData myClass = allClasses.searchClass(classname);
        if (myClass == null)
        {
            System.err.println(file_name+":"+" error: Class "+classname+" doesn't exist.");
            return null;
        }
        MethodData method = myClass.searchMethod(myName);
        if (method == null)
        {
            System.err.println(file_name+":"+" error: Method "+myName+" doesn't exist in class "+classname+".");
            return null;
        }

        n.f7.accept(this, classname+"."+myName);   // passes scope in VarDeclaration
        n.f8.accept(this, classname+"."+myName);

        String return_expr = n.f10.accept(this, classname+"."+myName);

        parsedOk = expressionEvaluator.evaluateType(return_expr, method.getType(),myClass.getName()+"."+
                method.getName()) && parsedOk; // checks that method has the same ret type that was declared

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
        String expression = n.f2.accept(this, scope);

        // finds type of id
        String type = null;
        if (scope.contains(".")) // in method of class
        {
            String classname, method;
            classname = scope.substring(0, scope.indexOf("."));
            method = scope.substring(scope.indexOf(".")+1, scope.length());

            VariableData var = allClasses.searchClass(classname).searchMethod(method).searchVariable(id);
            if (var == null)
                var = allClasses.searchClass(classname).searchVariable(id);
            if (var == null)
            {
                System.err.println(file_name+":"+" error: Variable "+id+" hasn't been declared in this scope.");
                parsedOk = false;
                return null;
            }
            type = var.getType();
        }
        else if (scope.equals("main"))  // in main
        {
            type = allClasses.getMainClass().searchVariable(id).getType();
        }
        else // in class
        {
            ClassData aClass = allClasses.searchClass(scope);
            type = aClass.searchVariable(id).getType();
        }

        // checks that they have the same type
        parsedOk = expressionEvaluator.compareVariableTypes(id, expression, type, scope) && parsedOk;

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
    public String visit(ArrayAssignmentStatement n, String scope) throws Exception
    {
        String _ret=null;
        String id = n.f0.accept(this, scope);
        parsedOk = expressionEvaluator.evaluateType(id, "int[]", scope) && parsedOk;

        String index = n.f2.accept(this, scope);
        parsedOk = expressionEvaluator.evaluateType(index, "int", scope) && parsedOk;

        String expression = n.f5.accept(this, scope);
        parsedOk = expressionEvaluator.evaluateType(expression, "int", scope) && parsedOk;
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
        String str =  n.f0.accept(this, argu);
        return str;
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

        // checks that both expression results evaluate to booleans
        boolean parsedExpr = expressionEvaluator.evaluateType(expr1, "boolean", scope);
        parsedExpr = expressionEvaluator.evaluateType(expr2, "boolean", scope) && parsedExpr;

        parsedOk = parsedExpr && parsedOk;
        if (parsedExpr)
            return expr1+"&&"+expr2;
        else
            return "-1";
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

        // checks that both expression results evaluate to ints
        boolean parsedExpr = expressionEvaluator.evaluateType(expr1, "int", scope);
        parsedExpr = expressionEvaluator.evaluateType(expr2, "int", scope) && parsedExpr;

        parsedOk = parsedExpr && parsedOk;
        if (parsedExpr)
            return expr1+"<"+expr2;
        else
            return "-1";
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

        // checks that both expression results evaluate to ints
        boolean parsedExpr = expressionEvaluator.evaluateType(expr1, "int", scope);
        parsedExpr = expressionEvaluator.evaluateType(expr2, "int", scope) && parsedExpr;

        parsedOk = parsedExpr && parsedOk;
        if (parsedExpr)
            return expr1+"+"+expr2;
        else
            return "-1";
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

        // checks that both expression results evaluate to ints
        boolean parsedExpr = expressionEvaluator.evaluateType(expr1, "int", scope);
        parsedExpr = expressionEvaluator.evaluateType(expr2, "int", scope) && parsedExpr;

        parsedOk = parsedExpr && parsedOk;
        if (parsedExpr)
            return expr1+"-"+expr2;
        else
            return "-1";
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

        // checks that both expression results evaluate to ints
        boolean parsedExpr = expressionEvaluator.evaluateType(expr1, "int", scope);
        parsedExpr = expressionEvaluator.evaluateType(expr2, "int", scope) && parsedExpr;

        parsedOk = parsedExpr && parsedOk;
        if (parsedExpr)
            return expr1+"*"+expr2;
        else
            return "-1";
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
        n.f1.accept(this, argu);
        res += "[";
        res += n.f2.accept(this, argu);    // prepei na tsekarw oti einai int
        n.f3.accept(this, argu);
        res += "]";

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
        res += ".";
        res += n.f2.accept(this, argu);
        res += "(";
        if (n.f4.present())
            res += n.f4.accept(this, argu);
        res += ")";
        return res;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
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
    public String visit(ArrayAllocationExpression n, String scope) throws Exception
    {
        String res = "new int[";                                // builds string
        String expr = n.f3.accept(this, scope);
        res += expr+"]";

        parsedOk = expressionEvaluator.evaluateType(expr, "int", scope) && parsedOk; // checks that f3 has an integer value

        return res;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String scope) throws Exception
    {
        String id = n.f1.accept(this, scope);
        parsedOk = expressionEvaluator.checkForClass(id) && parsedOk;
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
        return "n.f1.accept(this, argu)";
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

    public AllClasses getMyClasses()
    {
        return allClasses;
    }
}
