package IR.inst;

import java.io.PrintStream;

public class Call extends Inst{
	public String func_name;
	public Call(String name){
		this.func_name=name;
	}

	@Override
	public void printASM(PrintStream prt) {
		prt.println("\tcall "+func_name);
	}
}
