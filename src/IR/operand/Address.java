package IR.operand;

public class Address extends Operand{
	public Operand base,offset;
	public Address(Operand base, Operand offset){
		this.offset=offset;
		this.base=base;
	}
	@Override
	public String toString() {
		return null;
	}
}
