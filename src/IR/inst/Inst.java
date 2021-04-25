package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;

public abstract class Inst {
	public Register reg;
	public Block block;
	public Inst(Block block,Register reg){
		this.reg=reg;
		this.block=block;
	}
	public abstract ArrayList<Operand> getUse();
	public abstract void replaceUse(Operand x, Operand y);
	public abstract String toString();
}
