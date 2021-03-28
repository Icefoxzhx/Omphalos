package IR.inst;

import IR.operand.Operand;
import IR.operand.PReg;
import IR.operand.Symbol;
import IR.operand.VReg;

import java.io.PrintStream;

public class Store extends Inst{
	public Operand rd,addr;
	public Store(Operand rd,Operand addr){
		this.rd=rd;
		this.addr=addr;
	}
	@Override
	public void printASM(PrintStream prt) {
		if(rd instanceof VReg){
			prt.println("\tlw t3," + -(((VReg) rd).id + 1) * 4 + "(s0)");
			rd.color=new PReg("t3");
		}
		if(addr.isptr){
			prt.println("\tlw t4," + -(((VReg) addr).id + 1) * 4 + "(s0)");
			prt.println("\tsw " + rd.toString()+", " + "0" + "(t4)");
		}else if(addr instanceof Symbol){
			prt.println("\tsw "+rd.toString()+", "+addr.toString()+", t4");
		}else {
			prt.println("\tsw " + rd.toString()+", "+-(((VReg) addr).id + 1) * 4 + "(s0)");
		}
		rd.color=addr.color=null;
	}
}
