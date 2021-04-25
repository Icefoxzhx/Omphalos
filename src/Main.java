import AST.ProgramNode;
import FrontEnd.*;
import BackEnd.*;
import Parser.MxLexer;
import Parser.MxParser;
import Util.MxErrorListener;
import Util.error.Error;
import Util.symbol.Scope;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.InputStream;

public class Main{
    public static void main(String[] args)throws Exception{
        boolean codegen=true,optimize=false;
        if(args.length>0){
            for(String arg:args){
                switch(arg){
                    case "-semantic" -> codegen=false;
                    case "-codegen" -> codegen=true;
                    case "-O2" -> optimize=true;
                }
            }
        }
        //String name="./sema/basic-package/basic-41.mx";
        InputStream input=System.in;//new FileInputStream(name);
        try{
            ProgramNode ASTRoot;
            MxLexer lexer=new MxLexer(CharStreams.fromStream(input));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new MxErrorListener());
            MxParser parser=new MxParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(new MxErrorListener());
            ParseTree parseTreeRoot=parser.program();
            ASTBuilder astBuilder=new ASTBuilder();
            ASTRoot=(ProgramNode) astBuilder.visit(parseTreeRoot);
            Scope global=new Scope(null);
            new SymbolCollector(global).visit(ASTRoot);
            new TypeCollector(global).visit(ASTRoot);
            global.varMap.clear();
            new SemanticChecker(global).visit(ASTRoot);
            if(!codegen) return;
            IR.Root IRRoot=new IR.Root();
            new IRBuilder(IRRoot).visit(ASTRoot);
            ASM.Root ASMRoot=new ASM.Root();
            new ASMBuilder(IRRoot,ASMRoot).run();
            new RegAllocator(ASMRoot).Run();
            new ASMPrinter(ASMRoot).run();
        } catch(Error er){
            System.err.println(er);
            throw new RuntimeException();
        }
    }
}