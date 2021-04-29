package IR.inst;

import IR.Block;
import IR.operand.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class Store extends Inst{
	public Operand rs,addr;
	public Store(Block block, Operand rs, Operand addr){
		super(block,null);
		this.rs=rs;
		this.addr=addr;
	}


	@Override
	public ArrayList<Operand> getUse() {
		ArrayList<Operand> res=new ArrayList<>();
		res.add(addr);
		res.add(rs);
		return res;
	}

	@Override
	public void replaceUse(Operand x, Operand y) {
		if(rs==x) rs=y;
		if(addr==x) addr=y;
	}

	@Override
	public String toString() {
		return "sw "+rs.toString()+", "+addr.toString();
	}
}
