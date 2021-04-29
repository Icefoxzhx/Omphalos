package ASM.inst;

import ASM.Function;
import ASM.Root;
import ASM.operand.Register;
import Util.symbol.FuncSymbol;

import java.util.LinkedHashSet;

public class Call extends Inst{
	public Root root;
	public FuncSymbol func;
	public Call(FuncSymbol func,Root root){
		this.func=func;
		this.root=root;
	}

	@Override
	public LinkedHashSet<Register> getUse() {
		LinkedHashSet<Register> res=new LinkedHashSet<>();
		int sz=func.paramList.size()+(func.inClass?1:0);;
		for(int i=0;i<Integer.min(sz, 8);i++){
			res.add(root.getPReg(10+i));
		}
		return res;
	}

	@Override
	public LinkedHashSet<Register> getDef() {
		return new LinkedHashSet<>(root.getCallerSave());
	}

	@Override
	public void replaceUse(Register x, Register y) {

	}

	@Override
	public void replaceDef(Register x, Register y) {

	}


	@Override
	public String toString() {
		return "call "+func.abs_name;
	}
}
