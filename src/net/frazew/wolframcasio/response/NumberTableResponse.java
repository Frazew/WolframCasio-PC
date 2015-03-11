package net.frazew.wolframcasio.response;

import java.util.ArrayList;

public class NumberTableResponse extends Response {
	private ArrayList<String> operators = new ArrayList();
	private ArrayList<Integer> numbers = new ArrayList();
	private int result;
	
	public NumberTableResponse(String content) {
		super(content);
		this.type = EnumResponse.NUMBERTABLE;
		String[] errythin = this.content.split("\\|");
		for (int i = 0; i < errythin.length; i++) {
			String whatever = errythin[i].replace(" ", "");
			if (whatever.equals("+") || whatever.equals("=")) {
				operators.add(whatever);
			} else if (isInteger(whatever) && !whatever.equals(" ")) {
				if (i == errythin.length -1) result = Integer.parseInt(whatever);
				else numbers.add(Integer.parseInt(whatever));
			}
		}
	}
	
	public String generateResponse() {		
		int index = 0;
		String complete = "";
		for (int number : numbers) {
			complete = complete + number;
			System.out.println(number);
			complete = complete + operators.get(index);
			index++;
		}
		return complete + result;
	}
	
	private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
}
