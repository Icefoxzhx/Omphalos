package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;
import Util.symbol.FuncSymbol;

import java.util.ArrayList;

public class Call extends Inst{
	public ArrayList<Operand> params=new ArrayList<>();
	public FuncSymbol func;
	public Call(Block block,FuncSymbol func,Register rd){
		super(block,rd);
		this.func=func;
	}

	@Override
	public ArrayList<Operand> getUse() {
		return params;
	}

	@Override
	public void replaceUse(Operand x, Operand y) {
		for(int i=0;i<params.size();++i){
			if(params.get(i)==x) params.set(i,y);
		}
	}

	@Override
	public String toString() {
		return "call "+func.abs_name;
	}
}
