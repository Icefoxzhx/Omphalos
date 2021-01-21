package FrontEnd;

import AST.*;
import Parser.MxBaseVisitor;
import Parser.MxParser;
import Util.error.internalError;
import Util.error.syntaxError;
import Util.error.semanticError;
import Util.position;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.ArrayList;

public class ASTBuilder extends MxBaseVisitor<ASTNode>{
    @Override
    public ASTNode visitProgram(MxParser.ProgramContext ctx){
        ProgramNode res=new ProgramNode(new position(ctx));
        if(!ctx.programfragment().isEmpty()){
            for(ParserRuleContext x : ctx.programfragment()){
                ASTNode tmp=visit(x);
                if(tmp instanceof VarDefStmt){
                    res.body.addAll( ((VarDefStmt)tmp).varList );
                }else{
                    res.body.add(tmp);
                }
            }
        }
        return res;
    }

    @Override
    public ASTNode visitProgramfragment(MxParser.ProgramfragmentContext ctx) {
        if(ctx.funcDef()!=null) return visit(ctx.funcDef());
        else if(ctx.varDef()!=null) return visit(ctx.varDef());
        else return visit(ctx.classDef());
    }

    @Override
    public ASTNode visitClassDef(MxParser.ClassDefContext ctx) {
        ClassDefNode res=new ClassDefNode(new position(ctx),ctx.Identifier().getText());
        if(ctx.varDef()!=null){
            for(ParserRuleContext x : ctx.varDef()){
                res.varList.addAll(((VarDefStmt)visit(x)).varList);
            }
        }
        if(ctx.funcDef()!=null){
            for(ParserRuleContext x : ctx.funcDef()){
                res.funcList.add((FuncDefNode) visit(x));
            }
        }
        if(ctx.constructFuncDef()!=null){
            for(ParserRuleContext x : ctx.constructFuncDef()){
                res.constructorList.add((FuncDefNode) visit(x));
            }
        }
        return res;
    }

    @Override
    public ASTNode visitFuncDef(MxParser.FuncDefContext ctx) {
        FuncDefNode res=new FuncDefNode(new position(ctx),ctx.Identifier().getText(),(TypeNode) visit(ctx.returnType()),(BlockStmt) visit(ctx.suite()));
        if(ctx.paramList()!=null){
            for(ParserRuleContext x : ctx.paramList().param()){
                res.paramList.add((SingleVarDefStmt) visit(x));
            }
        }
        return res;
    }

    @Override
    public ASTNode visitVarDef(MxParser.VarDefContext ctx) {
        VarDefStmt res=new VarDefStmt(new position(ctx));
        TypeNode type=(TypeNode) visit(ctx.type());
        for(ParserRuleContext x : ctx.singleVarDef()){
            SingleVarDefStmt tmp=(SingleVarDefStmt) visit(x);
            tmp.type=type;
            res.varList.add(tmp);
        }
        return res;
    }

    @Override
    public ASTNode visitConstructFuncDef(MxParser.ConstructFuncDefContext ctx) {
        return new FuncDefNode(new position(ctx),ctx.Identifier().getText(),null,(BlockStmt) visit(ctx.suite()));
    }

    @Override
    public ASTNode visitSingleVarDef(MxParser.SingleVarDefContext ctx) {
        return new SingleVarDefStmt(new position(ctx),ctx.Identifier().getText(),ctx.expression()==null?null:(ExprNode) visit(ctx.expression()));
    }

    @Override
    public ASTNode visitParamList(MxParser.ParamListContext ctx) { return visitChildren(ctx); }

    @Override
    public ASTNode visitParam(MxParser.ParamContext ctx) {
        SingleVarDefStmt res=new SingleVarDefStmt(new position(ctx),ctx.Identifier().getText(),null);
        res.type=(TypeNode) visit(ctx.type());
        return res;
    }

    @Override
    public ASTNode visitBasicType(MxParser.BasicTypeContext ctx) {
        return new TypeNode(new position(ctx), ctx.getText(), 0);
    }

    @Override
    public ASTNode visitType(MxParser.TypeContext ctx) {
        return new TypeNode(new position(ctx),ctx.basicType().getText(),(ctx.getChildCount()-1)/2);
    }

    @Override
    public ASTNode visitReturnType(MxParser.ReturnTypeContext ctx) {
        if(ctx.type()!=null) return visit(ctx.type());
        else return new TypeNode(new position(ctx),ctx.Void().getText(),0);
    }

    @Override
    public ASTNode visitSuite(MxParser.SuiteContext ctx) {
        BlockStmt res=new BlockStmt(new position(ctx));
        if(ctx.statement()!=null){
            for(ParserRuleContext x : ctx.statement()){
                res.stmtList.add((StmtNode) visit(x));
            }
        }
        return res;
    }

    @Override
    public ASTNode visitBlock(MxParser.BlockContext ctx) {
        return visit(ctx.suite());
    }

    @Override
    public ASTNode visitVarDefStmt(MxParser.VarDefStmtContext ctx) {
        return visit(ctx.varDef());
    }

    @Override
    public ASTNode visitIfStmt(MxParser.IfStmtContext ctx) {
        return new IfStmt(new position(ctx),(ExprNode) visit(ctx.expression()),(StmtNode) visit(ctx.trueStmt),ctx.falseStmt==null?null:(StmtNode) visit(ctx.falseStmt));
    }

    @Override
    public ASTNode visitForStmt(MxParser.ForStmtContext ctx) {
        return new ForStmt(new position(ctx),ctx.init==null?null:(ExprNode) visit(ctx.init),ctx.cond==null?null:(ExprNode) visit(ctx.cond),ctx.incr==null?null:(ExprNode) visit(ctx.incr),(StmtNode) visit(ctx.statement()));
    }

