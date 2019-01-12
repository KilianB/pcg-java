package com.github.kilianB.pcg;

import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import com.github.kilianB.pcg.cas.PcgRRCas;
import com.github.kilianB.pcg.cas.PcgRSCas;
import com.github.kilianB.pcg.fast.PcgRSFast;
import com.github.kilianB.pcg.fast.PcgRSUFast;
import com.github.kilianB.pcg.lock.PcgRRLocked;
import com.github.kilianB.pcg.lock.PcgRSLocked;
import com.github.kilianB.pcg.sync.PcgRR;
import com.github.kilianB.pcg.sync.PcgRS;

@State(Scope.Benchmark)
public class PerformanceRNG {

	private MersenneTwister twister;
	private MersenneTwisterFast twisterFast;
	
	private Random jdkDefault;
	private SplittableRandom jdkSplittableRandom;
	
	//PCG Family
	private PcgRR pcgRR;
	private PcgRS pcgRS;
	private PcgRSFast pcgRSFast;

	//CAS
	private PcgRSCas pcgRSCas;
	private PcgRRCas pcgRRCas;
	
	private PcgRSLocked pcgRSLocked;
	private PcgRRLocked pcgRRLocked;
	
	@Setup(Level.Trial)
	public void setup() {
		twister = new MersenneTwister();
		twisterFast = new MersenneTwisterFast();
		
		jdkDefault = new Random();
		jdkSplittableRandom = new SplittableRandom();
		
		pcgRR = new PcgRR(0L, 0L);
		pcgRS = new PcgRS(0L, 0L);
		pcgRSFast = new PcgRSFast(0L, 0L);
		PcgRSUFast.seed(0L, 0L);
		
		pcgRSCas = new PcgRSCas(0L, 0L);
		pcgRRCas = new PcgRRCas(0L, 0L);

		pcgRSLocked = new PcgRSLocked(0L, 0L);
		pcgRRLocked = new PcgRRLocked(0L, 0L);
		
	}

	//MTwister
	@Benchmark
	public int nextIntMTwister() {
		return twister.nextInt();
	}
	
	@Benchmark
	public int nextIntMTwisterFast() {
		return twisterFast.nextInt();
	}
	
	//JDK
	
	@Benchmark
	public int nextIntJdkDefault() {
		return jdkDefault.nextInt();
	}
	
	@Benchmark
	public int nextIntJdkSplittableRandom() {
		return jdkSplittableRandom.nextInt();
	}
	
	//PCG
	
	@Benchmark
	public int nextIntPcgRR() {
		return pcgRR.nextInt();
	}
	
	@Benchmark
	public int nextIntPcgRRCas() {
		return pcgRRCas.nextInt();
	}

	@Benchmark
	public int nextIntPcgRRLocked() {
		return pcgRRLocked.nextInt();
	}

	@Benchmark
	public int nextIntPcgRS() {
		return pcgRS.nextInt();
	}

	@Benchmark
	public int nextIntPcgRSCas() {
		return pcgRSCas.nextInt();
	}

	@Benchmark
	public int nextIntPcgRSLocked() {
		return pcgRSLocked.nextInt();
	}

	@Benchmark
	public int nextIntPcgRSFast() {
		return pcgRSFast.nextInt();
	}

	@Benchmark
	public int nextIntPcgRSUFast() {
		return PcgRSUFast.nextInt();
	}
	
	//java -jar benchmarks.jar -tu us -w 15 -f 5 -o benchmarkSingle.txt
	//java -jar benchmarks.jar -tu us -w 15 -f 5 -t 4 -e "Fast|Splittable" -o benchmark4Cores.txt

	// 1 thread
//	Benchmark                                               Mode  Cnt    Score   Error   Units
//	kilianB.pcg.PerformanceRNG.nextIntJdkDefault           thrpt   25  104,567 ± 0,392  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntJdkSplittableRandom  thrpt   25  317,290 ± 0,383  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntMTwister             thrpt   25  127,633 ± 0,230  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntMTwisterFast         thrpt   25  163,916 ± 0,756  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRR                thrpt   25  226,486 ± 1,175  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRRCas             thrpt   25  107,424 ± 0,244  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRRLocked          thrpt   25   57,643 ± 0,352  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRS                thrpt   25  225,816 ± 1,144  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRSCas             thrpt   25  109,003 ± 0,321  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRSFast            thrpt   25  325,565 ± 1,794  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRSLocked          thrpt   25   57,539 ± 0,229  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRSUFast           thrpt   25  333,574 ± 1,698  ops/us


	
	//4 threads
//	Benchmark                                       Mode  Cnt   Score   Error   Units
//	kilianB.pcg.PerformanceRNG.nextIntJdkDefault   thrpt   25  12,469 ± 0,273  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntMTwister     thrpt   25  17,623 ± 0,904  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRR        thrpt   25  20,938 ± 0,193  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRRCas     thrpt   25   9,682 ± 0,460  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRRLocked  thrpt   25  34,036 ± 0,830  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRS        thrpt   25  20,862 ± 0,202  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRSCas     thrpt   25  11,184 ± 1,110  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRSLocked  thrpt   25  33,827 ± 1,103  ops/us

	// 8 Threads
//	Benchmark                                       Mode  Cnt   Score   Error   Units
//	kilianB.pcg.PerformanceRNG.nextIntJdkDefault   thrpt   25  12,556 ± 0,267  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntMTwister     thrpt   25  18,615 ± 0,270  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRR        thrpt   25  20,827 ± 0,452  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRRCas     thrpt   25  11,606 ± 1,290  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRRLocked  thrpt   25  34,805 ± 0,397  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRS        thrpt   25  20,710 ± 0,257  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRSCas     thrpt   25  10,931 ± 1,195  ops/us
//	kilianB.pcg.PerformanceRNG.nextIntPcgRSLocked  thrpt   25  34,518 ± 0,368  ops/us

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(PerformanceRNG.class.getSimpleName()).forks(2).mode(Mode.Throughput)
				.warmupTime(new TimeValue(13,TimeUnit.SECONDS))
				.timeUnit(TimeUnit.MICROSECONDS)
				/* output */
				//.output("benchmark.txt")
				//.resultFormat(ResultFormatType.TEXT)
				.build();
		
		//Todo output file path!
		
		new Runner(opt).run();
	}

}
