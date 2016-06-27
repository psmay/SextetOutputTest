package us.hgk.rhythm.exp.sextetoutputtest;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
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

	private Object lockMg = new Object();
	private volatile Renderer mg;

	private JFrame frame;
	private DrawingPanel panel;

	public Display(String baseResourceName) {
		String unlitName = baseResourceName + "-unlit.png";
		String litName = baseResourceName + "-lit.png";
		String mapName = baseResourceName + "-map.tsv";

		lightMap = loadLightMap(mapName);
		litOriginal = loadPngImage(litName);
		unlitOriginal = loadPngImage(unlitName);

		Dimension2D idealDimension = Geometry.getDimension2D(unlitOriginal.getWidth(), unlitOriginal.getHeight());
		Geometry.nudgeToPositive(idealDimension);
		desiredAspect = Geometry.getAspect(idealDimension);

		Dimension2D size = Geometry.getDimension2D(400, 400);
		Geometry.setAspectWithin(size, desiredAspect);
		Geometry.nudgeToPositive(size);
		panelPreferredSize = Geometry.toPositiveAwtDimension(size);
		

		// This initializes mg to something very small;
		// it will be changed to something useful when we call onResize()
		changeScale(1, 1);

		setUpWindow();
		frame.setVisible(true);
		onResize();
	}

	private void onResize() {
		Dimension2D panelDim = Geometry.getDimension2D(panel.getWidth(), panel.getHeight());
		Dimension panelNDim = Geometry.toPositiveAwtDimension(panelDim);
		
		Dimension2D innerDim = (Dimension2D) panelDim.clone();
		Geometry.setAspectWithin(innerDim, desiredAspect);
		Dimension innerNDim = Geometry.toPositiveAwtDimension(innerDim);
		
		int offsetX = (panelNDim.width - innerNDim.width) / 2;
		int offsetY = (panelNDim.height - innerNDim.height) / 2;
		
		changeScale(innerNDim.width, innerNDim.height);
		translateTransform = AffineTransform.getTranslateInstance(offsetX, offsetY);
		
		System.err.println("Panel dim " + panelNDim.width + "x" + panelNDim.height);
		System.err.println("Inner dim " + innerNDim.width + "x" + innerNDim.width);
		System.err.println("Draw offset " + offsetX + "x" + offsetY);
		panel.repaint();
	}
	
	private void changeScale(int proposedWidth, int proposedHeight) {
		Renderer newmg = new Renderer(unlitOriginal, litOriginal, proposedWidth, proposedHeight);

		synchronized (lockMg) {
			mg = newmg;
		}
	}

	private void setUpWindow() {
		frame = new JFrame("Testing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		panel = new DrawingPanel();
		panel.addComponentListener(new ThisComponentListener());
		frame.add(panel);
		frame.pack();
	}

	void updateStates(Iterable<Integer> trueStates) {
		Area area = buildClipArea(trueStates);
		mg.renderImage(area);
		panel.repaint();
	}

	private class ThisComponentListener extends ComponentAdapter {

		@Override
		public void componentResized(ComponentEvent e) {
			onResize();
		}
		
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
			Graphics2D g2 = (Graphics2D) g.create();

			Composite comp = g2.getComposite();
			Color c = g2.getColor();

			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			g2.setColor(Color.lightGray);
			g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());

			g2.setColor(c);
			g2.setComposite(comp);

			g2.transform(translateTransform);
			mg.drawRenderedImage(g2);

			g2.dispose();
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
