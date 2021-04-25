package IR.operand;

public class None extends Operand{
	@Override
	public String toString() {
		return "";
	}

	@Override
	public boolean equals(Operand x) {
		return (x instanceof None);
	}
}
