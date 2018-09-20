package com.github.kilianB.pcg;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PcgTest {

	@Test
	void seedNotZero() {
		assertTrue(Pcg.UNIQUE_SEED.get() != 0);
	}

}
