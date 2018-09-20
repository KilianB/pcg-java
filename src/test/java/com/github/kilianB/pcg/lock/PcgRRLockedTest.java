package com.github.kilianB.pcg.lock;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;

import com.github.kilianB.pcg.IncompatibleGeneratorException;
import com.github.kilianB.pcg.Pcg;
import com.github.kilianB.pcg.sync.PcgRS;

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
class PcgRRLockedTest {

	@Nested
	class State {
		
		void equalGenerationSeed() {
			Pcg rng0 = new PcgRRLocked(0, 0);
			Pcg rng1 = new PcgRRLocked(0, 0);

			byte[] values = new byte[50];
			byte[] values1 = new byte[50];

			rng0.nextBytes(values);
			rng1.nextBytes(values1);

			assertArrayEquals(values, values1);
		}
		
		@Test
		void unequelGenerationSeed() {
			
			Pcg rng0 = new PcgRRLocked();
			Pcg rng1 = new PcgRRLocked();
			
			byte[] values = new byte[50];
			byte[] values1 = new byte[50];
			
			rng0.nextBytes(values);
			rng1.nextBytes(values1);
			
			//the generators produce distinct output
			assertFalse(Arrays.equals(values, values1));
			
		}
		
		@Test
		void splitted() {
			try {
				
				Pcg rng = new PcgRRLocked();
				Pcg clone = rng.split();
			
				//Make sure that they don't share the same state
				byte[] values = new byte[50];
				byte[] values1 = new byte[50];
				
				rng.nextBytes(values);
				clone.nextBytes(values1);
				
				assertArrayEquals(values, values1);
			
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
		
		@Test
		void splittedDistinct() {
			try {
				Pcg rng = new PcgRRLocked();
				Pcg clone = rng.splitDistinct();
				
				//Make sure that they don't share the same state
				byte[] values = new byte[50];
				byte[] values0 = new byte[50];
				byte[] values1 = new byte[50];
				
				rng.nextBytes(values);
				clone.nextBytes(values1);
				
				rng.advance(-50);
				rng.nextBytes(values0);
				
				//first condition the generators don't impact each other
				assertArrayEquals(values, values0);
				//second condition the generators produce distinct output
				assertFalse(Arrays.equals(values, values1));
			
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
		
		@Test
		@Disabled
		void setIncEven() {
			assertThrows(IllegalArgumentException.class, ()->{
				//Both are private or protected
				//new PcgRSFast(4,4,false);
			});
		}
	}

	/**
	 * Rngs support to skip x numbers and fast skip or rewind it's state
	 * 
	 * @author Kilian
	 *
	 */
	@Nested
	class Step {

		@Test
		void skip() {

			Pcg rng = new PcgRRLocked(0, 0);
			
			for (int i = 0; i < 1000; i++) {
				rng.nextInt();
			}

			// Generate 1000 ints;
			int baseInt = rng.nextInt();

			// Re seed
			rng = new PcgRRLocked(0, 0);
			// Fast skip 1000
			rng.advance(1000);

			int skipInt = rng.nextInt();

			assertEquals(baseInt, skipInt);
		}

		@Test
		void rewind() {
			Pcg rng = new PcgRRLocked(0, 0);
			
			int[] generatedValues = new int[10];
			int[] generatedValues1 = new int[10];
			for (int i = 0; i < 10; i++) {
				generatedValues[i] = rng.nextInt();
			}

			// Rewind
			rng.advance(-10);
			for (int i = 0; i < 10; i++) {
				generatedValues1[i] = rng.nextInt();
			}
			assertArrayEquals(generatedValues, generatedValues1);
		}

	}

	@Nested
	class Distance {

		Pcg rng;
		Pcg rng0;
		
		@BeforeEach
		void reSeed() {

			// Seed
			long seed = System.nanoTime();
			long streamNumber = 0;

			// Seed both instances
			rng = new PcgRRLocked(seed, streamNumber);
			rng0 = new PcgRRLocked(seed, streamNumber);
		}

		@Test
		void identity() {
			assertEquals(0, rng.distance(rng));
		}

		@Test
		void positiveDistance() {
			rng0.advance(1000);
			assertEquals(1000, rng.distance(rng0));
		}

		@Test
		void negativeDistance() {
			rng0.advance(-1000);
			assertEquals(-1000, rng.distance(rng0));
		}

		@Test
		void equalDistance() {
			assertEquals(0, rng.distanceUnsafe(rng0));
		}

		@Test
		void incompatibleGeneratosClass() {
			assertThrows(IncompatibleGeneratorException.class,()->{
				rng.distance(new PcgRS());
			});
		}
		
		@SuppressWarnings("deprecation")
		@Test
		void incompatibleGeneratosIncrement() {
			assertThrows(IllegalArgumentException.class,()->{
				rng.distance(new PcgRS(rng.getState(),rng.getInc()+1,false));
			});
		}

	}

	/**
	 * Test if the bounded function return the expected values. Rough tests
	 * 
	 * @author Kilian
	 *
	 */
	@Nested
	class Bounds {


		Pcg rng;
		
		@BeforeEach
		void seed() {
			rng = new PcgRRLocked();
		}
		

		@Test
		void boolProbabilityAlwaysTrue() {
			for(int i = 0; i < 500; i++) {
				assertTrue(rng.nextBoolean(1d));
			}
		}

		@Test
		void boolProbabilityAlwaysFalse() {
			for(int i = 0; i < 500; i++) {
				assertFalse(rng.nextBoolean(0));
			}
		}

		/*
		 * This is just a very very rough test..
		 * Not sure if it even should be included
		 */
		@Test
		void booleanProbability() {

			int trueC = 0;
			double probability = 0.3;

			double acceptedDelta = 0.01;

			int reps = 50000;

			for (int i = 0; i < reps; i++) {
				if (rng.nextBoolean(probability)) {
					trueC++;
				}
			}
			double expected = (reps * probability);
			assertEquals(expected, trueC, reps * acceptedDelta);
		}

		@Test
		void intBoundPow2() {
			int upperBound = 4;
			for (int i = 0; i < 10000; i++) {
				int genInt = rng.nextInt(upperBound);
				if (genInt < 0 || genInt >= upperBound) {
					fail();
				}
			}
		}
		
		@Test
		void intBound() {
			int upperBound = 141;
			for (int i = 0; i < 10000; i++) {
				int genInt = rng.nextInt(upperBound);
				if (genInt < 0 || genInt >= upperBound) {
					fail();
				}
			}
		}
		
		@Test
		void longBound() {
			long upperBound = 4;
			for (int i = 0; i < 10000; i++) {
				long genLong = rng.nextLong(upperBound);
				if (genLong < 0 || genLong >= upperBound) {
					fail();
				}
			}
		}

	}
	
	@Test
	void nonFast() {
		Pcg rng = new PcgRRLocked();
		assertFalse(rng.isFast());
	}


}
