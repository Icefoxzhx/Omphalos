package ASM.inst;

import ASM.operand.*;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class Store extends Inst{
	public Register rs,addr;
	public Imm offset;
	public Store(Register rs, Register addr, Imm offset){
		this.rs=rs;
		this.addr=addr;
		this.offset=offset;
	}

	@Override
	public LinkedHashSet<Register> getUse() {
		return new LinkedHashSet<>(Arrays.asList(rs,addr));
	}

	@Override
	public LinkedHashSet<Register> getDef() {
		return new LinkedHashSet<>();
	}

	@Override
	public void replaceUse(Register x, Register y) {
		if(rs==x) rs=y;
		if(addr==x) addr=y;
	}

	@Override
	public void replaceDef(Register x, Register y) {

	}

	@Override
	public String toString() {
		return "sw "+rs.toString()+", "+offset.toString()+"("+addr.toString()+")";
	}
	/*
	@Override
	public void printASM(PrintStream prt) {
		if(rd instanceof VReg){
			prt.println("\tlw t3," + printVReg(-(((VReg) rd).id + 1) * 4));
			rd.color=new PReg("t3");
		}
		if(addr instanceof Address){
			prt.println("\tlw t4," + printVReg(-(((VReg) addr).id + 1) * 4));
			prt.println("\tsw " + rd.toString()+", " + "0" + "(t4)");
		}else if(addr instanceof Symbol){
			prt.println("\tsw "+rd.toString()+", "+addr.toString()+", t4");
		}else {
			prt.println("\tsw " + rd.toString()+", "+ printVReg(-(((VReg) addr).id + 1) * 4));
		}
		rd.color=addr.color=null;

		if(rd instanceof VReg && rd.color==null){
			prt.println("\tlw t3," + printVReg(-(((VReg) rd).id + 1) * 4));
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
			prt.println("\tlw t6," + printVReg(-(((VReg) rd).id + 1) * 4));
			prt.println("\tsw " + rd.toString()+",  0(t6)");
		}else if(rd instanceof VReg){
			prt.println("\tsw "+rd.toString()+", " + printVReg(-(((VReg) rd).id + 1) * 4));
		}else if(rd instanceof Symbol){
			prt.println("\tsw " + rd.toString() + ", " + ((Symbol) rd).name + ", t6");
		}
	}

	 */
}