    @Override
    public ASTNode visitWhileStmt(MxParser.WhileStmtContext ctx) {
        return new WhileStmt(new position(ctx),(ExprNode) visit(ctx.expression()),(StmtNode) visit(ctx.statement()));
    }

    @Override
    public ASTNode visitReturnStmt(MxParser.ReturnStmtContext ctx) {
        return new ReturnStmt(new position(ctx),ctx.expression()==null?null:(ExprNode) visit(ctx.expression()));
    }

    @Override
    public ASTNode visitBreakStmt(MxParser.BreakStmtContext ctx) {
        return new BreakStmt(new position(ctx));
    }

    @Override
    public ASTNode visitContinueStmt(MxParser.ContinueStmtContext ctx) {
        return new ContinueStmt(new position(ctx));
    }

    @Override
    public ASTNode visitPureExprStmt(MxParser.PureExprStmtContext ctx) {
        return new PureExprStmt(new position(ctx),(ExprNode) visit(ctx.expression()));
    }

    @Override
    public ASTNode visitEmptyStmt(MxParser.EmptyStmtContext ctx) {
        return new EmptyStmt(new position(ctx));
    }

    @Override
    public ASTNode visitExpressionList(MxParser.ExpressionListContext ctx) {
        ExprListExpr res=new ExprListExpr(new position(ctx));
        for(ParserRuleContext x : ctx.expression()){
            res.exprList.add((ExprNode) visit(x));
        }
        return res;
    }

    @Override
    public ASTNode visitNew(MxParser.NewContext ctx) {
        return visit(ctx.creator());
    }

    @Override
    public ASTNode visitFuncCall(MxParser.FuncCallContext ctx) {
        FuncCallExpr res=new FuncCallExpr(new position(ctx),(ExprNode) visit(ctx.expression()));
        if(res.base instanceof MemberAccessExpr){
            ((MemberAccessExpr)(res.base)).isFunc=true;
            res.base.assignable=false;
        }
        if(ctx.expressionList()!=null) res.exprList.addAll(((ExprListExpr)visit(ctx.expressionList())).exprList);
        return res;
    }

    @Override
    public ASTNode visitIdentifier(MxParser.IdentifierContext ctx) {
        return new VarExpr(new position(ctx),ctx.Identifier().getText());
    }

    @Override
    public ASTNode visitSuffixExpr(MxParser.SuffixExprContext ctx) {
        return new SuffixExpr(new position(ctx),(ExprNode) visit(ctx.expression()),ctx.op.getText());
    }

    @Override
    public ASTNode visitMemberAccess(MxParser.MemberAccessContext ctx) {
        return new MemberAccessExpr(new position(ctx),(ExprNode) visit(ctx.expression()),ctx.Identifier().getText());
    }

    @Override
    public ASTNode visitConst(MxParser.ConstContext ctx) {
        return visit(ctx.constant());
    }

    @Override
    public ASTNode visitBinaryExpr(MxParser.BinaryExprContext ctx) {
        return new BinaryExpr(new position(ctx),(ExprNode) visit(ctx.expression(0)),(ExprNode) visit(ctx.expression(1)),ctx.op.getText());
    }

    @Override
    public ASTNode visitThis(MxParser.ThisContext ctx) {
        return new ThisExpr(new position(ctx));
    }

    @Override
    public ASTNode visitSubscript(MxParser.SubscriptContext ctx) {
        return new SubscriptExpr(new position(ctx),(ExprNode) visit(ctx.expression(0)),(ExprNode) visit(ctx.expression(1)));
    }

    @Override
    public ASTNode visitUnaryExpr(MxParser.UnaryExprContext ctx) {
        return new UnaryExpr(new position(ctx),(ExprNode) visit(ctx.expression()),ctx.op.getText());
    }

    @Override
    public ASTNode visitSubExpression(MxParser.SubExpressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public ASTNode visitConstant(MxParser.ConstantContext ctx) {
        if(ctx.DecimalInteger()!=null) return new IntConstExpr(new position(ctx),Integer.parseInt(ctx.DecimalInteger().getText()));
        else if(ctx.boolValue!=null) return new BoolConstExpr(new position(ctx),Boolean.parseBoolean(ctx.boolValue.getText()));
        else if(ctx.StringLiteral()!=null) return new StrConstExpr(new position(ctx),ctx.StringLiteral().getText());
        else return new NullConstExpr(new position(ctx));
    }

    @Override
    public ASTNode visitErrorCreator(MxParser.ErrorCreatorContext ctx) {
        throw new syntaxError("ErrorCreator",new position(ctx));
    }

    @Override
    public ASTNode visitArrayCreator(MxParser.ArrayCreatorContext ctx) {
        NewExpr res=new NewExpr(new position(ctx),(TypeNode) visit(ctx.basicType()),(ctx.getChildCount()-ctx.expression().size()-1)/2);
        for(ParserRuleContext x : ctx.expression()){
            res.exprList.add((ExprNode) visit(x));
        }
        return res;
    }

    @Override
    public ASTNode visitClassCreator(MxParser.ClassCreatorContext ctx) {
        return new NewExpr(new position(ctx),(TypeNode) visit(ctx.basicType()),0);
    }

    @Override
    public ASTNode visitBasicCreator(MxParser.BasicCreatorContext ctx) {
        return new NewExpr(new position(ctx),(TypeNode) visit(ctx.basicType()),0);
    }
}