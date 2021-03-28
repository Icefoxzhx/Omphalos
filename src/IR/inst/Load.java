package IR.inst;

import IR.operand.Operand;

import java.io.PrintStream;

public class Load extends Inst{
	public Operand ld,addr;
	public Load(Operand ld,Operand addr){
		this.ld=ld;
		this.addr=addr;
	}
	@Override
	public void printASM(PrintStream prt) {
		prt.println("\tlw "+ld.toString()+", "+addr.toString());
	}
}
