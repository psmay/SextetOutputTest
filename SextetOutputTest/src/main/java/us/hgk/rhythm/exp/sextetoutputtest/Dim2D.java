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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Dim2D {
	private final double width;
	private final double height;

	private Dim2D(double width, double height) {
		this.width = width;
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public double getAspect() {
		return width / height;
	}

	public Dimension2D copyTo(Dimension2D dest) {
		dest.setSize(width, height);
		return dest;
	}

	public Dimension2D toDimension2D() {
		return new Dimension2D() {
			private double w = width;
			private double h = height;

			@Override
			public void setSize(double width, double height) {
				w = width;
				h = height;
			}

			@Override
			public double getWidth() {
				return w;
			}

			@Override
			public double getHeight() {
				return h;
			}
		};
	}

	public Dimension toAwtDimension() {
		return new Dimension((int) Math.round(width), (int) Math.round(height));
	}

	public Dimension toPositiveAwtDimension() {
		int newW = (int) Math.round(width);
		int newH = (int) Math.round(height);

		return new Dimension(Math.max(1, newW), Math.max(1, newH));
	}

	public static Dim2D get(double width, double height) {
		return new Dim2D(width, height);
	}

	public static Dim2D get(Dimension2D d) {
		return get(d.getWidth(), d.getHeight());
	}

	public static Dim2D get(Component component) {
		return Dim2D.get(component.getWidth(), component.getHeight());
	}

	public static Dim2D get(BufferedImage img) {
		return Dim2D.get(img.getWidth(), img.getHeight());
	}

	public static Dim2D get(Image img) {
		return Dim2D.get(img.getWidth(null), img.getHeight(null));
	}

	public Dim2D changeWidth(double newWidth) {
		return Dim2D.get(newWidth, height);
	}

	public Dim2D changeHeight(double newHeight) {
		return Dim2D.get(width, newHeight);
	}

	public Dim2D changeAspectKeepWidth(double aspect) {
		return changeHeight(width / aspect);
	}

	public Dim2D changeAspectKeepHeight(double aspect) {
		return changeWidth(height * aspect);
	}

	public Dim2D changeAspectWithin(double aspect) {
		if (width > aspect * height) {
			return changeAspectKeepHeight(aspect);
		} else {
			return changeAspectKeepWidth(aspect);
		}
	}

	public Dim2D ensureMinimum(double minWidth, double minHeight) {
		if (width < minWidth || height < minHeight) {
			return get(Math.max(minWidth, width), Math.max(minHeight, height));
		}
		return this;
	}

	public Dim2D ensureOneOneMinimum() {
		return ensureMinimum(1.0, 1.0);
	}

	public Dim2D round() {
		long w = Math.round(width);
		long h = Math.round(height);
		return get(w, h);
	}

	// round and ensure 1x1 minimum
	public Dim2D adjusted() {
		long w = Math.max(1, Math.round(width));
		long h = Math.max(1, Math.round(height));
		return get(w, h);
	}

	public Point2D.Double getCenteredRectangleOffset(Dim2D innerRectangleDimensions) {
		double x = (this.width - innerRectangleDimensions.width) * 0.5;
		double y = (this.height - innerRectangleDimensions.height) * 0.5;
		return new Point2D.Double(x, y);
	}

	public boolean isWidthPositive() {
		return width > 0;
	}

	public boolean isHeightPositive() {
		return height > 0;
	}

	public boolean isPositive() {
		return isWidthPositive() && isHeightPositive();
	}

}
