package us.hgk.rhythm.exp.sextetoutputtest;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SextetBitSequence extends AbstractList<Boolean> {

	private String sextetData;
	
	public SextetBitSequence(String sextetData) {
		if(sextetData == null) {
			throw new NullPointerException();
		}
		if(sextetData.matches("[\\x0A\\x0D]")) {
			throw new IllegalArgumentException("Sextet data must not contain any newline characters");
		}
		if(sextetData.matches("[^\\x30-\\x6F]")) {
			throw new IllegalArgumentException("Sextet data must not contain characters outside the range 0x30..0x6F");
		}
		this.sextetData = sextetData;
	}
	
	private int getSextet(int sextetIndex) {
		if(sextetIndex >= 0 && sextetIndex < sextetData.length()) {
			return (int) sextetData.charAt(sextetIndex);
		}
		throw new IndexOutOfBoundsException();
	}
	
	private int getSextetAtBit(int bitIndex) {
		if(bitIndex >= 0) {
			return getSextet(bitIndex / 6);
		}
		throw new IndexOutOfBoundsException();
	}
	
	private int getSextetMask(int bitIndex) {
		int subIndex = bitIndex % 6;
		return (1 << subIndex);
	}
	
	
	@Override
	public Boolean get(int index) {
		return getBoolean(index);
	}
	
	@Override
	public int size() {
		return sextetData.length() * 6;
	}
	
	public boolean getBoolean(int index) {
		int sextet = getSextetAtBit(index);
		int mask = getSextetMask(index);
		return (sextet & mask) != 0;
	}
	
	public Iterable<Integer> trueIndices() {
		return new TrueIndexIterable(sextetData);
	}
	
	private static class TrueIndexIterable implements Iterable<Integer> {
		private final String sextetData;
		
		public TrueIndexIterable(String sextetData) {
			if(sextetData == null) {
				throw new NullPointerException();
			}
		this.sextetData = sextetData;
		}

		@Override
		public Iterator<Integer> iterator() {
			return new TrueIndexIterator(sextetData);
		}
		
	}
	private static class TrueIndexIterator implements Iterator<Integer> {

		private final String sextetData;
		private final int sextetLength;

		private boolean advancePastCurrent = false;
		
		TrueIndexIterator(String sextetData) {
			this.sextetData = sextetData;
			this.sextetLength = sextetData.length();
		}
		
		private int sextetIndex = 0;
		private int subIndex = 0;
		private int bitIndex = 0;
		
		private boolean seek(boolean skip) {
			// Implemented as a continuable double for loop.
			// If skip is false, stops and returns true for the first bit at or later than the current bit where sextet data is true.
			// Repeated calls to seek(false) are no-ops; they repeatedly find a bit at the current location (or no bit at all).
			// If skip is true, seeks the same bit as seek(false), then skips it and advances to the next.
			for(; sextetIndex < sextetLength; ++sextetIndex, subIndex = 0) {
				int sextet = sextetData.charAt(sextetIndex);
				
				if((sextet & 0x3F) == 0) {
					// All-zero sextet
					bitIndex += 6;
					continue;
				}
				
				for(; subIndex < 6; ++subIndex, ++bitIndex) {
					if((sextet & (1 << subIndex)) != 0) {
						if(skip) {
							// Skip this, but not another
							skip = false;
						}
						else {
							return true;
						}
					}
				}
			}
			return false;
		}
		
		@Override
		public boolean hasNext() {
			boolean found = seek(advancePastCurrent);
			advancePastCurrent = false;
			return found;
		}

		@Override
		public Integer next() {
			return nextInt();
		}
		
		public int nextInt() {
			if(hasNext()) {
				int value = bitIndex;
				advancePastCurrent = true;
				return value;
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
}
