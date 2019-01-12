package com.github.kilianB.pcg.fast;

import com.github.kilianB.pcg.Pcg;
import com.github.kilianB.pcg.PcgBaseTest;

/**
 * JUnit tests are only used to test methods like distance advance/skip/rewind
 * states and other ordinary functionality. It is not designed to test
 * distributions or statistical properties! <p>
 * 
 * Statistical properties are tested with PractRand evaluation <p> Performance
 * are checked by jmh
 * 
 * @author Kilian
 *
 */
class PcgRSFastTest extends PcgBaseTest{

	@Override
	public Pcg getInstance() {
		return new PcgRSFast();
	}

	@Override
	public Pcg getInstance(long seed, long streamNumber) {
		return new PcgRSFast(seed, streamNumber);
	}

	@Override
	public boolean isFast() {
		return true;
	}

}
