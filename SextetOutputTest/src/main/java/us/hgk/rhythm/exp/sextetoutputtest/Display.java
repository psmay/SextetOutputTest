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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Display {

	private final Map<Integer, LightInfo> lightMap;
	private final BufferedImage litOriginal, unlitOriginal;
	private final double desiredAspect;
	private final Dimension panelPreferredSize;
	private AffineTransform translateTransform;

	private Object lockRenderer = new Object();
	private volatile Renderer renderer;

	private JFrame frame;
	private DrawingPanel panel;

	public Display(String lightSetName) {
		String unlitName = "light-sets/" + lightSetName + "/unlit.png";
		String litName = "light-sets/" + lightSetName + "/lit.png";
		String mapName = "light-sets/" + lightSetName + "/map.tsv";

		lightMap = loadLightMap(mapName);
		litOriginal = loadPngImage(litName);
		unlitOriginal = loadPngImage(unlitName);

		desiredAspect = Dim2D.get(unlitOriginal).adjusted().getAspect();

		Dim2D size = Dim2D.get(400, 400).changeAspectWithin(desiredAspect).adjusted();
		panelPreferredSize = size.toAwtDimension();

		// This initializes mg to something very small;
		// it will be changed to something useful when we call onResize()
		resizeRenderer(1, 1);

		setUpWindow();
		frame.setVisible(true);
		onResize();
	}

	private void onResize() {
		Component component = panel;

		Dim2D panelD = Dim2D.get(component).adjusted();
		Dim2D innerD = panelD.changeAspectWithin(desiredAspect).adjusted();

		Point2D.Double offset = panelD.getCenteredRectangleOffset(innerD);

		resizeRenderer((int) innerD.getWidth(), (int) innerD.getHeight());
		translateTransform = AffineTransform.getTranslateInstance(offset.getX(), offset.getY());

		panel.repaint();
	}

	private void resizeRenderer(int proposedWidth, int proposedHeight) {
		Renderer renderer = new Renderer(unlitOriginal, litOriginal, proposedWidth, proposedHeight);

		synchronized (lockRenderer) {
			this.renderer = renderer;
		}
	}

	private void setUpWindow() {
		frame = new JFrame("SextetOutputTest");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		panel = new DrawingPanel();
		panel.addComponentListener(new ThisComponentListener());
		frame.add(panel);
		frame.pack();
	}

	void updateStates(Iterable<Integer> trueStates) {
		Area area = buildClipArea(trueStates);
		renderer.renderImage(area);
		panel.repaint();
	}

	private class ThisComponentListener extends ComponentAdapter {

		@Override
		public void componentResized(ComponentEvent e) {
			onResize();
		}

	}

	private void paintDrawingPanel(Graphics2D g2) {
		Composite comp = g2.getComposite();
		Color c = g2.getColor();

		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		g2.setColor(Color.lightGray);
		
		g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());

		g2.setColor(c);
		g2.setComposite(comp);
		
		g2.transform(translateTransform);
		
		synchronized (lockRenderer) {
			renderer.drawRenderedImage(g2);
		}

		g2.dispose();
	}

	private class DrawingPanel extends JPanel {
		private static final long serialVersionUID = -4814671716253111614L;

		public DrawingPanel() {
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(panelPreferredSize);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			paintDrawingPanel((Graphics2D) g);
		}
	}

	private Area buildClipArea(Iterable<Integer> trueStates) {
		// Build the clip area
		Area area = new Area();
		for (int index : trueStates) {
			LightInfo light = lightMap.get(index);
			if (light != null) {
				Rectangle rect = light.getRectangle();
				if (rect != null) {
					area.add(new Area(rect));
				}
			}
		}
		return area;
	}

	private static BufferedImage loadPngImage(String imageName) {
		try {
			return Manager.getResourceAsBufferedImage(imageName);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not load PNG resource '" + imageName + "'");
		}
	}

	private static Map<Integer, LightInfo> loadLightMap(String mapName) {
		try (InputStream is = Manager.getResourceAsStream(mapName);
				InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
				BufferedReader reader = new BufferedReader(isr);) {

			return LightInfo.mapBitIndexToLightFromTsv(reader);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not load TSV resource '" + mapName + "'");
		}
	}


}
