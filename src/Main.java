import AST.ProgramNode;
import FrontEnd.ASTBuilder;
import FrontEnd.SemanticChecker;
import FrontEnd.SymbolCollector;
import FrontEnd.TypeCollector;
import Parser.MxLexer;
import Parser.MxParser;
import Util.MxErrorListener;
import Util.error.Error;
import Util.symbol.Scope;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;

public class Main{
	public static void main(String[] args)throws Exception{
		String name="D:\Useless\Study\Compiler Design\Compiler-2021-testcases-6958d31c7c7a2736eda184072c247b34402413af\sema\basic-package\basic-1.mx";
		InputStream input=new FileInputStream(name);
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
        } catch(Error er){
        	System.err.println(er.toString());
            throw new RuntimeException();
        }
    }
}