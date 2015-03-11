package net.frazew.wolframcasio.response;

public class Response {
	protected EnumResponse type;
	protected String content;
	
	public Response(String content) {
		this.type = EnumResponse.BASIC;
		this.content = content;
	}
	
	public String generateResponse() {
		String response = this.content;
		if (response.contains("integral")) {
			String[] sides = response.split("=");
			String integral = sides[0].replace("integral ", "integral(") + ")";
			response = integral + " = " + sides[1];
		}
		return response;
	}
	
	public EnumResponse getType() {
		return type;
	}

	enum EnumResponse {
		BASIC("basic"),
		TABLE("table"),
		IMAGE("image"),
		NUMBERTABLE("numbertable");
		
		private String name;
		EnumResponse(String name) {
			this.name = name;
		}
	}
}
