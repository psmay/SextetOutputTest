package us.hgk.rhythm.exp.sextetoutputtest;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;

import javax.imageio.ImageIO;

public class Manager {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	public static void start(String lightSetName) {
		Display display = new Display(lightSetName);
		
		// Draw first image
		display.updateStates(Collections.<Integer>emptyList());
		
		try (InputStreamReader isr = new InputStreamReader(System.in, UTF8);
				BufferedReader r = new BufferedReader(isr)) {
			
			String line;
			
			while(( line = r.readLine() ) != null) {
				SextetBitSequence sbs;
				try {
					sbs = new SextetBitSequence(line);
					display.updateStates(sbs.trueIndices());
				}
				catch(IllegalArgumentException e) {
					System.err.print("Skipping this packet: " + e.getMessage());
				}
				
			}
		} catch (EOFException e) {
			// Exit silently
		} catch (IOException e) {
			System.err.println("An I/O error occurred: " + e.getMessage());
		}
	}
	
	static BufferedImage getResourceAsBufferedImage(String filename) throws IOException, FileNotFoundException {
		return ImageIO.read(getResourceAsStream(filename));
	}

	static InputStream getResourceAsStream(String filename) throws FileNotFoundException {
		InputStream stream = Manager.class.getClassLoader().getResourceAsStream(filename);

		if (stream == null) {
			throw new FileNotFoundException("The resource '" + filename + "' could not be found.");
		}

		return stream;
	}

}
