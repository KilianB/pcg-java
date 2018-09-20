package com.github.kilianB.pcg.lock;

/**
 * Extend the random class in order to make this rng accessible for
 * Collection.sort
 * 
 * @author Kilian
 *
 */
public class PcgRSLocked extends RandomBaseLocked {

	public PcgRSLocked() {
		super();
	}
	
	public PcgRSLocked(long seed, long streamNumber) {
		super(seed, streamNumber);
	}
	
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
	@Deprecated
	public PcgRSLocked(long seed, long streamNumber, boolean dummy) {
		super(seed, streamNumber, true);
	}

	@Override
	protected int getInt(long state) {
		return (int) (((state >>> 22) ^ state) >>> ((state >>> 61) + 22));
	}

	
//	protected int rotateRightUnsigned(int value, int rot) {
//		 return (value >>> rot) | (value << ((- rot) & 31));
//	}	

	
}
