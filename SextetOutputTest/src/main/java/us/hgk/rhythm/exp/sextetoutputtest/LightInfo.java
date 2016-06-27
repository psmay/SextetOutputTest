package us.hgk.rhythm.exp.sextetoutputtest;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LightInfo {

	private final int bitIndex;
	private final String description;
	private Rectangle rect;

	public LightInfo(int bitIndex, String description, Rectangle rect) {
		this.bitIndex = bitIndex;
		this.description = description;
		this.rect = rect;
	}

	public int getBitIndex() {
		return bitIndex;
	}

	public String getDescription() {
		return description;
	}

	public Rectangle getRectangle() {
		return rect;
	}
	
	private static int parseIntIo(String s) throws IOException {
		try {
			return Integer.parseInt(s);
		}
		catch(NumberFormatException e) {
			throw new IOException("Cannot parse '" + s + "' as an integer", e);
		}
	}
	
	static Map<Integer, LightInfo> mapBitIndexToLightFromTsv(BufferedReader reader) throws IOException {
		HashMap<Integer, LightInfo> map = new HashMap<>();

		String line;
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split("\t");
			if (fields.length == 0) {
				continue;
			} else if (fields.length != 6) {
				throw new IOException(fields.length + " field(s) found instead of the expected 6");
			}

			String description;
			int bitIndex, x, y, width, height;

			bitIndex = parseIntIo(fields[0]);
			description = fields[1];
			x = parseIntIo(fields[2]);
			y = parseIntIo(fields[3]);
			width = parseIntIo(fields[4]);
			height = parseIntIo(fields[5]);

			// For now, the tsv only describes rectangles
			Rectangle rect = new Rectangle(x, y, width, height);
			
			LightInfo light = new LightInfo(bitIndex, description, rect);
			map.put(bitIndex, light);
		}
		return map;
	}
}
