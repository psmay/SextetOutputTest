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
		display.updateStates(Collections.<Integer> emptyList());

		try (InputStreamReader isr = new InputStreamReader(System.in, UTF8);
				BufferedReader r = new BufferedReader(isr)) {

			String line;

			while ((line = r.readLine()) != null) {
				SextetBitSequence sbs;
				try {
					sbs = new SextetBitSequence(line);
					if (!sbs.isNoop()) {
						display.updateStates(sbs.trueIndices());
					}
				} catch (IllegalArgumentException e) {
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
