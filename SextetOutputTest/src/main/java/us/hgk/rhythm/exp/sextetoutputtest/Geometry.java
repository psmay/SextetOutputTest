package us.hgk.rhythm.exp.sextetoutputtest;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

class Geometry {
	public static Dimension2D getDimension2D(double width, double height) {
		return new DoubleDimension2D(width, height);
	}

	public static double getAspect(Dimension2D existing) {
		return existing.getWidth() / existing.getHeight();
	}

	public static Dimension2D nudgeToPositive(Dimension2D existing) {
		double width = Math.max(existing.getWidth(), 1.0);
		double height = Math.max(existing.getHeight(), 1.0);
		existing.setSize(width, height);
		return existing;
	}

	public static Dimension toPositiveAwtDimension(Dimension2D existing) {
		int width = (int) Math.max(Math.round(existing.getWidth()),  1.0);
		int height = (int) Math.max(Math.round(existing.getHeight()), 1.0);
		return new Dimension(width, height);
	}
	
	

	public static double getHeightForAspect(double aspect, double width) {
		return width / aspect;
	}
	
	public static double getWidthForAspect(double aspect, double height) {
		return height * aspect;
	}
	
	// Sets the width property of the existing dimension so that the
	// ratio of the new width to the existing height equals the given aspect.
	public static Dimension2D setAspectFromHeight(Dimension2D existing, double aspect) {
		return setAspectFromHeight(existing, existing.getHeight(), aspect);
	}

	private static Dimension2D setAspectFromHeight(Dimension2D dest, double height, double aspect) {
		dest.setSize(getWidthForAspect(aspect, height), height);
		return dest;
	}

	// Sets the height property of the existing dimension so that the
	// ratio of the existing width to the new height equals the given aspect.
	public static Dimension2D setAspectFromWidth(Dimension2D existing, double aspect) {
		return setAspectFromWidth(existing, existing.getWidth(), aspect);
	}

	private static Dimension2D setAspectFromWidth(Dimension2D dest, double width, double aspect) {
		dest.setSize(width, getHeightForAspect(aspect, width));
		return dest;
	}

	// Sets the width and height properties of the existing dimension to those
	// of the largest rectangle that is smaller than or equal to the existing
	// rectangle and has the given aspect.
	public static Dimension2D setAspectWithin(Dimension2D existing, double aspect) {
		double maxWidth = existing.getWidth();
		double maxHeight = existing.getHeight();

		if (maxWidth > aspect * maxHeight) {
			return setAspectFromHeight(existing, maxHeight, aspect);
		} else {
			return setAspectFromWidth(existing, maxWidth, aspect);
		}
	}

	public static Rectangle2D setAspectCenteredWithin(Rectangle2D existing, double aspect) {
		double origX = existing.getX();
		double origY = existing.getY();
		double origW = existing.getWidth();
		double origH = existing.getHeight();
		
		Dimension2D wh = setAspectWithin( getDimension2D(origW, origH), aspect );
		
		double xTranslate = (origW - wh.getWidth()) * 0.5;
		double yTranslate = (origH - wh.getHeight()) * 0.5;
		existing.setRect(origX + xTranslate, origY + yTranslate, wh.getWidth(), wh.getHeight());
		return existing;
	}
}
