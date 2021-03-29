package IR.inst;

import IR.operand.*;

import java.io.PrintStream;

public class Load extends Inst{
	public Operand rd,addr;
	public Load(Operand rd,Operand addr){
		this.rd=rd;
		this.addr=addr;
	}
	@Override
	public void printASM(PrintStream prt) {
		if(addr instanceof VReg && addr.color==null){
			prt.println("\tlw t3," + -(((VReg) addr).id + 1) * 4 + "(s0)");
			addr.color=new PReg("t3");
		}
		if(addr instanceof Symbol && addr.color==null){
			prt.println("\tlw t3, " + addr.toString());
			addr.color=new PReg("t3");
		}
		if(addr instanceof Address){
			prt.println("\tlw "+addr.toString()+", 0("+addr.toString()+")");
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
		addr.color=null;
		rd.color=null;
	}
}
