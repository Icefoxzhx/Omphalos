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
		return "lw "+reg.toString()+", "+addr.toString();
	}
}
