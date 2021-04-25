package IR.inst;

import IR.Block;
import IR.operand.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Load extends Inst{
	public Operand addr;
	public Load(Block block, Register rd, Operand addr){
		super(block, rd);
		this.addr=addr;
	}

	@Override
	public ArrayList<Operand> getUse() {
		return new ArrayList<>(Collections.singletonList(addr));
	}

	@Override
	public void replaceUse(Operand x, Operand y) {
		if(addr==x) addr=y;
	}

	@Override
	public String toString() {
		return "lw "+reg.toString()+", 0("+addr.toString()+")";
	}

	/*
	@Override
	public void printASM(PrintStream prt) {
		if(addr instanceof VReg && addr.color==null){
			prt.println("\tlw t3," + printVReg(-(((VReg) addr).id + 1) * 4));
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
			prt.println("\tlw t6," + printVReg(-(((VReg) rd).id + 1) * 4));
			prt.println("\tsw " + rd.toString()+",  0(t6)");
		}else if(rd instanceof VReg){
			prt.println("\tsw "+rd.toString()+", " + printVReg(-(((VReg) rd).id + 1) * 4));
		}else if(rd instanceof Symbol){
			prt.println("\tsw " + rd.toString() + ", " + ((Symbol) rd).name + ", t6");
		}
		addr.color=null;
		rd.color=null;
	}

	 */
}
