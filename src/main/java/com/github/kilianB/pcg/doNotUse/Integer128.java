package com.github.kilianB.pcg.doNotUse;

/**
 * Integer supporting 128 bit operations
 * 
 * Signed or unsigned? Do we want to do more than basic synchonization on a
 * single method? Since we do compound operations using a lock might be
 * suitable. But keep performance in mind. Maybe this is the time for atomic
 * values or cas operations...
 * 
 * @author Kilian
 *
 */
public class Integer128 {

	/** Signed upper 64 bit */
	private long upperWord;
	/** Unsigned lower 64 bit*/
	private long lowerWord;

	public Integer128(long upper, long lower) {
		this.upperWord = upper;
		this.lowerWord = lower;
	}

	/*
	 * Bit arithmetic
	 */

	/**
	 * Flip all bits of the long integer
	 */
	public synchronized void flip() {
		upperWord = ~upperWord;
		lowerWord = ~lowerWord;
	}

	/**
	 * Perform an exclusive or on the two .
	 * 
	 * <pre>
	 *   | 0 1
	 * --+-----
	 * 0 | 0 1
	 * 1 | 1 0
	 * </pre>
	 * 
	 * @param i
	 */
	public synchronized void xor(Integer128 i) {
		upperWord ^= i.upperWord;
		lowerWord ^= i.lowerWord;
	}

	public synchronized void or(Integer128 i) {
		upperWord |= i.upperWord;
		lowerWord |= i.lowerWord;
	}

	public synchronized void mask(Integer128 i) {
		upperWord &= i.upperWord;
		lowerWord &= i.lowerWord;
	}

	/**
	 * The default java leftshift operation
	 * 
	 * @param shift
	 */
	public synchronized void shiftLeftRotate(int shift) {

	}

	public synchronized void shiftLeft(int shift) {

		// TODO we break with java convention here if we don't mod128 the shift

		System.out.println("Upper: " + Long.toBinaryString(upperWord));
		System.out.println("Lower: " + Long.toBinaryString(lowerWord));

		// Mask still correct with greater than 64 bit shifts?
		long mask = lowerWord >>> (64 - shift);

		System.out.println("Mask:  " + Long.toBinaryString(mask));

		if (shift >= 64) {
			// Java shifts are mod 64 for longs and reenter ...
			upperWord = 0;
			lowerWord = 0;
		} else {
			upperWord <<= shift;
			lowerWord <<= shift;
		}

		System.out.println("Upper: " + Long.toBinaryString(upperWord) + " Before Mask");
		upperWord |= mask;

		System.out.println("Upper: " + Long.toBinaryString(upperWord));
		System.out.println("Lower: " + Long.toBinaryString(lowerWord));
	}

	public synchronized void shiftRight(int shift) {

	}

	public synchronized void shiftRightUnsiged(int shift) {
		//Long.compareUnsigned(x, y)
	}

	/*
	 * Integer Arithmetic 
	 */
	public synchronized void add(Integer128 summand) {

		//care about carry bit. 
		
	}
	
	

	public synchronized void add(long summand) {
		// Allows implicit widening cast
		
		
	}

	public synchronized void subtract(Integer128 subtrahend) {

	}

	public synchronized void subtract(long subtrahend) {

	}

	public synchronized void multiply(Integer128 subtrahend) {

	}

	public synchronized void multiply(long subtrahend) {

	}

	public synchronized void divide(Integer128 subtrahend) {

	}

	public synchronized void divide(long subtrahend) {

	}

	/*
	 * Utility functions
	 */

	public int compareTo(Integer128 i) {
		return 0;
	}

	public int compareToUnsigned(Integer128 i) {
		return 0;
	}

	public String toBinaryString() {
		return Long.toBinaryString(upperWord) + Long.toBinaryString(lowerWord);
	}

	public static void main(String[] args) {

		var longInt = new Integer128(115550L, Long.MAX_VALUE + 1);
		System.out.println(longInt.toBinaryString());

		longInt.shiftLeft(64);

	}
	
	public String toString() {
		//new BigInteger()
		return "";
	}

}
