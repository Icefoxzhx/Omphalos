package ASM.inst;

import ASM.operand.*;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;

public class Load extends Inst{
	public Register rd,addr;
	public Imm offset;
	public Load(Register rd, Register addr, Imm offset){
		this.rd=rd;
		this.addr=addr;
		this.offset=offset;
	}

	@Override
	public HashSet<Register> getUse() {
		return new HashSet<>(Collections.singletonList(addr));
	}

	@Override
	public HashSet<Register> getDef() {
		return new HashSet<>(Collections.singletonList(rd));
	}

	@Override
	public void replaceUse(Register x, Register y) {
		if(addr==x) addr=y;
	}

	@Override
	public void replaceDef(Register x, Register y) {
		if(rd==x) rd=y;
	}

	@Override
	public String toString() {
		return "lw "+rd.toString()+", "+offset.toString()+"("+addr.toString()+")";
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
