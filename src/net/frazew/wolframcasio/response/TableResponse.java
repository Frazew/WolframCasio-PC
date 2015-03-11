package net.frazew.wolframcasio.response;

import java.util.HashMap;

public class TableResponse extends Response {
	private HashMap<String, String> table = new HashMap();
	
	public TableResponse(String content) {
		super(content);
		this.type = EnumResponse.TABLE;
		String[] split = this.content.split("TODO");
		for (int i = 0; i < split.length; i++) {
		}
	}
	
	public String generateResponse() {
		return this.content;
	}
}
