package us.hgk.rhythm.exp.sextetoutputtest;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Renderer {
	private BufferedImage onStateImage, offStateImage;
	private BufferedImage[] pages = new BufferedImage[2];
	private int originalWidth, originalHeight;
	private int actualWidth, actualHeight;
	private int renderedPageIndex;
	AffineTransform scaleTransform;

	public Renderer(Image originalOffStateImage, Image originalOnStateImage, int width, int height) {
		this.actualWidth = Math.max(1, width);
		this.actualHeight = Math.max(1, height);

		originalWidth = originalOffStateImage.getWidth(null);
		originalHeight = originalOffStateImage.getHeight(null);

		if (originalWidth < 1 || originalHeight < 1) {
			throw new IllegalArgumentException("Original image dimensions must be positive");
		}

		double scaleX = ((double) actualWidth) / originalWidth;
		double scaleY = ((double) actualHeight) / originalHeight;

		scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);

		int type = BufferedImage.TYPE_INT_ARGB;

		offStateImage = new BufferedImage(actualWidth, actualHeight, type);
		onStateImage = new BufferedImage(actualWidth, actualHeight, type);
		pages[0] = new BufferedImage(actualWidth, actualHeight, type);
		pages[1] = new BufferedImage(actualWidth, actualHeight, type);

		scaleImageInto(offStateImage, originalOffStateImage);
		scaleImageInto(onStateImage, originalOnStateImage);
		drawImageInto(pages[0], offStateImage);
		drawImageInto(pages[1], offStateImage);
		renderedPageIndex = 0;
	}

	public int getWidth() { return actualWidth; }
	public int getHeight() { return actualHeight; }
	
	private void scaleImageInto(BufferedImage dest, Image src) {
		Graphics2D g = dest.createGraphics();
		g.drawImage(src, 0, 0, actualWidth, actualHeight, null);
		g.dispose();
	}

	private void drawImageInto(BufferedImage dest, Image src) {
		Graphics2D g = dest.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
	}

	private static final Color BLANK_COLOR = new Color(0, true);

	private int pageIndexAfter(int n) {
		return (n + 1) % pages.length;
	}

	private int getWorkPageIndex() {
		return pageIndexAfter(renderedPageIndex);
	}

	synchronized void renderImage(Shape clipShape) {
		int workPageIndex = getWorkPageIndex();

		System.err.println("Rendering image on page " + workPageIndex);
		
		BufferedImage workPage = pages[workPageIndex];

		Graphics2D g2 = workPage.createGraphics();
		g2.transform(scaleTransform);

		
		Composite comp = g2.getComposite();
		Color c = g2.getColor();
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));		
		g2.setColor(BLANK_COLOR);
		g2.fillRect(0, 0, originalWidth, originalHeight);
		
		g2.setColor(c);
		g2.setComposite(comp);
		
		
		
		
		// Clear image
		//g2.setColor(BLANK_COLOR);
		//g2.fillRect(0, 0, originalWidth, originalHeight);

		// Draw unlit, set clip area, draw lit, restore clip area
		g2.drawImage(offStateImage, 0, 0, originalWidth, originalHeight, null);
		//Shape normalClip = g2.getClip();
		g2.setClip(clipShape);

		g2.drawImage(onStateImage, 0, 0, originalWidth, originalHeight, null);
		
		
		//g2.setClip(normalClip);
		

		g2.dispose();

		System.err.println("Done rendering; advancing rendered page index to " +workPageIndex);
		renderedPageIndex = workPageIndex;
	}

	void drawRenderedImage(Graphics g) {
		int i = renderedPageIndex;
		System.err.println("Drawing rendered image from page " + i);
		g.drawImage(pages[i], 0, 0, actualWidth, actualHeight, null);
	}
}
