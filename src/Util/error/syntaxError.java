package Util.error;
import Util.position;

public class syntaxError extends Error{
	public syntaxError(String msg, position pos){
		super("Syntax Error: "+msg,pos);
	}
}