package com.github.kilianB.pcg.lock;

/**
 * A 64 bit State PcgRNG with 32 bit output. PCG-XSH-RR <p>
 * 
 * The pcg family combines a linear congruential generators with a permutation
 * output function resulting in high quality pseudo random numbers. <p>
 * 
 * The original concept was introduced by Melissa O’Neill please refer to <a
 * href="http://www.pcg-random.org/">pcg-random</a> for more information. <p>
 * 
 * The RR instance permutates the output using the following function:
 * 
 * <pre>
 * int shift = (int) (((state >>> 18) ^ state) >>> 27);
 * int rotation = (int) (state >>> 59);
 * return Integer.rotateRight(shift, rotation);
 * </pre>
 * 
 * Regarding the RR Instance:
 * 
 * "[...The] design goal is to be a good all-purpose random number generator.
 * The intent is to balance speed with statistical performance and reasonable
 * security, charting a middle-of-the-road path. (It’s the generator that I
 * recommend for most users.) The strategy is to perform an xorshift to improve
 * the high bits, then randomly rotate them so that all bits are full period.
 * Hence the mnemonic PCG-XSH-RR, “xorshift high (bits), random rotation”."
 * 
 * This implementation is thread safe utilizing read write locks. During non contested
 * periods this version performs considerably slower and only gains the upper hand
 * if the synchronized parts are hammered consistently. 
 * 
 * @author Kilian
 * @see pcg-random.com
 * @see #PcgRRLocked
 */
public class PcgRRLocked extends RandomBaseLocked {

	public PcgRRLocked() {
		super();
	}
	
	public PcgRRLocked(long seed, long streamNumber) {
		super(seed, streamNumber);
	}
	
	@Deprecated
	/**
	 * Copy constructor. <b>Has</b> to be implemented in all inheriting instances.
	 * This will be invoked through reflection! when calling {@link #split()} or
	 * {@link #splitDistinct()} If no special behavior is desired simply pass though
	 * the values.
	 * 
	 * This constructor should usually not be called manually as the seed and
	 * increment will just be set without performing any randomization.
	 * 
	 * @param initialState
	 *            of the lcg. The value will be set and not altered.
	 * @param increment
	 *            used in the lcg. has to be odd
	 * @param dummy
	 *            unused. Resolve signature disambiguate
	 */
	public PcgRRLocked(long seed, long streamNumber, boolean dummy) {
		super(seed, streamNumber, true);
	}

	@Override
	protected int getInt(long state) {
		//Permuted output function
		int shift = (int) (((state >>> 18) ^ state) >>> 27);
		int rotation = (int) (state >>> 59);
		return Integer.rotateRight(shift, rotation);
	}

	
//	protected int rotateRightUnsigned(int value, int rot) {
//		 return (value >>> rot) | (value << ((- rot) & 31));
//	}	

	
}
