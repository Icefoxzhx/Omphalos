package IR.inst;

import java.io.PrintStream;

public abstract class Inst {
	public abstract void printASM(PrintStream prt);
	public String printVReg(int id){
		if(-id<2048){
			return id+"(s0)";
		}else{
			System.out.println("\tli s1, "+id);
			System.out.println("\tadd s1, s1, s0");
			return "0(s1)";
		}
	}
}
