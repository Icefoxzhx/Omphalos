package Util.error;
import Util.position;

public abstract class Error extends RuntimeException{
	private position pos;
	private String message;
	
	public Error(String msg,position pos){
		this.pos=pos;
		this.message=msg;
	}

	public String toString(){
		return message+": "+pos.toString();
	}
}