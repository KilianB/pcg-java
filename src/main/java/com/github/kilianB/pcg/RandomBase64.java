package com.github.kilianB.pcg;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

/**
 * Base class for 64 bit state pcg random number generators with 32 bit output.
 * 
 * The PCG family uses a linear congruential generator as the state-transition
 * function—the “CG” of PCG stands for “congruential generator”. Linear
 * congruential generators are known to be statistically weak.
 * <p>
 * 
 * PCG uses a new technique called permutation functions on tuples to produce
 * output that is much more random than the RNG's internal state. The output
 * function is defined by the extending classes.
 * <p>
 * 
 * A paper highlighting the individual properties can be found here. <a href=
 * "http://www.pcg-random.org/paper.html">http://www.pcg-random.org/paper.html</a>.
 * This class is an adaption to the original c
 * <a href="https://github.com/imneme/pcg-c">source code</a> provided by M.E.
 * O'Neill.
 * <p>
 *
 * <b>Contract:</b> every extending class <b>must</b> implement a copy
 * constructor with a signature of(long,long,boolean). As it does not perform
 * proper initialization of the seed this method should not be exposed.
 * 
 * @author Kilian
 *
 * @see <a href="http://www.pcg-random.org/">www.pcg-random.org</a>
 */
public abstract class RandomBase64 extends Random implements Pcg {

	private static final long serialVersionUID = -4396858403047759432L;

	/**
	 * Linear congruential constant. Same as MMIX by Donald Knuth and Newlib, Musl
	 */
	protected static final long MULT_64 = 6364136223846793005L;

	/**
	 * Seeds the generator with 2 longs generated by xorshift*. The values choosen
	 * are very likely not used in any other invocation of this constructor.
	 */
	public RandomBase64() {
		this(getRandomSeed(), getRandomSeed());
	}

