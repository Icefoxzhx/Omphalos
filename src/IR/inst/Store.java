package IR.inst;

import IR.operand.*;

import java.io.PrintStream;

public class Store extends Inst{
	public Operand rd,addr;
	public Store(Operand rd,Operand addr){
		this.rd=rd;
		this.addr=addr;
	}
	//to be modified
	@Override
	public void printASM(PrintStream prt) {
		if(rd instanceof VReg){
			prt.println("\tlw t3," + -(((VReg) rd).id + 1) * 4 + "(s0)");
			rd.color=new PReg("t3");
		}
		if(addr instanceof Address){
			prt.println("\tlw t4," + -(((VReg) addr).id + 1) * 4 + "(s0)");
			prt.println("\tsw " + rd.toString()+", " + "0" + "(t4)");
		}else if(addr instanceof Symbol){
			prt.println("\tsw "+rd.toString()+", "+addr.toString()+", t4");
		}else {
			prt.println("\tsw " + rd.toString()+", "+-(((VReg) addr).id + 1) * 4 + "(s0)");
		}
		rd.color=addr.color=null;

		if(rd instanceof VReg && rd.color==null){
			prt.println("\tlw t3," + -(((VReg) rd).id + 1) * 4 + "(s0)");
			rd.color=new PReg("t3");
		}
		if(rd instanceof Symbol && addr.color==null){
			prt.println("\tlw t3, " + rd.toString());
			rd.color=new PReg("t3");
		}
		if(rd instanceof Address){
			prt.println("\tlw "+rd.toString()+", 0("+rd.toString()+")");
		}
		if((rd instanceof VReg|| rd instanceof Symbol) && rd.color==null){
			rd.color=new PReg("t5");
		}
		prt.println("\tlw "+rd.toString()+", 0("+addr.toString()+")");
		if(rd instanceof Address){
			prt.println("\tlw t6," + -(((VReg) rd).id + 1) * 4 + "(s0)");
			prt.println("\tsw " + rd.toString()+",  0(t6)");
		}else if(rd instanceof VReg){
			prt.println("\tsw "+rd.toString()+", " + -(((VReg) rd).id + 1) * 4 + "(s0)");
		}else if(rd instanceof Symbol){
			prt.println("\tsw " + rd.toString() + ", " + ((Symbol) rd).name + ", t6");
		}
	}
}
