package net.frazew.wolframcasio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import jssc.SerialPort;
import jssc.SerialPortException;
import net.frazew.wolframcasio.response.ImageResponse;
import net.frazew.wolframcasio.response.NumberTableResponse;
import net.frazew.wolframcasio.response.Response;
import net.frazew.wolframcasio.response.TableResponse;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class WolframCasio {
	static SAXBuilder saxBuilder = new SAXBuilder();
	private Document document;
	private ArrayList<String> titles = new ArrayList();
	private ArrayList<Response> screens = new ArrayList();
	static byte[] NL_REPLACE = new byte[] {0xA};
	private int currentScreen = 0;
	private SerialPort serialPort;
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private HashMap<byte[], byte[]> replacement = new HashMap();
	private String apiKey;
	
	public WolframCasio(String port, String apiKey) {
		this.apiKey = apiKey;
		serialPort = new SerialPort(port);
		replacement.put("&".getBytes(), "arcsin".getBytes());
		replacement.put("~".getBytes(), "sin".getBytes());
		replacement.put("\"".getBytes(), "arccos".getBytes());
		replacement.put("#".getBytes(), "cos".getBytes());
		replacement.put("'".getBytes(), "arctan".getBytes());
		replacement.put("|".getBytes(), "tan".getBytes());
		replacement.put("`".getBytes(), "sqrt(".getBytes());
		replacement.put("\\".getBytes(), "^2".getBytes());
		replacement.put("@".getBytes(), "pi".getBytes());
		replacement.put("?".getBytes(), " ".getBytes());
	}
	
	public boolean connect() throws SerialPortException {
		return serialPort.openPort() && serialPort.setParams(9600, 8, 0, 0);
	}
	
	public void loop() throws SerialPortException, IOException {
		String request = "";
    	byte[] buffer = new byte[1];
		while (true) {
        	read(buffer);
        	boolean shouldWrite = true;
        	if (buffer[0] == 0x18) break;
        	if (buffer[0] == 0x1B) {
        		shouldWrite = false;
        		request = "";
        	}
        	if (buffer[0] == 0x7F) {
        		shouldWrite = false;
        		request = request.substring(0, request.length() - 1);
        		write(request.getBytes());
        	}
      		ByteArrayInputStream bis;
      		InputStream ris;
      		ByteArrayOutputStream bos = new ByteArrayOutputStream();
      		int b;
      		for (Entry<byte[], byte[]> entry : this.replacement.entrySet()) {
      			bis = new ByteArrayInputStream(buffer);
      		 	ris = new ReplacingInputStream(bis, entry.getKey(), entry.getValue());
      		 	while (-1 != (b = ris.read())) bos.write(b);
      			buffer = bos.toByteArray();
      			bos.flush();
      			bos.reset();
      		}
      		String str = "";
        	if (shouldWrite) str = new String(buffer);
        	if (shouldWrite && !str.trim().isEmpty()) {
        		str = str.trim();
        		write(buffer);
        	} else if (shouldWrite && str.trim().isEmpty()){
        		str = "";
        	}
        	if (buffer[0] == 0xA) {
        		write((byte) 0x3);
	            write("---------------------".getBytes());
	            write("Recherche en cours...".getBytes());
	            write("---------------------".getBytes());

	            if (request != "") {
	            	String json = null;
	            	try {
	        	        json = request(new URL("http://api.wolframalpha.com/v2/query?input=" + URLEncoder.encode(request, "utf-8") + "&appid=" + this.apiKey));
	            	} catch (Exception e) {
	            		e.printStackTrace();
	            	}
	            	
	            	if (json != null) {
	            		titles.clear();
	            		screens.clear();
	            		currentScreen = 0;
	            		SAXBuilder sxb = new SAXBuilder();
	            	      try
	            	      {
	            	         document = saxBuilder.build(new StringReader(json));
	            	      }
	            	      catch(Exception e){System.out.println(e);}
	            	      
	            	      List list = document.getRootElement().getChildren("pod");

	            	      Iterator i = list.iterator();
	            	      while(i.hasNext())
	            	      {
	            	    	  Element elem = (Element) i.next();

	            	    	  if (elem.getChild("subpod").getChild("plaintext").getValue() != null && !elem.getChild("subpod").getChild("plaintext").getValue().isEmpty()) {
	            	    		  titles.add(elem.getAttribute("title").getValue());
	            	    		  if (elem.getAttribute("title").getValue().equals("Manipulatives illustration")) screens.add(new NumberTableResponse(elem.getChild("subpod").getChild("plaintext").getValue()));
	            	    		  else if (elem.getChild("subpod").getChild("plaintext").getValue().contains("|")) screens.add(new TableResponse(elem.getChild("subpod").getChild("plaintext").getValue()));
	            	    		  else screens.add(new Response(elem.getChild("subpod").getChild("plaintext").getValue()));
	            	    	  } else if (elem.getChild("subpod").getChild("img") != null) {
	            	    		  titles.add(elem.getAttribute("title").getValue());
	            	    		  screens.add(new ImageResponse(elem.getChild("subpod").getChild("img").getAttributeValue("src")));
	            	    	  }
	            	      }
				            write((byte) 0x3);
	            	      write(titles.get(currentScreen).getBytes());
      	            		write((byte)0xA);
      	            		write("---------------------".getBytes());
      	            		write(screens.get(currentScreen).generateResponse().getBytes());
            	    	  while (true) {
            	    		  read(buffer);
            	    		  if (buffer[0] == 0x18) return;
            	    		  int prevCurrentScreen = currentScreen;
          	            	if (buffer[0] == 0xA) break;
          	            	if (buffer[0] == 0x17) {
          	            		currentScreen++;
          	            	}
          	            	if (buffer[0] == 0x16) {
          	            		currentScreen--;
          	            	}
          	            	if (currentScreen < 0) currentScreen = 0;
          	            	if (currentScreen >= screens.size()) currentScreen = screens.size() - 1;
          	            	if (prevCurrentScreen != currentScreen) {
          	            		write((byte)0x3);
          	            		byte[] screen = screens.get(currentScreen).generateResponse().getBytes();
          	            		bis = new ByteArrayInputStream(screen);
          	            		write(titles.get(currentScreen).getBytes());
          	            		write((byte)0xA);
          	            		write("---------------------".getBytes());
          	            		ris = new ReplacingInputStream(bis, " | ".getBytes("ascii"), NL_REPLACE);
	          	                while (-1 != (b = ris.read())) bos.write(b);
          	            		write(bos.toByteArray());
          	        			bos.reset();
          	            	}
          	            	}
	            	}
	            }
	            request = "";
        	}
        	request = request + str;
    	}
	}

	private String request(URL url) throws IOException {
		InputStream inputStream = null;
        inputStream = url.openConnection(Proxy.NO_PROXY).getInputStream();
        return IOUtils.toString(inputStream);
	}

	private void read(byte[] buffer) throws SerialPortException {
		buffer = serialPort.readBytes(1);
	}
	
	private void write(byte par) throws SerialPortException {
		serialPort.writeByte(par);
	}
	
	private void write(byte[] par) throws SerialPortException {
		serialPort.writeBytes(par);
	}
	
	public void close() throws SerialPortException {
		serialPort.closePort();
	}
	
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
