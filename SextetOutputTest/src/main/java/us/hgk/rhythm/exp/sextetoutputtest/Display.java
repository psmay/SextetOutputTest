package us.hgk.rhythm.exp.sextetoutputtest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
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
	private final BufferedImage lit, unlit, offscreen;
	
	private JFrame frame;
	private TestPane testPane;
	
	public Display(String baseResourceName) {
		String unlitName = baseResourceName + "-unlit.png";
		String litName = baseResourceName + "-lit.png";
		String mapName = baseResourceName + "-map.tsv";
		
		lightMap = loadLightMap(mapName);
		lit = loadPngImage(litName);
		unlit = loadPngImage(unlitName);
		offscreen = new BufferedImage(unlit.getWidth(), unlit.getHeight(), unlit.getType());	
		
		setUpWindow();
		frame.setVisible(true);
	}
	
	private void setUpWindow() {
		frame = new JFrame("Testing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		testPane = new TestPane();
		frame.add(testPane);
		frame.pack();
	}
	
	void updateStates(Iterable<Integer> trueStates) {
		testPane.updateOffscreenImage(trueStates);
	}
	
	private class TestPane extends JPanel {
		private static final long serialVersionUID = -4814671716253111614L;

		public TestPane() {
		}

		@Override
		public Dimension getPreferredSize() {
			int bw = offscreen.getWidth();
			int bh = offscreen.getHeight();

			// dw/dh = bw/bh
			// dw = bw*dh/bh
			int dh = 400;
			int dw = (bw * dh) / bh;

			return new Dimension(dw, dh);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();
			
			int h = this.getHeight();
			int w = this.getWidth();

			g2d.drawImage(offscreen, 0, 0, w, h, this);

			g2d.dispose();
		}

		void updateOffscreenImage(Iterable<Integer> trueStates) {
			Area area = buildClipArea(trueStates);
			
			setIgnoreRepaint(true);
			
			Graphics2D g2 = offscreen.createGraphics();
			
			// Clear image
			g2.setColor(new Color(0, true));
			g2.fillRect(0, 0, offscreen.getWidth(), offscreen.getHeight());
			
			// Draw unlit, set clip area, draw lit, restore clip area
			g2.drawImage(unlit, 0, 0, this);
			Shape normalClip = g2.getClip();
			g2.setClip(area);
			g2.drawImage(lit, 0, 0, this);
			g2.setClip(normalClip);
			
			g2.dispose();
			
			setIgnoreRepaint(false);
			repaint();
		}

		private Area buildClipArea(Iterable<Integer> trueStates) {
			// Build the clip area
			Area area = new Area();
			for(int index : trueStates) {
				LightInfo light = lightMap.get(index);
				if(light != null) {
					Shape shape = light.getClipShape();
					if(shape != null) {
						area.add(new Area(shape));
					}
				}
			}
			return area;
		}	
	}

	private static BufferedImage loadPngImage(String imageName) {
		try {
			return Manager.getResourceAsBufferedImage(imageName);
		}
		catch(IOException e) {
			throw new IllegalArgumentException("Could not load PNG resource '" + imageName + "'");
		}
	}

	private static Map<Integer, LightInfo> loadLightMap(String mapName) {
		try(InputStream is = Manager.getResourceAsStream(mapName);
				InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
				BufferedReader reader = new BufferedReader(isr);
				) {
			
			return LightInfo.mapBitIndexToLightFromTsv(reader);
		}
		catch(IOException e) {
			throw new IllegalArgumentException("Could not load TSV resource '" + mapName + "'");
		}
	}
}