	/**
	 * Create a random number generator with the given seed and stream number. The
	 * seed defines the current state in which the rng is in and corresponds to
	 * seeds usually found in other RNG instances. RNGs with different seeds are
	 * able to catch up after they exhaust their period and produce the same
	 * numbers. (2^63).
	 * <p>
	 * 
	 * Different stream numbers alter the increment of the rng and ensure distinct
	 * state sequences
	 * <p>
	 * 
	 * Only generators with the same seed AND stream numbers will produce identical
	 * values
	 * <p>
	 * 
	 * @param seed         used to compute the starting state of the RNG
	 * @param streamNumber used to compute the increment for the lcg.
	 */
	public RandomBase64(long seed, long streamNumber) {
		setSeed(seed, streamNumber);
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
	 * @param initialState of the lcg. The value will be set and not altered.
	 * @param increment    used in the lcg. has to be odd
	 * @param dummy        unused. Resolve signature disambiguate
	 */
	@Deprecated
	protected RandomBase64(long initialState, long increment, boolean dummy) {

		if (increment == 0) {
			throw new IllegalArgumentException("The increment can't be 0");
		}

		if (increment % 2 == 0) {
			throw new IllegalArgumentException("Increment has to be odd");
		}

		// Use getters and setters to let fast implementation overwrite this behavior
		// while still maintaining inheritance.
		setState(initialState);
		setInc(increment);
	}

	/**
	 * Sets the seed of this random number generator using a single long seed. The
	 * general contract of setSeed is that it alters the state of this random number
	 * generator object so as to be in exactly the same state as if it had just been
	 * created with the argument seed as a seed.
	 * <p>
	 * Be aware that seeding this random number generation requires 2 arguments. A
	 * seed and a stream number.
	 * <p>
	 * Calling this method is equivalent to {@link #RandomBase64(long, long)};
	 * Confusion may arise when constructing a rng instance with a given seed and a
	 * different stream number and expecting the rngs to be in the same state as
	 * right after the constructor call.
	 * <p>
	 * In this case please refer to the method {@link #setSeed(long, long)};
	 * 
	 * @deprecated This method behaves differently than you would expect from the
	 *             random base class.
	 * @since 1.0.1
	 */
	@Override
	public void setSeed(long seed) {
		setSeed(seed, seed);
	}

	/**
	 * Sets the seed of this random number generator using . The general contract of
	 * setSeed is that it alters the state of this random number generator object so
	 * as to be in exactly the same state as if it had just been created with the
	 * argument seed as a seed.
	 * 
	 * Only generators with the same seed AND stream numbers will produce identical
	 * values
	 * <p>
	 * 
	 * @param seed         used to compute the starting state of the RNG
	 * @param streamNumber used to compute the increment for the lcg.
	 */
	public void setSeed(long seed, long streamNumber) {
		setState(0);
		/*
		 * We need to ensure that subclasses can override and add synchronization as
		 * they please. use getters and setters.
		 * 
		 * Although there are rules for the choice of constants [17], if we pick a
		 * power-of-two modulus and a good multiplicative constant, the only constraint
		 * on c for a full period generator is that c is odd and > 0
		 * 
		 * Chapter 4.2.1 (http://www.pcg-random.org/pdf/hmc-cs-2014-0905.pdf)
		 */
		setInc((streamNumber << 1) | 1); // 2* + 1
		stepRight();
		setState(getState() + seed);
		stepRight();
	}

	/**
	 * Update the state of the lcg and move a step forward. The old state should be
	 * used to extract bits used to construct a number.
	 * <p>
	 * 
	 * When implementations use the newly generate state to calculate random numbers
	 * {@link #isFast()} has to return true.
	 * 
	 * @return the old value of the state variable before updating.
	 */
	protected abstract long stepRight();

	@Override
	@SuppressWarnings("unchecked")
	public <T> T split() throws ReflectiveOperationException {
		try {
			return (T) getClass().getDeclaredConstructor(long.class, long.class, boolean.class).newInstance(getState(),
					getInc(), true);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new ReflectiveOperationException("Failed to instantiate clone constructor");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T splitDistinct() throws ReflectiveOperationException {
		try {
			long curInc;
			long curState;

			// No reason to CAS here. we don't swap the inc around all the time
			do {
				// Has to be odd
				curInc = ((nextLong(Math.abs(getInc())) ^ (~System.nanoTime())) * 2) + 1;
			} while (curInc == getInc());

			// State swaps by each call to nextLong
			do {
				curState = (nextLong(Math.abs(getState())) ^ (~System.nanoTime()));
			} while (curState == getState());

			return (T) getClass().getDeclaredConstructor(long.class, long.class, boolean.class).newInstance(curState,
					curInc, true);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			e.getCause().printStackTrace();
			throw new ReflectiveOperationException("Failed to instantiate clone constructor");
		}
	}

	@Override
	public int next(int n) {
		int nInt = nextInt();
		int shifted = (nInt >>> (32 - n));
		return shifted;
	}

	/**
	 * Construct a 32bit int from the given 64bit state using a permutation
	 * function. The produced int will be used to construct all other datatypes
	 * returned by this RNG.
	 * 
	 * @param state random int as produced by the internal lcg
	 * @return a random int with randomly set bits
	 * 
	 */
	protected abstract int getInt(long state);

	// Non standard random functions. add missing convinience methods

	@Override
	public boolean nextBoolean(double probability) {
		if (probability < 0.0 || probability > 1.0)
			throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
		if (probability == 0.0)
			return false; // fix half-open issues
		else if (probability == 1.0)
			return true; // fix half-open issues
		return nextDouble() < probability;
	}

	@Override
	public byte nextByte() {
		return (byte) (next(8));
	}

	@Override
	public void nextBytes(byte[] bytes) {
		// According to merseene twister
		/*
		 * A bug fix for all versions of the JDK. The JDK appears to use all four bytes
		 * in an integer as independent byte values! Totally wrong. I've submitted a bug
		 * report.
		 */
		for (int x = 0; x < bytes.length; x++)
			bytes[x] = (byte) next(8);
	}

	@Override
	public char nextChar() {
		return (char) (next(16));
	}

	@Override
	public short nextShort() {
		return (short) (next(16));
	}

	@Override
	public int nextInt() {
		return getInt(stepRight());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The long value is composed of two integers and a 64 bit state therefore,
	 * returning all possible long values with equal probability.
	 * 
	 */
	public long nextLong() {
		// Don't override just update javadocs
		return super.nextLong();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The long value is composed of two integers and a 64 bit state therefore,
	 * returning all possible long values with equal probability.
	 */
	@Override
	public long nextLong(long n) {
		if (n <= 0)
			throw new IllegalArgumentException("n must be positive, got: " + n);
		long bits;
		long val;
		do {
			bits = (nextLong() >>> 1);
			val = bits % n;
		} while (bits - val + (n - 1) < 0);
		return val;
	}

	@Override
	public float nextFloat(boolean includeZero, boolean includeOne) {
		float d = 0.0f;
		do {
			d = nextFloat(); // grab a value, initially from half-open [0.0f, 1.0f)
			if (includeOne && nextBoolean())
				d += 1.0f; // if includeOne, with 1/2 probability, push to [1.0f, 2.0f)
		} while ((d > 1.0f) || // everything above 1.0f is always invalid
				(!includeZero && d == 0.0f)); // if we're not including zero, 0.0f is invalid
		return d;
	}

	@Override
	public double nextDouble(boolean includeZero, boolean includeOne) {
		double d = 0.0;
		do {
			d = nextDouble(); // grab a value, initially from half-open [0.0, 1.0)
			if (includeOne && nextBoolean())
				d += 1.0; // if includeOne, with 1/2 probability, push to [1.0, 2.0)
		} while ((d > 1.0) || // everything above 1.0 is always invalid
				(!includeZero && d == 0.0)); // if we're not including zero, 0.0 is invalid
		return d;
	}

	/*
	 * Protected static isn't really "clean" but roll with it. No reason to make it
	 * public and define it as a default method in the interface as it is defined
	 * only for the 64 bit state. We want to have control over every instance
	 * therefor take the static path. The fast instance needs access to it. Else we
	 * could also provide package visbility or use reflection
	 */
	/**
	 * Return a random 64 bit seed ensuring uniqueness by using a xorshift64* shift
	 * algorithm. This implementation is thread safe.
	 * 
	 * @return a unique seed
	 */
	protected static long getRandomSeed() {
		// xorshift64*
		for (;;) {
			long current = UNIQUE_SEED.get();
			long next = current;
			next ^= next >> 12;
			next ^= next << 25; // b
			next ^= next >> 27; // c
			next *= 0x2545F4914F6CDD1DL;
			if (UNIQUE_SEED.compareAndSet(current, next))
				return next;
		}
	}

	// Below add some support for fast instances which almost always do not rely on
	// the defined methods in this class but still extend it to allow for
	// polymorphism.

	@Override
	public long getMult() {
		return MULT_64;
	}

	/**
	 * Set the internal state of the pcg. This method is used during the seeding
	 * process of this class and therefore, it is most likely is never correct to
	 * alter the variable passed to this function.
	 * <p>
	 * Allowed operations are synchronization on those methods-
	 * <p>
	 * 
	 * @param newState of the pcg
	 */
	protected abstract void setState(long newState);

	/**
	 * Set the increment of the pcg. This method is used during the seeding process
	 * of this class and therefore, it is most likely is never correct to alter the
	 * variable passed to this function.
	 * <p>
	 * "Although there are rules for the choice of constants [17], if we pick a
	 * power-of-two modulus and a good multiplicative constant, the only constraint
	 * on c for a full period generator is that c is odd and {@literal>} 0"
	 * <p>
	 * Chapter 4.2.1 (http://www.pcg-random.org/pdf/hmc-cs-2014-0905.pdf)
	 * <p>
	 * Allowed operations are synchronization on those methods-
	 * 
	 * @param newInc of the pcg
	 */
	protected abstract void setInc(long newInc);
}
