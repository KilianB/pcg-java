package com.github.kilianB.pcg.cas;

import java.util.Random;

import org.junit.jupiter.api.Nested;

import com.github.kilianB.pcg.Pcg;
import com.github.kilianB.pcg.PcgBaseTest;
import com.github.kilianB.pcg.RandomBurdenTest;

/**
 * JUnit tests are only used to test methods like distance advance/skip/rewind
 * states and other ordinary functionality. It is not designed to test
 * distributions or statistical properties!
 * <p>
 * 
 * Statistical properties are tested with PractRand evaluation
 * <p>
 * Performance are checked by jmh
 * 
 * @author Kilian
 *
 */
class PcgRSCasTest extends PcgBaseTest{

	@Override
	public Pcg getInstance() {
		return new PcgRSCas();
	}

	@Override
	public Pcg getInstance(long seed, long streamNumber) {
		return new PcgRSCas(seed, streamNumber);
	}

	@Override
	public boolean isFast() {
		return false;
	}
	
	@Nested
	class RandomBurden extends RandomBurdenTest{
		@Override
		protected Random getInstance() {
			return new PcgRSCas();
		}
	}
}
