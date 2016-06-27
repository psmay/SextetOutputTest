/*******************************************************************************
 * ("The Expat License")
 * 
 * Copyright © 2016 Peter S. May
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package us.hgk.rhythm.exp.sextetoutputtest;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Renderer {
	private BufferedImage onStateImage, offStateImage;
	private BufferedImage[] pages = new BufferedImage[2];
	private int renderedPageIndex;
	AffineTransform scaleTransform;
	private final Dim2D actualDim, originalDim;
	private final int actualWidth, actualHeight, originalWidth, originalHeight;

	public Renderer(Image originalOffStateImage, Image originalOnStateImage, int width, int height) {
		actualDim = Dim2D.get(width, height).adjusted();
		Dimension actualDimInt = actualDim.toAwtDimension();
		actualWidth = actualDimInt.width;
		actualHeight = actualDimInt.height;

		originalDim = Dim2D.get(originalOffStateImage).adjusted();
		Dimension originalDimInt = originalDim.toAwtDimension();
		originalWidth = originalDimInt.width;
		originalHeight = originalDimInt.height;

		scaleTransform = getQuotientAsScale(actualDim, originalDim);

		int type = BufferedImage.TYPE_INT_ARGB;

		BufferedImage[] images = createBufferedImages(actualWidth, actualHeight, type, 4);
		pages[0] = images[0];
		pages[1] = images[1];
		offStateImage = images[2];
		onStateImage = images[3];

		scaleImageInto(offStateImage, originalOffStateImage);
		scaleImageInto(onStateImage, originalOnStateImage);
		drawImageInto(pages[0], offStateImage);
		drawImageInto(pages[1], offStateImage);
		renderedPageIndex = 0;
	}

	private static BufferedImage[] createBufferedImages(int width, int height, int imgType, final int count) {
		BufferedImage[] images = new BufferedImage[count];

		for (int i = 0; i < count; ++i) {
			images[i] = new BufferedImage(width, height, imgType);
		}

		return images;
	}

	private AffineTransform getQuotientAsScale(Dim2D a, Dim2D b) {
		double scaleX = a.getWidth() / b.getWidth();
		double scaleY = a.getHeight() / b.getHeight();

		AffineTransform st = AffineTransform.getScaleInstance(scaleX, scaleY);
		return st;
	}

	public int getWidth() {
		return actualWidth;
	}

	public int getHeight() {
		return actualHeight;
	}

	public Dimension getDimension() {
		return new Dimension(actualWidth, actualHeight);
	}

	// This bit is necessary to scale down an image with any sort of quality;
	// bilinear/bicubic interpolation appears to be ineffectual without it.
	// It is allowed to be a little slow since it only occurs on a resize rather
	// than on every frame.
	//
	// The algorithm is to scale the image down in steps, with each step scaling
	// each dimension down but by a factor no smaller than (about) 1/2.
	private void scaleImageInto(BufferedImage dest, Image src) {
		int currentWidth = src.getWidth(null);
		int currentHeight = src.getWidth(null);
		Image currentImage = src;
		BufferedImage nextImage = null;

		while (true) {
			int nextWidth = Math.max(2 * currentWidth / 3, actualWidth);
			int nextHeight = Math.max(2 * currentHeight / 3, actualHeight);

			if (nextWidth == actualWidth && nextHeight == actualHeight) {
				nextImage = dest;
			} else {
				nextImage = new BufferedImage(nextWidth, nextHeight, dest.getType());
			}

			Graphics2D dg = nextImage.createGraphics();
			dg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			dg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			dg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			dg.drawImage(currentImage, 0, 0, nextWidth, nextHeight, null);
			dg.dispose();

			if (nextImage == dest) {
				break;
			} else {
				currentImage = nextImage;
				currentWidth = nextWidth;
				currentHeight = nextHeight;
				nextImage = null;
			}
		}
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
		BufferedImage workPage = pages[workPageIndex];

		{
			Graphics2D g2 = workPage.createGraphics();

			// This transform is used to allow the clip shape, which has been
			// defined in terms of the original image, to work with the resized
			// image instead.
			g2.transform(scaleTransform);

			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			g2.setColor(BLANK_COLOR);
			g2.fillRect(0, 0, originalWidth, originalHeight);

			g2.drawImage(offStateImage, 0, 0, originalWidth, originalHeight, null);
			g2.setClip(clipShape);
			g2.drawImage(onStateImage, 0, 0, originalWidth, originalHeight, null);
			
			g2.dispose();
		}

		// Advance to next page
		renderedPageIndex = workPageIndex;
	}

	void drawRenderedImage(Graphics g) {
		BufferedImage renderedPage = pages[renderedPageIndex];
		g.drawImage(renderedPage, 0, 0, actualWidth, actualHeight, null);
	}
}
