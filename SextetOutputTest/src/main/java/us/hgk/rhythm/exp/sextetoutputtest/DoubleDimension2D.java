package us.hgk.rhythm.exp.sextetoutputtest;

import java.awt.geom.Dimension2D;

public class DoubleDimension2D extends Dimension2D implements Cloneable {
	private double width, height;

	public DoubleDimension2D(double width, double height) {
		this.width = width;
		this.height = height;
	}

	public DoubleDimension2D() {
		this(0.0, 0.0);
	}

	public DoubleDimension2D(Dimension2D dimension) {
		if (dimension == null) {
			throw new NullPointerException();
		}

		this.width = dimension.getWidth();
		this.height = dimension.getHeight();
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public Object clone() {
		return new DoubleDimension2D(this);
	}

}
