package AST;

public interface ASTVisitor{
    void visit(ProgramNode it);
    void visit(SingleVarDefStmt it);
    void visit(TypeNode it);
    void visit(VarDefStmt it);
    void visit(BlockStmt it);
    void visit(FuncDefNode it);
    void visit(ClassDefNode it);
    void visit(BreakStmt it);
    void visit(ContinueStmt it);
    void visit(EmptyStmt it);
    void visit(ForStmt it);
    void visit(IfStmt it);
    void visit(PureExprStmt it);
    void visit(ReturnStmt it);
    void visit(WhileStmt it);
    void visit(NewExpr it);
    void visit(BoolConstExpr it);
    void visit(IntConstExpr it);
    void visit(NullConstExpr it);
    void visit(StrConstExpr it);
    void visit(ExprListExpr it);
    void visit(BinaryExpr it);
    void visit(FuncCallExpr it);
    void visit(MemberAccessExpr it);
    void visit(SubscriptExpr it);
    void visit(SuffixExpr it);
    void visit(ThisExpr it);
    void visit(UnaryExpr it);
    void visit(VarExpr it);
}