package ASM.inst;

import ASM.operand.Register;

import java.io.PrintStream;
import java.util.LinkedHashSet;

public abstract class Inst {
	//public abstract void printASM(PrintStream prt);
	public abstract LinkedHashSet<Register> getUse();
	public abstract LinkedHashSet<Register> getDef();
	public abstract void replaceUse(Register x, Register y);
	public abstract void replaceDef(Register x, Register y);
	public abstract String toString();
	public boolean isOriginalOffset=false;
	/*public String printVReg(int id){
		if(-id<2048){
			return id+"(s0)";
		}else{
			System.out.println("\tli s1, "+id);
			System.out.println("\tadd s1, s1, s0");
			return "0(s1)";
		}
	}
	 */
}
