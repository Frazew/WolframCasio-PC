package net.frazew.wolframcasio.response;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import net.frazew.wolframcasio.response.Response.EnumResponse;

public class ImageResponse extends Response {
	private BufferedImage image;
	private ASCII ascii = new ASCII(false);
	
	public ImageResponse(String content) {
		super(content);
		this.type = EnumResponse.IMAGE;
		try {
			image = ImageIO.read(new URL(content));
			Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
			Dimension boundary = new Dimension(21, 9);
			Dimension newDim = getScaledDimension(imgSize, boundary);
			image = resize(image, image.getType(), (int)newDim.getWidth(), (int)newDim.getHeight());
			System.out.println(new ASCII(false).convert(image));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String generateResponse() {		
		//return ascii.convert(image);
		return "image WIP";
	}
	
	private static BufferedImage resize(BufferedImage originalImage, int type, Integer img_width, Integer img_height)
	{
		BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, img_width, img_height, null);
		g.dispose();
		
		return resizedImage;
	}
	
	private static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {
	    int original_width = imgSize.width;
	    int original_height = imgSize.height;
	    int bound_width = boundary.width;
	    int bound_height = boundary.height;
	    int new_width = original_width;
	    int new_height = original_height;

	    // first check if we need to scale width
	    if (original_width > bound_width) {
	        //scale width to fit
	        new_width = bound_width;
	        //scale height to maintain aspect ratio
	        new_height = (new_width * original_height) / original_width;
	    }

	    // then check if we need to scale even with the new height
	    if (new_height > bound_height) {
	        //scale height to fit instead
	        new_height = bound_height;
	        //scale width to maintain aspect ratio
	        new_width = (new_height * original_width) / original_height;
	    }

	    return new Dimension(new_width, new_height);
	}
}
