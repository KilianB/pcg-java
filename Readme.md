# PCG Random Number Generation, Java Edition


<a href="https://travis-ci.org/KilianB/pcg-java"><img src="https://travis-ci.org/KilianB/pcg-java.svg?branch=master"/></a>
<a href="https://github.com/KilianB/pcg-java/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT%20License-blue.svg"/></a>
[![Codecov](https://img.shields.io/codecov/c/github/codecov/example-python.svg)](https://codecov.io/gh/KilianB/pcg-java)
<a href="https://bintray.com/kilianb/maven/pcg-java-10/_latestVersion"><img src="https://api.bintray.com/packages/kilianb/maven/pcg-java-10/images/download.svg"/></a>



> PCG is a family of simple fast space-efficient statistically good algorithms for random number generation. Unlike many general-purpose RNGs, they are also hard to predict.


This implementation is based on Melissa E. O'Neill <a href="https://github.com/imneme/pcg-c">C code</a> and <a href="http://www.pcg-random.org/paper.html">paper</a> <a href="http://www.pcg-random.org/pdf/hmc-cs-2014-0905.pdf">PCG: A Family of Simple Fast
Space-Efficient Statistically Good Algorithms for Random Number Generation</a>. 

You are highly advised to check out her website for further information: <a href="http://www.pcg-random.org">www.pcg-random.org</a>
Java implementation featuring a 64 bit state RNG with 32 bit output.

<p align="center">
	<img src ="https://user-images.githubusercontent.com/9025925/45636036-e2f75e80-baa6-11e8-9db8-f43c2c709943.png">
</p>
Table taken from <a href="http://www.pcg-random.org/">http://www.pcg-random.org/</a>.


## Features

<ul>
	<li>High quality number generator</li>
	<li>Thread safe</li>
	<li>Quick advance and rewind allowing to reset the generator to a previous state or to skip the next k numbers</li>
	<li>Calculate the distance between different generator instances</li>
	<li>Extends the random interface to conveniently uses methods like <code>Collection.sort</code></li>
	<li>Allows to split instances like <code>SplittableRandom</code></li>
</ul>

Note: The PCG family is <b>not</b>  cryptographically secure! 

## Usage 

1. Either download the <a href="build">pre build jar files</a> and add them to your build path manually
2. Use Maven, gradle or ivy via jcenter and <a href="https://bintray.com/kilianb/maven/pcg-java-10">bintray</a> 


````
<repositories>
  <repository>
    <id>jcenter</id>
    <url>https://jcenter.bintray.com/</url>
  </repository>
</repositories>

<!-- Java 10 Version -->
<dependency>
  <groupId>com.github.kilianB</groupId>
  <artifactId>pcg-java-10</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>

<!-- Java 8 Version -->
<dependency>
  <groupId>com.github.kilianB</groupId>
  <artifactId>pcg-java-8</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>

````



Several different random number generators are bundled in this package. Pick one which suits your needs

````Java

// Uniquely seed
PcgRR rng = new PcgRR();

int randomInt = rng.nextInt();
int randomIntInRange = rng.nextInt(0,10);
double nextGaus = rng.nextGaus();

````

<details>
  
  <summary>Exand additional features and examples.</summary>
  
````Java
/* Next to randomly setting the internal state (seeding) the pcg family also allows to select one of 
many streams which all follow a different random number sequence ensuring that internal stages 
never  converge between instance. */
long seed = 0;
long streamNumber = 0;
PcgRR rng = new PcgRR(seed,streamNumber);

/* This istance will produce the same output when generating a number but they don't share a
reference*/
PcgRR rngClone = rng.split();

rng.nextInt() == rngClone.nextInt() // TRUE

/* This instance does not share a state and has a different seed and stream number!*/
RngRR distinctRng = rng.splitDistinct();

//Distance
rngClone = rng.split();

for(int i = 0; i < 100; i++;){
    rng.nextInt();
}
rngClone.distance(rng)  // 100

//Rewind or fast skip x numbers
int randomInt = rng.nextInt()
rng.advance(-1) // rewind 1 step 
randomInt == rng.Next() // TRUE
````

</details>
<br>


### Choose the correct generator 
<table>
	<tr>
		<td>Generator</td>
		<td>Description</td>
		<td>Thread safety</td>
		<td>Notes</td>
	</tr>
	<tr>	
		<td>PcgRR</td>
		<td>Balance speed with statistical performance and reasonable security.
		See chapter 6.3.1 in the paper. </td>
		<td>Yes synchronization*</td>
		<td>Recommended for multi threaded use</td>
	</tr>
	<tr>
		<td>PcgRS</td>
		<td>Instead of rotating the bits perform an implied shift.
			See chapter 6.3.2 in the paper.
			Slightly worse statistical performance**
		</td>
		<td>Yes synchronization*</td>
		<td>This generator is supposed to be faster due to performing a shift instead of rotating bits. But in benchmarks it performed slightly worse than the RR version. No real reason to pick this one over the RR at the moment.</td>
	</tr>
	<tr>
		<td>PcgRSFast</td>
		<td>
<ul>
<li>Identical output to the PcgRS but drops synchronization</li>
<li>Manually inlined all method calls and use bit masking instead of casting where ever applicable</li>
</ul>
		</td>		
		<td>No!</td>
		<td>The go to instance when thread safety is not desired</td>
	</tr>
	<tr>
		<td>PcgRSUFast</td>
		<td>
			<ul>
				<li>highest performance overall and identical code to the PcgRSFast but additionally
			uses static methods instead of local once</li>
				<li>Does not extend java.util.Random</li>
			</ul>
		</td>
		<td>No!</td>
		<td>This is a more lets see
			how quick we can get approach instead of a reasonable class. Usually the PcgRSFast is preferable.</td>
	</tr>
</table>

* 3 separate implementations with different synchronization approaches were implemented and benchmarked with synchronization outperforming
locks and cas instructions by far.
** when speaking about less statistical performance we are still way beyond everything generated by java random or mt!

## Benchmark
Statistical and performance benchmarks were performed using pract rand and Oracle's jmh harness respectively 

### Statistical Quality (performed on nextInt only)

PractRand Test Suite

````
rng=PcgRSUFast stdin32, seed=Random
length= 64 gigabytes (2^36 bytes), time= 2351 seconds
  Test Name                         Raw       Processed     Evaluation
  BCFN(2+0,13-0,T)                  R=  +1.0  p = 0.335     normal           
  BCFN(2+1,13-0,T)                  R=  -2.7  p = 0.865     normal           
  BCFN(2+2,13-0,T)                  R=  +0.7  p = 0.386     normal           
````

<br>
<details>

<summary>Click here to expand the full report</summary>

````
rng=PcgRSUFast stdin32, seed=Random
length= 64 gigabytes (2^36 bytes), time= 2351 seconds
  Test Name                         Raw       Processed     Evaluation
  BCFN(2+0,13-0,T)                  R=  +1.0  p = 0.335     normal           
  BCFN(2+1,13-0,T)                  R=  -2.7  p = 0.865     normal           
  BCFN(2+2,13-0,T)                  R=  +0.7  p = 0.386     normal           
  BCFN(2+3,13-0,T)                  R=  +4.2  p = 0.044     normal           
  BCFN(2+4,13-0,T)                  R=  +1.2  p = 0.315     normal           
  BCFN(2+5,13-0,T)                  R=  -0.8  p = 0.627     normal           
  BCFN(2+6,13-0,T)                  R=  +1.8  p = 0.233     normal           
  BCFN(2+7,13-0,T)                  R=  -2.3  p = 0.824     normal           
  BCFN(2+8,13-1,T)                  R=  -1.9  p = 0.781     normal           
  BCFN(2+9,13-1,T)                  R=  -4.7  p = 0.976     normal           
  BCFN(2+10,13-2,T)                 R=  +3.5  p = 0.078     normal           
  BCFN(2+11,13-3,T)                 R=  +2.7  p = 0.136     normal           
  BCFN(2+12,13-3,T)                 R=  -3.7  p = 0.944     normal           
  BCFN(2+13,13-4,T)                 R=  -0.3  p = 0.532     normal           
  BCFN(2+14,13-5,T)                 R=  -0.4  p = 0.543     normal           
  BCFN(2+15,13-5,T)                 R=  +1.6  p = 0.236     normal           
  BCFN(2+16,13-6,T)                 R=  -0.2  p = 0.494     normal           
  BCFN(2+17,13-6,T)                 R=  +1.5  p = 0.243     normal           
  BCFN(2+18,13-7,T)                 R=  -2.0  p = 0.793     normal           
  BCFN(2+19,13-8,T)                 R=  -3.2  p = 0.960     normal           
  BCFN(2+20,13-8,T)                 R=  -3.1  p = 0.956     normal           
  BCFN(2+21,13-9,T)                 R=  -2.3  p = 0.891     normal           
  BCFN(2+22,13-9,T)                 R=  -2.1  p = 0.860     normal           
  DC6-9x1Bytes-1                    R=  +1.9  p = 0.191     normal           
  Gap-16:A                          R=  +0.5  p = 0.435     normal           
  Gap-16:B                          R=  +0.9  p = 0.270     normal           
  FPF-14+6/16:(0,14-0)              R=  +3.2  p = 0.014     normal           
  FPF-14+6/16:(1,14-0)              R=  -1.2  p = 0.806     normal           
  FPF-14+6/16:(2,14-0)              R=  -1.5  p = 0.861     normal           
  FPF-14+6/16:(3,14-0)              R=  -1.0  p = 0.751     normal           
  FPF-14+6/16:(4,14-0)              R=  +0.5  p = 0.356     normal           
  FPF-14+6/16:(5,14-0)              R=  +1.4  p = 0.158     normal           
  FPF-14+6/16:(6,14-0)              R=  -1.3  p = 0.825     normal           
  FPF-14+6/16:(7,14-0)              R=  -0.5  p = 0.638     normal           
  FPF-14+6/16:(8,14-0)              R=  -0.1  p = 0.538     normal           
  FPF-14+6/16:(9,14-0)              R=  -0.4  p = 0.609     normal           
  FPF-14+6/16:(10,14-0)             R=  +0.6  p = 0.331     normal           
  FPF-14+6/16:(11,14-0)             R=  +0.2  p = 0.433     normal           
  FPF-14+6/16:(12,14-1)             R=  +0.1  p = 0.467     normal           
  FPF-14+6/16:(13,14-2)             R=  +0.3  p = 0.421     normal           
  FPF-14+6/16:(14,14-2)             R=  +0.3  p = 0.427     normal           
  FPF-14+6/16:(15,14-3)             R=  +1.2  p = 0.198     normal           
  FPF-14+6/16:(16,14-4)             R=  +0.6  p = 0.336     normal           
  FPF-14+6/16:(17,14-5)             R=  -1.8  p = 0.904     normal           
  FPF-14+6/16:(18,14-5)             R=  -2.6  p = 0.973     normal           
  FPF-14+6/16:(19,14-6)             R=  +0.4  p = 0.380     normal           
  FPF-14+6/16:(20,14-7)             R=  -0.8  p = 0.713     normal           
  FPF-14+6/16:(21,14-8)             R=  +3.1  p = 0.023     normal           
  FPF-14+6/16:(22,14-8)             R=  +0.8  p = 0.281     normal           
  FPF-14+6/16:(23,14-9)             R=  +0.0  p = 0.464     normal           
  FPF-14+6/16:(24,14-10)            R=  -1.6  p = 0.888     normal           
  FPF-14+6/16:(25,14-11)            R=  -0.9  p = 0.694     normal           
  FPF-14+6/16:(26,14-11)            R=  -0.4  p = 0.539     normal           
  FPF-14+6/16:all                   R=  +0.1  p = 0.478     normal           
  FPF-14+6/16:cross                 R=  +1.4  p = 0.091     normal           
  BRank(12):128(16)                 R=  -0.1  p~= 0.520     normal           
  BRank(12):256(8)                  R=  -0.2  p~= 0.490     normal           
  BRank(12):384(2)                  R=  +1.6  p~= 0.168     normal           
  BRank(12):512(8)                  R=  +2.5  p~= 0.020     normal           
  BRank(12):768(2)                  R=  -0.2  p~= 0.554     normal           
  BRank(12):1K(4)                   R=  +0.4  p~= 0.340     normal           
  BRank(12):1536(1)                 R=  -0.7  p~= 0.689     normal           
  BRank(12):2K(4)                   R=  +1.7  p~= 0.060     normal           
  BRank(12):3K(1)                   R=  +1.8  p~= 0.146     normal           
  BRank(12):4K(2)                   R=  -0.2  p~= 0.554     normal           
  BRank(12):6K(1)                   R=  -0.7  p~= 0.689     normal           
  BRank(12):8K(2)                   R=  +0.8  p~= 0.293     normal           
  BRank(12):12K(1)                  R=  -0.7  p~= 0.689     normal           
  mod3n(5):(0,9-0)                  R=  -2.8  p = 0.921     normal           
  mod3n(5):(1,9-0)                  R=  +0.7  p = 0.369     normal           
  mod3n(5):(2,9-0)                  R=  -0.9  p = 0.675     normal           
  mod3n(5):(3,9-0)                  R=  +0.6  p = 0.388     normal           
  mod3n(5):(4,9-0)                  R=  +0.5  p = 0.408     normal           
  mod3n(5):(5,9-0)                  R=  +2.0  p = 0.166     normal           
  mod3n(5):(6,9-0)                  R=  -1.2  p = 0.721     normal           
  mod3n(5):(7,9-0)                  R=  -1.2  p = 0.727     normal           
  mod3n(5):(8,9-1)                  R=  -1.0  p = 0.696     normal           
  mod3n(5):(9,9-1)                  R=  +1.1  p = 0.292     normal           
  mod3n(5):(10,9-2)                 R=  -1.9  p = 0.829     normal           
  mod3n(5):(11,9-2)                 R=  +1.8  p = 0.187     normal           
  mod3n(5):(12,9-3)                 R=  -0.3  p = 0.556     normal           
  mod3n(5):(13,9-3)                 R=  +1.3  p = 0.255     normal           
  mod3n(5):(14,9-4)                 R=  +0.1  p = 0.468     normal           
  mod3n(5):(15,9-4)                 R=  +1.5  p = 0.216     normal           
  TMFn(2+0):wl                      R=  -0.2  p~= 0.5       normal           
  TMFn(2+1):wl                      R=  +0.2  p~= 0.5       normal           
  TMFn(2+2):wl                      R=  -1.3  p~= 0.7       normal           
  TMFn(2+3):wl                      R=  +0.7  p~= 0.4       normal           
  TMFn(2+4):wl                      R=  +1.8  p~= 0.3       normal           
  TMFn(2+5):wl                      R=  +0.8  p~= 0.4       normal           
  TMFn(2+6):wl                      R=  +1.9  p~= 0.3       normal           
  TMFn(2+7):wl                      R=  +2.2  p~= 0.2       normal           
  TMFn(2+8):wl                      R=  -0.9  p~= 0.6       normal           
  TMFn(2+9):wl                      R=  +1.2  p~= 0.3       normal           
  TMFn(2+10):wl                     R=  +1.5  p~= 0.3       normal           
  TMFn(2+11):wl                     R=  +5.2  p~= 0.08      normal           
  [Low1/8]BCFN(2+0,13-0,T)          R=  +1.8  p = 0.232     normal           
  [Low1/8]BCFN(2+1,13-0,T)          R=  +2.0  p = 0.205     normal           
  [Low1/8]BCFN(2+2,13-0,T)          R=  -0.0  p = 0.500     normal           
  [Low1/8]BCFN(2+3,13-0,T)          R=  -1.0  p = 0.657     normal           
  [Low1/8]BCFN(2+4,13-0,T)          R=  +0.7  p = 0.390     normal           
  [Low1/8]BCFN(2+5,13-1,T)          R=  +1.3  p = 0.299     normal           
  [Low1/8]BCFN(2+6,13-1,T)          R=  +1.6  p = 0.248     normal           
  [Low1/8]BCFN(2+7,13-2,T)          R=  +2.7  p = 0.134     normal           
  [Low1/8]BCFN(2+8,13-3,T)          R=  +0.7  p = 0.377     normal           
  [Low1/8]BCFN(2+9,13-3,T)          R=  -3.1  p = 0.903     normal           
  [Low1/8]BCFN(2+10,13-4,T)         R=  -2.5  p = 0.854     normal           
  [Low1/8]BCFN(2+11,13-5,T)         R=  -3.1  p = 0.910     normal           
  [Low1/8]BCFN(2+12,13-5,T)         R=  +3.3  p = 0.095     normal           
  [Low1/8]BCFN(2+13,13-6,T)         R=  -2.8  p = 0.892     normal           
  [Low1/8]BCFN(2+14,13-6,T)         R=  +1.2  p = 0.281     normal           
  [Low1/8]BCFN(2+15,13-7,T)         R=  +1.2  p = 0.277     normal           
  [Low1/8]BCFN(2+16,13-8,T)         R=  +0.8  p = 0.305     normal           
  [Low1/8]BCFN(2+17,13-8,T)         R=  +0.8  p = 0.311     normal           
  [Low1/8]BCFN(2+18,13-9,T)         R=  -2.4  p = 0.908     normal           
  [Low1/8]BCFN(2+19,13-9,T)         R=  +0.4  p = 0.339     normal           
  [Low1/8]DC6-9x1Bytes-1            R=  +0.8  p = 0.401     normal           
  [Low1/8]Gap-16:A                  R=  -0.7  p = 0.746     normal           
  [Low1/8]Gap-16:B                  R=  -0.2  p = 0.556     normal           
  [Low1/8]FPF-14+6/16:(0,14-0)      R=  +1.1  p = 0.224     normal           
  [Low1/8]FPF-14+6/16:(1,14-0)      R=  -1.9  p = 0.911     normal           
  [Low1/8]FPF-14+6/16:(2,14-0)      R=  -1.7  p = 0.889     normal           
  [Low1/8]FPF-14+6/16:(3,14-0)      R=  +0.4  p = 0.398     normal           
  [Low1/8]FPF-14+6/16:(4,14-0)      R=  +2.1  p = 0.069     normal           
  [Low1/8]FPF-14+6/16:(5,14-0)      R=  -0.4  p = 0.601     normal           
  [Low1/8]FPF-14+6/16:(6,14-0)      R=  +2.2  p = 0.062     normal           
  [Low1/8]FPF-14+6/16:(7,14-0)      R=  +0.5  p = 0.359     normal           
  [Low1/8]FPF-14+6/16:(8,14-0)      R=  -0.5  p = 0.631     normal           
  [Low1/8]FPF-14+6/16:(9,14-1)      R=  -2.3  p = 0.949     normal           
  [Low1/8]FPF-14+6/16:(10,14-2)     R=  -0.6  p = 0.675     normal           
  [Low1/8]FPF-14+6/16:(11,14-2)     R=  -1.4  p = 0.843     normal           
  [Low1/8]FPF-14+6/16:(12,14-3)     R=  -0.5  p = 0.630     normal           
  [Low1/8]FPF-14+6/16:(13,14-4)     R=  -0.6  p = 0.655     normal           
  [Low1/8]FPF-14+6/16:(14,14-5)     R=  +0.2  p = 0.443     normal           
  [Low1/8]FPF-14+6/16:(15,14-5)     R=  -0.7  p = 0.698     normal           
  [Low1/8]FPF-14+6/16:(16,14-6)     R=  -0.1  p = 0.530     normal           
  [Low1/8]FPF-14+6/16:(17,14-7)     R=  -1.5  p = 0.854     normal           
  [Low1/8]FPF-14+6/16:(18,14-8)     R=  +1.0  p = 0.223     normal           
  [Low1/8]FPF-14+6/16:(19,14-8)     R=  -1.1  p = 0.778     normal           
  [Low1/8]FPF-14+6/16:(20,14-9)     R=  +0.9  p = 0.252     normal           
  [Low1/8]FPF-14+6/16:(21,14-10)    R=  -0.2  p = 0.522     normal           
  [Low1/8]FPF-14+6/16:(22,14-11)    R=  +0.4  p = 0.340     normal           
  [Low1/8]FPF-14+6/16:(23,14-11)    R=  +1.7  p = 0.116     normal           
  [Low1/8]FPF-14+6/16:all           R=  -0.4  p = 0.615     normal           
  [Low1/8]FPF-14+6/16:cross         R=  -0.3  p = 0.565     normal           
  [Low1/8]BRank(12):128(8)          R=  +1.5  p~= 0.080     normal           
  [Low1/8]BRank(12):256(8)          R=  -0.7  p~= 0.670     normal           
  [Low1/8]BRank(12):384(2)          R=  +0.8  p~= 0.293     normal           
  [Low1/8]BRank(12):512(4)          R=  -0.8  p~= 0.670     normal           
  [Low1/8]BRank(12):768(1)          R=  +0.4  p~= 0.366     normal           
  [Low1/8]BRank(12):1K(4)           R=  -1.4  p~= 0.890     normal           
  [Low1/8]BRank(12):1536(1)         R=  -0.7  p~= 0.689     normal           
  [Low1/8]BRank(12):2K(2)           R=  -0.2  p~= 0.554     normal           
  [Low1/8]BRank(12):3K(1)           R=  +0.4  p~= 0.366     normal           
  [Low1/8]BRank(12):4K(2)           R=  -1.0  p~= 0.744     normal           
  [Low1/8]BRank(12):6K(1)           R=  -0.7  p~= 0.689     normal           
  [Low1/8]mod3n(5):(0,9-0)          R=  +1.0  p = 0.303     normal           
  [Low1/8]mod3n(5):(1,9-0)          R=  -2.1  p = 0.849     normal           
  [Low1/8]mod3n(5):(2,9-0)          R=  +3.5  p = 0.044     normal           
  [Low1/8]mod3n(5):(3,9-0)          R=  -0.4  p = 0.577     normal           
  [Low1/8]mod3n(5):(4,9-0)          R=  +0.4  p = 0.421     normal           
  [Low1/8]mod3n(5):(5,9-1)          R=  +1.0  p = 0.306     normal           
  [Low1/8]mod3n(5):(6,9-1)          R=  -1.1  p = 0.712     normal           
  [Low1/8]mod3n(5):(7,9-2)          R=  -1.6  p = 0.790     normal           
  [Low1/8]mod3n(5):(8,9-2)          R=  -1.4  p = 0.756     normal           
  [Low1/8]mod3n(5):(9,9-3)          R=  -2.8  p = 0.926     normal           
  [Low1/8]mod3n(5):(10,9-3)         R=  -0.0  p = 0.494     normal           
  [Low1/8]mod3n(5):(11,9-4)         R=  -0.2  p = 0.530     normal           
  [Low1/8]mod3n(5):(12,9-4)         R=  +3.5  p = 0.047     normal           
  [Low1/8]mod3n(5):(13,9-5)         R=  +2.5  p = 0.108     normal           
  [Low1/8]mod3n(5):(14,9-5)         R=  -1.8  p = 0.830     normal           
  [Low1/8]mod3n(5):(15,9-6)         R=  +3.7  p = 0.042     normal           
  [Low1/8]TMFn(2+0):wl              R=  -2.0  p~= 0.7       normal           
  [Low1/8]TMFn(2+1):wl              R=  -1.5  p~= 0.7       normal           
  [Low1/8]TMFn(2+2):wl              R=  -3.7  p~= 0.9       normal           
  [Low1/8]TMFn(2+3):wl              R=  -3.4  p~= 0.8       normal           
  [Low1/8]TMFn(2+4):wl              R=  -0.2  p~= 0.5       normal           
  [Low1/8]TMFn(2+5):wl              R=  +0.6  p~= 0.4       normal           
  [Low1/8]TMFn(2+6):wl              R=  +0.3  p~= 0.4       normal           
  [Low1/8]TMFn(2+7):wl              R=  -0.1  p~= 0.5       normal           
  [Low1/8]TMFn(2+8):wl              R=  +2.3  p~= 0.2       normal           
  [Low1/16]BCFN(2+0,13-0,T)         R=  +0.9  p = 0.356     normal           
  [Low1/16]BCFN(2+1,13-0,T)         R=  +0.9  p = 0.360     normal           
  [Low1/16]BCFN(2+2,13-0,T)         R=  +0.4  p = 0.435     normal           
  [Low1/16]BCFN(2+3,13-0,T)         R=  +4.1  p = 0.050     normal           
  [Low1/16]BCFN(2+4,13-1,T)         R=  -0.6  p = 0.587     normal           
  [Low1/16]BCFN(2+5,13-1,T)         R=  +0.3  p = 0.450     normal           
  [Low1/16]BCFN(2+6,13-2,T)         R=  +1.5  p = 0.261     normal           
  [Low1/16]BCFN(2+7,13-3,T)         R=  +6.2  p =  9.3e-3   normal           
  [Low1/16]BCFN(2+8,13-3,T)         R=  -0.6  p = 0.581     normal           
  [Low1/16]BCFN(2+9,13-4,T)         R=  +1.4  p = 0.269     normal           
  [Low1/16]BCFN(2+10,13-5,T)        R=  -2.0  p = 0.795     normal           
  [Low1/16]BCFN(2+11,13-5,T)        R=  +1.6  p = 0.239     normal           
  [Low1/16]BCFN(2+12,13-6,T)        R=  -0.7  p = 0.577     normal           
  [Low1/16]BCFN(2+13,13-6,T)        R=  -0.3  p = 0.517     normal           
  [Low1/16]BCFN(2+14,13-7,T)        R=  +0.4  p = 0.381     normal           
  [Low1/16]BCFN(2+15,13-8,T)        R=  +0.5  p = 0.359     normal           
  [Low1/16]BCFN(2+16,13-8,T)        R=  +2.8  p = 0.116     normal           
  [Low1/16]BCFN(2+17,13-9,T)        R=  +0.6  p = 0.314     normal           
  [Low1/16]BCFN(2+18,13-9,T)        R=  +1.0  p = 0.263     normal           
  [Low1/16]DC6-9x1Bytes-1           R=  -2.5  p = 0.942     normal           
  [Low1/16]Gap-16:A                 R=  -2.1  p = 0.957     normal           
  [Low1/16]Gap-16:B                 R=  -0.2  p = 0.551     normal           
  [Low1/16]FPF-14+6/16:(0,14-0)     R=  -0.2  p = 0.566     normal           
  [Low1/16]FPF-14+6/16:(1,14-0)     R=  -1.3  p = 0.828     normal           
  [Low1/16]FPF-14+6/16:(2,14-0)     R=  +2.1  p = 0.075     normal           
  [Low1/16]FPF-14+6/16:(3,14-0)     R=  +0.9  p = 0.263     normal           
  [Low1/16]FPF-14+6/16:(4,14-0)     R=  -1.2  p = 0.811     normal           
  [Low1/16]FPF-14+6/16:(5,14-0)     R=  -1.5  p = 0.863     normal           
  [Low1/16]FPF-14+6/16:(6,14-0)     R=  -0.0  p = 0.505     normal           
  [Low1/16]FPF-14+6/16:(7,14-0)     R=  -0.3  p = 0.592     normal           
  [Low1/16]FPF-14+6/16:(8,14-1)     R=  +2.2  p = 0.062     normal           
  [Low1/16]FPF-14+6/16:(9,14-2)     R=  +1.7  p = 0.120     normal           
  [Low1/16]FPF-14+6/16:(10,14-2)    R=  -1.3  p = 0.815     normal           
  [Low1/16]FPF-14+6/16:(11,14-3)    R=  +0.5  p = 0.363     normal           
  [Low1/16]FPF-14+6/16:(12,14-4)    R=  +2.4  p = 0.047     normal           
  [Low1/16]FPF-14+6/16:(13,14-5)    R=  -2.1  p = 0.939     normal           
  [Low1/16]FPF-14+6/16:(14,14-5)    R=  +1.9  p = 0.092     normal           
  [Low1/16]FPF-14+6/16:(15,14-6)    R=  -0.1  p = 0.520     normal           
  [Low1/16]FPF-14+6/16:(16,14-7)    R=  +0.1  p = 0.455     normal           
  [Low1/16]FPF-14+6/16:(17,14-8)    R=  -0.7  p = 0.690     normal           
  [Low1/16]FPF-14+6/16:(18,14-8)    R=  -1.3  p = 0.815     normal           
  [Low1/16]FPF-14+6/16:(19,14-9)    R=  +2.5  p = 0.054     normal           
  [Low1/16]FPF-14+6/16:(20,14-10)   R=  +1.5  p = 0.141     normal           
  [Low1/16]FPF-14+6/16:(21,14-11)   R=  -1.4  p = 0.866     normal           
  [Low1/16]FPF-14+6/16:(22,14-11)   R=  -0.3  p = 0.523     normal           
  [Low1/16]FPF-14+6/16:all          R=  +0.3  p = 0.438     normal           
  [Low1/16]FPF-14+6/16:cross        R=  +2.6  p = 0.014     normal           
  [Low1/16]BRank(12):128(8)         R=  -0.3  p~= 0.500     normal           
  [Low1/16]BRank(12):256(4)         R=  +2.2  p~= 0.030     normal           
  [Low1/16]BRank(12):384(2)         R=  -1.0  p~= 0.744     normal           
  [Low1/16]BRank(12):512(4)         R=  -0.8  p~= 0.670     normal           
  [Low1/16]BRank(12):768(1)         R=  -0.7  p~= 0.689     normal           
  [Low1/16]BRank(12):1K(2)          R=  -1.0  p~= 0.744     normal           
  [Low1/16]BRank(12):1536(1)        R=  -0.7  p~= 0.689     normal           
  [Low1/16]BRank(12):2K(2)          R=  -0.2  p~= 0.554     normal           
  [Low1/16]BRank(12):3K(1)          R=  -0.7  p~= 0.689     normal           
  [Low1/16]BRank(12):4K(1)          R=  +0.4  p~= 0.366     normal           
  [Low1/16]mod3n(5):(0,9-0)         R=  -1.6  p = 0.792     normal           
  [Low1/16]mod3n(5):(1,9-0)         R=  +2.2  p = 0.134     normal           
  [Low1/16]mod3n(5):(2,9-0)         R=  +2.0  p = 0.163     normal           
  [Low1/16]mod3n(5):(3,9-0)         R=  -3.5  p = 0.962     normal           
  [Low1/16]mod3n(5):(4,9-1)         R=  -4.4  p = 0.987     normal           
  [Low1/16]mod3n(5):(5,9-1)         R=  +1.3  p = 0.262     normal           
  [Low1/16]mod3n(5):(6,9-2)         R=  +1.1  p = 0.287     normal           
  [Low1/16]mod3n(5):(7,9-2)         R=  -1.6  p = 0.794     normal           
  [Low1/16]mod3n(5):(8,9-3)         R=  -4.5  p =1-7.5e-3   normal           
  [Low1/16]mod3n(5):(9,9-3)         R=  -1.8  p = 0.811     normal           
  [Low1/16]mod3n(5):(10,9-4)        R=  +0.3  p = 0.423     normal           
  [Low1/16]mod3n(5):(11,9-4)        R=  +2.2  p = 0.132     normal           
  [Low1/16]mod3n(5):(12,9-5)        R=  -2.1  p = 0.871     normal           
  [Low1/16]mod3n(5):(13,9-5)        R=  +0.2  p = 0.434     normal           
  [Low1/16]mod3n(5):(14,9-6)        R=  -1.3  p = 0.729     normal           
  [Low1/16]mod3n(5):(15,9-6)        R=  -2.1  p = 0.893     normal           
  [Low1/16]TMFn(2+0):wl             R=  +3.2  p~= 0.2       normal           
  [Low1/16]TMFn(2+1):wl             R=  +0.3  p~= 0.4       normal           
  [Low1/16]TMFn(2+2):wl             R=  -0.9  p~= 0.6       normal           
  [Low1/16]TMFn(2+3):wl             R=  +1.0  p~= 0.4       normal           
  [Low1/16]TMFn(2+4):wl             R=  -1.2  p~= 0.7       normal           
  [Low1/16]TMFn(2+5):wl             R=  -2.9  p~= 0.8       normal           
  [Low1/16]TMFn(2+6):wl             R=  +0.2  p~= 0.5       normal           
  [Low1/16]TMFn(2+7):wl             R=  +1.0  p~= 0.4       normal           
  [Low1/32]BCFN(2+0,13-0,T)         R=  +2.2  p = 0.184     normal           
  [Low1/32]BCFN(2+1,13-0,T)         R=  +4.8  p = 0.028     normal           
  [Low1/32]BCFN(2+2,13-1,T)         R=  +0.7  p = 0.376     normal           
  [Low1/32]BCFN(2+3,13-1,T)         R=  -3.3  p = 0.914     normal           
  [Low1/32]BCFN(2+4,13-1,T)         R=  -2.2  p = 0.817     normal           
  [Low1/32]BCFN(2+5,13-2,T)         R=  -1.4  p = 0.706     normal           
  [Low1/32]BCFN(2+6,13-3,T)         R=  -0.2  p = 0.522     normal           
  [Low1/32]BCFN(2+7,13-3,T)         R=  -1.8  p = 0.769     normal           
  [Low1/32]BCFN(2+8,13-4,T)         R=  +1.7  p = 0.236     normal           
  [Low1/32]BCFN(2+9,13-5,T)         R=  +0.5  p = 0.399     normal           
  [Low1/32]BCFN(2+10,13-5,T)        R=  +3.0  p = 0.113     normal           
  [Low1/32]BCFN(2+11,13-6,T)        R=  -1.2  p = 0.675     normal           
  [Low1/32]BCFN(2+12,13-6,T)        R=  +3.3  p = 0.094     normal           
  [Low1/32]BCFN(2+13,13-7,T)        R=  +7.2  p =  8.3e-3   normal           
  [Low1/32]BCFN(2+14,13-8,T)        R=  +6.1  p = 0.018     normal           
  [Low1/32]BCFN(2+15,13-8,T)        R=  +8.5  p =  4.5e-3   normal           
  [Low1/32]BCFN(2+16,13-9,T)        R= +12.7  p =  5.8e-4   normal           
  [Low1/32]BCFN(2+17,13-9,T)        R=  +6.1  p = 0.019     normal           
  [Low1/32]DC6-9x1Bytes-1           R=  +2.8  p = 0.116     normal           
  [Low1/32]Gap-16:A                 R=  +0.7  p = 0.415     normal           
  [Low1/32]Gap-16:B                 R=  -1.2  p = 0.802     normal           
  [Low1/32]FPF-14+6/16:(0,14-0)     R=  -1.2  p = 0.795     normal           
  [Low1/32]FPF-14+6/16:(1,14-0)     R=  +0.4  p = 0.394     normal           
  [Low1/32]FPF-14+6/16:(2,14-0)     R=  -3.0  p = 0.984     normal           
  [Low1/32]FPF-14+6/16:(3,14-0)     R=  +0.6  p = 0.338     normal           
  [Low1/32]FPF-14+6/16:(4,14-0)     R=  -1.2  p = 0.795     normal           
  [Low1/32]FPF-14+6/16:(5,14-0)     R=  -0.8  p = 0.712     normal           
  [Low1/32]FPF-14+6/16:(6,14-0)     R=  +1.0  p = 0.239     normal           
  [Low1/32]FPF-14+6/16:(7,14-1)     R=  +0.8  p = 0.289     normal           
  [Low1/32]FPF-14+6/16:(8,14-2)     R=  +2.0  p = 0.079     normal           
  [Low1/32]FPF-14+6/16:(9,14-2)     R=  +0.7  p = 0.323     normal           
  [Low1/32]FPF-14+6/16:(10,14-3)    R=  +0.9  p = 0.262     normal           
  [Low1/32]FPF-14+6/16:(11,14-4)    R=  +0.6  p = 0.342     normal           
  [Low1/32]FPF-14+6/16:(12,14-5)    R=  -1.1  p = 0.782     normal           
  [Low1/32]FPF-14+6/16:(13,14-5)    R=  -2.4  p = 0.959     normal           
  [Low1/32]FPF-14+6/16:(14,14-6)    R=  +2.7  p = 0.034     normal           
  [Low1/32]FPF-14+6/16:(15,14-7)    R=  -0.2  p = 0.543     normal           
  [Low1/32]FPF-14+6/16:(16,14-8)    R=  -0.9  p = 0.724     normal           
  [Low1/32]FPF-14+6/16:(17,14-8)    R=  -0.9  p = 0.734     normal           
  [Low1/32]FPF-14+6/16:(18,14-9)    R=  -0.4  p = 0.580     normal           
  [Low1/32]FPF-14+6/16:(19,14-10)   R=  +0.4  p = 0.358     normal           
  [Low1/32]FPF-14+6/16:(20,14-11)   R=  +0.6  p = 0.296     normal           
  [Low1/32]FPF-14+6/16:(21,14-11)   R=  -1.0  p = 0.730     normal           
  [Low1/32]FPF-14+6/16:all          R=  -0.8  p = 0.730     normal           
  [Low1/32]FPF-14+6/16:cross        R=  -0.7  p = 0.736     normal           
  [Low1/32]BRank(12):128(8)         R=  -1.9  p~= 0.990     normal           
  [Low1/32]BRank(12):256(4)         R=  -0.1  p~= 0.490     normal           
  [Low1/32]BRank(12):384(2)         R=  -0.2  p~= 0.554     normal           
  [Low1/32]BRank(12):512(4)         R=  +0.4  p~= 0.340     normal           
  [Low1/32]BRank(12):768(1)         R=  +1.8  p~= 0.146     normal           
  [Low1/32]BRank(12):1K(2)          R=  -0.2  p~= 0.554     normal           
  [Low1/32]BRank(12):1536(1)        R=  -0.7  p~= 0.689     normal           
  [Low1/32]BRank(12):2K(2)          R=  -0.2  p~= 0.554     normal           
  [Low1/32]BRank(12):3K(1)          R=  -0.7  p~= 0.689     normal           
  [Low1/32]BRank(12):4K(1)          R=  +0.4  p~= 0.366     normal           
  [Low1/32]mod3n(5):(0,9-0)         R=  -2.2  p = 0.861     normal           
  [Low1/32]mod3n(5):(1,9-0)         R=  +2.0  p = 0.165     normal           
  [Low1/32]mod3n(5):(2,9-0)         R=  -1.9  p = 0.834     normal           
  [Low1/32]mod3n(5):(3,9-1)         R=  -0.2  p = 0.544     normal           
  [Low1/32]mod3n(5):(4,9-1)         R=  +2.2  p = 0.140     normal           
  [Low1/32]mod3n(5):(5,9-2)         R=  -2.8  p = 0.920     normal           
  [Low1/32]mod3n(5):(6,9-2)         R=  -2.1  p = 0.860     normal           
  [Low1/32]mod3n(5):(7,9-3)         R=  +0.2  p = 0.443     normal           
  [Low1/32]mod3n(5):(8,9-3)         R=  +2.4  p = 0.114     normal           
  [Low1/32]mod3n(5):(9,9-4)         R=  -1.9  p = 0.832     normal           
  [Low1/32]mod3n(5):(10,9-4)        R=  -0.2  p = 0.515     normal           
  [Low1/32]mod3n(5):(11,9-5)        R=  +1.6  p = 0.194     normal           
  [Low1/32]mod3n(5):(12,9-5)        R=  +0.7  p = 0.322     normal           
  [Low1/32]mod3n(5):(13,9-6)        R=  -1.8  p = 0.846     normal           
  [Low1/32]mod3n(5):(14,9-6)        R=  -1.2  p = 0.719     normal           
  [Low1/32]mod3n(5):(15,9-6)        R=  -2.0  p = 0.882     normal           
  [Low1/32]TMFn(2+0):wl             R=  +1.5  p~= 0.3       normal           
  [Low1/32]TMFn(2+1):wl             R=  +0.3  p~= 0.5       normal           
  [Low1/32]TMFn(2+2):wl             R=  +1.2  p~= 0.3       normal           
  [Low1/32]TMFn(2+3):wl             R=  -2.9  p~= 0.8       normal           
  [Low1/32]TMFn(2+4):wl             R=  +0.1  p~= 0.5       normal           
  [Low1/32]TMFn(2+5):wl             R=  -0.0  p~= 0.5       normal           
  [Low1/32]TMFn(2+6):wl             R=  +1.2  p~= 0.3       normal           
  [Low1/64]BCFN(2+0,13-1,T)         R=  +3.9  p = 0.060     normal           
  [Low1/64]BCFN(2+1,13-1,T)         R=  +1.8  p = 0.223     normal           
  [Low1/64]BCFN(2+2,13-1,T)         R=  -0.8  p = 0.623     normal           
  [Low1/64]BCFN(2+3,13-1,T)         R=  -2.5  p = 0.846     normal           
  [Low1/64]BCFN(2+4,13-2,T)         R=  +2.1  p = 0.192     normal           
  [Low1/64]BCFN(2+5,13-3,T)         R=  +2.7  p = 0.133     normal           
  [Low1/64]BCFN(2+6,13-3,T)         R=  -0.5  p = 0.569     normal           
  [Low1/64]BCFN(2+7,13-4,T)         R=  +2.8  p = 0.125     normal           
  [Low1/64]BCFN(2+8,13-5,T)         R=  -1.5  p = 0.721     normal           
  [Low1/64]BCFN(2+9,13-5,T)         R=  +6.2  p = 0.012     normal           
  [Low1/64]BCFN(2+10,13-6,T)        R=  +6.0  p = 0.016     normal           
  [Low1/64]BCFN(2+11,13-6,T)        R=  +0.7  p = 0.352     normal           
  [Low1/64]BCFN(2+12,13-7,T)        R=  +7.0  p =  9.3e-3   normal           
  [Low1/64]BCFN(2+13,13-8,T)        R=  +4.7  p = 0.042     normal           
  [Low1/64]BCFN(2+14,13-8,T)        R=  +7.2  p =  9.6e-3   normal           
  [Low1/64]BCFN(2+15,13-9,T)        R=  +6.4  p = 0.017     normal           
  [Low1/64]BCFN(2+16,13-9,T)        R=  +3.7  p = 0.068     normal           
  [Low1/64]DC6-9x1Bytes-1           R=  +1.7  p = 0.258     normal           
  [Low1/64]Gap-16:A                 R=  +3.0  p = 0.038     normal           
  [Low1/64]Gap-16:B                 R=  +3.0  p = 0.018     normal           
  [Low1/64]FPF-14+6/16:(0,14-0)     R=  -0.3  p = 0.580     normal           
  [Low1/64]FPF-14+6/16:(1,14-0)     R=  +1.6  p = 0.130     normal           
  [Low1/64]FPF-14+6/16:(2,14-0)     R=  -0.3  p = 0.596     normal           
  [Low1/64]FPF-14+6/16:(3,14-0)     R=  +0.5  p = 0.370     normal           
  [Low1/64]FPF-14+6/16:(4,14-0)     R=  -1.8  p = 0.897     normal           
  [Low1/64]FPF-14+6/16:(5,14-0)     R=  -0.5  p = 0.627     normal           
  [Low1/64]FPF-14+6/16:(6,14-1)     R=  -0.3  p = 0.580     normal           
  [Low1/64]FPF-14+6/16:(7,14-2)     R=  +1.3  p = 0.191     normal           
  [Low1/64]FPF-14+6/16:(8,14-2)     R=  -0.6  p = 0.674     normal           
  [Low1/64]FPF-14+6/16:(9,14-3)     R=  -1.9  p = 0.913     normal           
  [Low1/64]FPF-14+6/16:(10,14-4)    R=  +2.2  p = 0.062     normal           
  [Low1/64]FPF-14+6/16:(11,14-5)    R=  -1.5  p = 0.864     normal           
  [Low1/64]FPF-14+6/16:(12,14-5)    R=  -1.6  p = 0.870     normal           
  [Low1/64]FPF-14+6/16:(13,14-6)    R=  -0.9  p = 0.722     normal           
  [Low1/64]FPF-14+6/16:(14,14-7)    R=  -1.6  p = 0.875     normal           
  [Low1/64]FPF-14+6/16:(15,14-8)    R=  -1.1  p = 0.775     normal           
  [Low1/64]FPF-14+6/16:(16,14-8)    R=  -1.5  p = 0.866     normal           
  [Low1/64]FPF-14+6/16:(17,14-9)    R=  +1.2  p = 0.186     normal           
  [Low1/64]FPF-14+6/16:(18,14-10)   R=  +0.6  p = 0.309     normal           
  [Low1/64]FPF-14+6/16:(19,14-11)   R=  -0.2  p = 0.474     normal           
  [Low1/64]FPF-14+6/16:(20,14-11)   R=  +1.0  p = 0.215     normal           
  [Low1/64]FPF-14+6/16:all          R=  -0.6  p = 0.669     normal           
  [Low1/64]FPF-14+6/16:cross        R=  +0.7  p = 0.215     normal           
  [Low1/64]BRank(12):128(8)         R=  -1.1  p~= 0.860     normal           
  [Low1/64]BRank(12):256(4)         R=  +1.7  p~= 0.060     normal           
  [Low1/64]BRank(12):384(1)         R=  +0.4  p~= 0.366     normal           
  [Low1/64]BRank(12):512(4)         R=  -1.4  p~= 0.890     normal           
  [Low1/64]BRank(12):768(1)         R=  -0.7  p~= 0.689     normal           
  [Low1/64]BRank(12):1K(2)          R=  -0.2  p~= 0.554     normal           
  [Low1/64]BRank(12):1536(1)        R=  -0.7  p~= 0.689     normal           
  [Low1/64]BRank(12):2K(2)          R=  -0.2  p~= 0.554     normal           
  [Low1/64]BRank(12):3K(1)          R=  +0.4  p~= 0.366     normal           
  [Low1/64]mod3n(5):(0,9-0)         R=  +2.2  p = 0.140     normal           
  [Low1/64]mod3n(5):(1,9-0)         R=  -1.8  p = 0.815     normal           
  [Low1/64]mod3n(5):(2,9-1)         R=  +0.5  p = 0.395     normal           
  [Low1/64]mod3n(5):(3,9-1)         R=  +0.2  p = 0.459     normal           
  [Low1/64]mod3n(5):(4,9-2)         R=  +2.0  p = 0.155     normal           
  [Low1/64]mod3n(5):(5,9-2)         R=  +1.2  p = 0.267     normal           
  [Low1/64]mod3n(5):(6,9-3)         R=  +4.4  p = 0.019     normal           
  [Low1/64]mod3n(5):(7,9-3)         R=  +1.5  p = 0.221     normal           
  [Low1/64]mod3n(5):(8,9-4)         R=  -0.3  p = 0.544     normal           
  [Low1/64]mod3n(5):(9,9-4)         R=  -1.0  p = 0.677     normal           
  [Low1/64]mod3n(5):(10,9-5)        R=  -1.1  p = 0.688     normal           
  [Low1/64]mod3n(5):(11,9-5)        R=  -2.1  p = 0.868     normal           
  [Low1/64]mod3n(5):(12,9-6)        R=  -0.3  p = 0.501     normal           
  [Low1/64]mod3n(5):(13,9-6)        R=  -0.1  p = 0.472     normal           
  [Low1/64]mod3n(5):(14,9-6)        R=  -0.7  p = 0.604     normal           
  [Low1/64]mod3n(5):(15,9-6)        R=  +2.6  p = 0.091     normal           
  [Low1/64]TMFn(2+0):wl             R=  -2.8  p~= 0.8       normal           
  [Low1/64]TMFn(2+1):wl             R=  -0.9  p~= 0.6       normal           
  [Low1/64]TMFn(2+2):wl             R=  -0.6  p~= 0.6       normal           
  [Low1/64]TMFn(2+3):wl             R=  -2.1  p~= 0.8       normal           
  [Low1/64]TMFn(2+4):wl             R=  +0.3  p~= 0.5       normal           
  [Low1/64]TMFn(2+5):wl             R=  -0.5  p~= 0.6       normal           
  [Low4/16]BCFN(2+0,13-0,T)         R=  -2.6  p = 0.855     normal           
  [Low4/16]BCFN(2+1,13-0,T)         R=  -0.8  p = 0.621     normal           
  [Low4/16]BCFN(2+2,13-0,T)         R=  -3.3  p = 0.912     normal           
  [Low4/16]BCFN(2+3,13-0,T)         R=  +0.4  p = 0.437     normal           
  [Low4/16]BCFN(2+4,13-0,T)         R=  +1.0  p = 0.339     normal           
  [Low4/16]BCFN(2+5,13-0,T)         R=  +3.7  p = 0.066     normal           
  [Low4/16]BCFN(2+6,13-1,T)         R=  -3.4  p = 0.918     normal           
  [Low4/16]BCFN(2+7,13-1,T)         R=  +1.2  p = 0.303     normal           
  [Low4/16]BCFN(2+8,13-2,T)         R=  +2.3  p = 0.173     normal           
  [Low4/16]BCFN(2+9,13-3,T)         R=  +1.6  p = 0.246     normal           
  [Low4/16]BCFN(2+10,13-3,T)        R=  -2.1  p = 0.801     normal           
  [Low4/16]BCFN(2+11,13-4,T)        R=  -3.1  p = 0.906     normal           
  [Low4/16]BCFN(2+12,13-5,T)        R=  +3.4  p = 0.090     normal           
  [Low4/16]BCFN(2+13,13-5,T)        R=  -3.5  p = 0.939     normal           
  [Low4/16]BCFN(2+14,13-6,T)        R=  +4.4  p = 0.047     normal           
  [Low4/16]BCFN(2+15,13-6,T)        R=  +0.9  p = 0.327     normal           
  [Low4/16]BCFN(2+16,13-7,T)        R=  +0.3  p = 0.402     normal           
  [Low4/16]BCFN(2+17,13-8,T)        R=  -0.0  p = 0.437     normal           
  [Low4/16]BCFN(2+18,13-8,T)        R=  +0.6  p = 0.335     normal           
  [Low4/16]BCFN(2+19,13-9,T)        R=  +2.3  p = 0.139     normal           
  [Low4/16]BCFN(2+20,13-9,T)        R=  -2.6  p = 0.945     normal           
  [Low4/16]DC6-9x1Bytes-1           R=  +3.6  p = 0.029     normal           
  [Low4/16]Gap-16:A                 R=  +1.0  p = 0.308     normal           
  [Low4/16]Gap-16:B                 R=  -0.4  p = 0.606     normal           
  [Low4/16]FPF-14+6/16:(0,14-0)     R=  +1.7  p = 0.119     normal           
  [Low4/16]FPF-14+6/16:(1,14-0)     R=  -2.3  p = 0.946     normal           
  [Low4/16]FPF-14+6/16:(2,14-0)     R=  +0.1  p = 0.461     normal           
  [Low4/16]FPF-14+6/16:(3,14-0)     R=  +0.7  p = 0.316     normal           
  [Low4/16]FPF-14+6/16:(4,14-0)     R=  +2.8  p = 0.024     normal           
  [Low4/16]FPF-14+6/16:(5,14-0)     R=  -2.0  p = 0.918     normal           
  [Low4/16]FPF-14+6/16:(6,14-0)     R=  -1.2  p = 0.803     normal           
  [Low4/16]FPF-14+6/16:(7,14-0)     R=  +1.6  p = 0.137     normal           
  [Low4/16]FPF-14+6/16:(8,14-0)     R=  -0.0  p = 0.508     normal           
  [Low4/16]FPF-14+6/16:(9,14-0)     R=  +2.4  p = 0.048     normal           
  [Low4/16]FPF-14+6/16:(10,14-1)    R=  -0.6  p = 0.672     normal           
  [Low4/16]FPF-14+6/16:(11,14-2)    R=  +1.0  p = 0.238     normal           
  [Low4/16]FPF-14+6/16:(12,14-2)    R=  +1.5  p = 0.156     normal           
  [Low4/16]FPF-14+6/16:(13,14-3)    R=  +0.9  p = 0.269     normal           
  [Low4/16]FPF-14+6/16:(14,14-4)    R=  -1.3  p = 0.825     normal           
  [Low4/16]FPF-14+6/16:(15,14-5)    R=  -0.8  p = 0.715     normal           
  [Low4/16]FPF-14+6/16:(16,14-5)    R=  +1.4  p = 0.156     normal           
  [Low4/16]FPF-14+6/16:(17,14-6)    R=  +1.6  p = 0.138     normal           
  [Low4/16]FPF-14+6/16:(18,14-7)    R=  +0.9  p = 0.249     normal           
  [Low4/16]FPF-14+6/16:(19,14-8)    R=  -1.2  p = 0.799     normal           
  [Low4/16]FPF-14+6/16:(20,14-8)    R=  +0.8  p = 0.280     normal           
  [Low4/16]FPF-14+6/16:(21,14-9)    R=  -2.7  p = 0.989     normal           
  [Low4/16]FPF-14+6/16:(22,14-10)   R=  -0.8  p = 0.691     normal           
  [Low4/16]FPF-14+6/16:(23,14-11)   R=  -1.0  p = 0.741     normal           
  [Low4/16]FPF-14+6/16:(24,14-11)   R=  -1.2  p = 0.794     normal           
  [Low4/16]FPF-14+6/16:all          R=  +1.5  p = 0.155     normal           
  [Low4/16]FPF-14+6/16:cross        R=  +0.7  p = 0.231     normal           
  [Low4/16]BRank(12):128(8)         R=  -0.7  p~= 0.750     normal           
  [Low4/16]BRank(12):256(8)         R=  -1.5  p~= 0.940     normal           
  [Low4/16]BRank(12):384(2)         R=  -1.0  p~= 0.744     normal           
  [Low4/16]BRank(12):512(4)         R=  -1.4  p~= 0.890     normal           
  [Low4/16]BRank(12):768(2)         R=  -1.0  p~= 0.744     normal           
  [Low4/16]BRank(12):1K(4)          R=  -0.1  p~= 0.490     normal           
  [Low4/16]BRank(12):1536(1)        R=  +1.8  p~= 0.146     normal           
  [Low4/16]BRank(12):2K(2)          R=  -1.0  p~= 0.744     normal           
  [Low4/16]BRank(12):3K(1)          R=  -0.7  p~= 0.689     normal           
  [Low4/16]BRank(12):4K(2)          R=  -1.0  p~= 0.744     normal           
  [Low4/16]BRank(12):6K(1)          R=  -0.7  p~= 0.689     normal           
  [Low4/16]BRank(12):8K(1)          R=  -0.7  p~= 0.689     normal           
  [Low4/16]mod3n(5):(0,9-0)         R=  -4.5  p = 0.989     normal           
  [Low4/16]mod3n(5):(1,9-0)         R=  +2.9  p = 0.075     normal           
  [Low4/16]mod3n(5):(2,9-0)         R=  -0.6  p = 0.621     normal           
  [Low4/16]mod3n(5):(3,9-0)         R=  +2.8  p = 0.081     normal           
  [Low4/16]mod3n(5):(4,9-0)         R=  -0.2  p = 0.538     normal           
  [Low4/16]mod3n(5):(5,9-0)         R=  +2.7  p = 0.094     normal           
  [Low4/16]mod3n(5):(6,9-1)         R=  -2.3  p = 0.872     normal           
  [Low4/16]mod3n(5):(7,9-1)         R=  +1.8  p = 0.180     normal           
  [Low4/16]mod3n(5):(8,9-2)         R=  +2.4  p = 0.120     normal           
  [Low4/16]mod3n(5):(9,9-2)         R=  +2.8  p = 0.084     normal           
  [Low4/16]mod3n(5):(10,9-3)        R=  +0.5  p = 0.394     normal           
  [Low4/16]mod3n(5):(11,9-3)        R=  +5.6  p =  4.5e-3   normal           
  [Low4/16]mod3n(5):(12,9-4)        R=  +1.4  p = 0.237     normal           
  [Low4/16]mod3n(5):(13,9-4)        R=  +1.1  p = 0.281     normal           
  [Low4/16]mod3n(5):(14,9-5)        R=  -1.5  p = 0.779     normal           
  [Low4/16]mod3n(5):(15,9-5)        R=  +0.8  p = 0.315     normal           
  [Low4/16]TMFn(2+0):wl             R=  -0.9  p~= 0.6       normal           
  [Low4/16]TMFn(2+1):wl             R=  -2.5  p~= 0.8       normal           
  [Low4/16]TMFn(2+2):wl             R=  -1.9  p~= 0.7       normal           
  [Low4/16]TMFn(2+3):wl             R=  +0.3  p~= 0.5       normal           
  [Low4/16]TMFn(2+4):wl             R=  +0.6  p~= 0.4       normal           
  [Low4/16]TMFn(2+5):wl             R=  -0.8  p~= 0.6       normal           
  [Low4/16]TMFn(2+6):wl             R=  +0.6  p~= 0.4       normal           
  [Low4/16]TMFn(2+7):wl             R=  +1.0  p~= 0.4       normal           
  [Low4/16]TMFn(2+8):wl             R=  +0.1  p~= 0.5       normal           
  [Low4/16]TMFn(2+9):wl             R=  +2.3  p~= 0.2       normal           
  [Low4/32]BCFN(2+0,13-0,T)         R=  +0.8  p = 0.372     normal           
  [Low4/32]BCFN(2+1,13-0,T)         R=  +5.1  p = 0.021     normal           
  [Low4/32]BCFN(2+2,13-0,T)         R=  +2.6  p = 0.144     normal           
  [Low4/32]BCFN(2+3,13-0,T)         R=  -0.1  p = 0.518     normal           
  [Low4/32]BCFN(2+4,13-0,T)         R=  -1.4  p = 0.719     normal           
  [Low4/32]BCFN(2+5,13-1,T)         R=  -2.7  p = 0.870     normal           
  [Low4/32]BCFN(2+6,13-1,T)         R=  -3.2  p = 0.910     normal           
  [Low4/32]BCFN(2+7,13-2,T)         R=  -1.1  p = 0.660     normal           
  [Low4/32]BCFN(2+8,13-3,T)         R=  -4.0  p = 0.955     normal           
  [Low4/32]BCFN(2+9,13-3,T)         R=  +2.4  p = 0.161     normal           
  [Low4/32]BCFN(2+10,13-4,T)        R=  +2.7  p = 0.138     normal           
  [Low4/32]BCFN(2+11,13-5,T)        R=  +4.0  p = 0.058     normal           
  [Low4/32]BCFN(2+12,13-5,T)        R=  +0.8  p = 0.351     normal           
  [Low4/32]BCFN(2+13,13-6,T)        R=  -2.8  p = 0.889     normal           
  [Low4/32]BCFN(2+14,13-6,T)        R=  +2.2  p = 0.172     normal           
  [Low4/32]BCFN(2+15,13-7,T)        R=  -0.4  p = 0.511     normal           
  [Low4/32]BCFN(2+16,13-8,T)        R=  -3.0  p = 0.948     normal           
  [Low4/32]BCFN(2+17,13-8,T)        R=  -2.6  p = 0.898     normal           
  [Low4/32]BCFN(2+18,13-9,T)        R=  -2.8  p = 0.961     normal           
  [Low4/32]BCFN(2+19,13-9,T)        R=  -1.5  p = 0.739     normal           
  [Low4/32]DC6-9x1Bytes-1           R=  +2.0  p = 0.183     normal           
  [Low4/32]Gap-16:A                 R=  +3.3  p = 0.016     normal           
  [Low4/32]Gap-16:B                 R=  +3.0  p = 0.018     normal           
  [Low4/32]FPF-14+6/16:(0,14-0)     R=  -1.2  p = 0.812     normal           
  [Low4/32]FPF-14+6/16:(1,14-0)     R=  -1.0  p = 0.752     normal           
  [Low4/32]FPF-14+6/16:(2,14-0)     R=  +1.5  p = 0.147     normal           
  [Low4/32]FPF-14+6/16:(3,14-0)     R=  +1.3  p = 0.189     normal           
  [Low4/32]FPF-14+6/16:(4,14-0)     R=  +1.1  p = 0.216     normal           
  [Low4/32]FPF-14+6/16:(5,14-0)     R=  +1.0  p = 0.245     normal           
  [Low4/32]FPF-14+6/16:(6,14-0)     R=  -0.6  p = 0.670     normal           
  [Low4/32]FPF-14+6/16:(7,14-0)     R=  -1.5  p = 0.855     normal           
  [Low4/32]FPF-14+6/16:(8,14-0)     R=  +1.1  p = 0.220     normal           
  [Low4/32]FPF-14+6/16:(9,14-1)     R=  -1.1  p = 0.795     normal           
  [Low4/32]FPF-14+6/16:(10,14-2)    R=  -0.1  p = 0.517     normal           
  [Low4/32]FPF-14+6/16:(11,14-2)    R=  +2.0  p = 0.086     normal           
  [Low4/32]FPF-14+6/16:(12,14-3)    R=  +1.4  p = 0.170     normal           
  [Low4/32]FPF-14+6/16:(13,14-4)    R=  +1.9  p = 0.100     normal           
  [Low4/32]FPF-14+6/16:(14,14-5)    R=  +0.5  p = 0.358     normal           
  [Low4/32]FPF-14+6/16:(15,14-5)    R=  +0.5  p = 0.352     normal           
  [Low4/32]FPF-14+6/16:(16,14-6)    R=  -1.3  p = 0.825     normal           
  [Low4/32]FPF-14+6/16:(17,14-7)    R=  +0.7  p = 0.314     normal           
  [Low4/32]FPF-14+6/16:(18,14-8)    R=  -0.7  p = 0.689     normal           
  [Low4/32]FPF-14+6/16:(19,14-8)    R=  -0.2  p = 0.524     normal           
  [Low4/32]FPF-14+6/16:(20,14-9)    R=  -0.8  p = 0.700     normal           
  [Low4/32]FPF-14+6/16:(21,14-10)   R=  -1.8  p = 0.925     normal           
  [Low4/32]FPF-14+6/16:(22,14-11)   R=  +1.8  p = 0.114     normal           
  [Low4/32]FPF-14+6/16:(23,14-11)   R=  -0.2  p = 0.473     normal           
  [Low4/32]FPF-14+6/16:all          R=  +0.8  p = 0.294     normal           
  [Low4/32]FPF-14+6/16:cross        R=  -1.0  p = 0.851     normal           
  [Low4/32]BRank(12):128(8)         R=  +0.1  p~= 0.450     normal           
  [Low4/32]BRank(12):256(8)         R=  +1.6  p~= 0.060     normal           
  [Low4/32]BRank(12):384(2)         R=  -1.0  p~= 0.744     normal           
  [Low4/32]BRank(12):512(4)         R=  -1.4  p~= 0.890     normal           
  [Low4/32]BRank(12):768(1)         R=  +0.4  p~= 0.366     normal           
  [Low4/32]BRank(12):1K(4)          R=  -0.8  p~= 0.670     normal           
  [Low4/32]BRank(12):1536(1)        R=  -0.7  p~= 0.689     normal           
  [Low4/32]BRank(12):2K(2)          R=  -0.2  p~= 0.554     normal           
  [Low4/32]BRank(12):3K(1)          R=  -0.7  p~= 0.689     normal           
  [Low4/32]BRank(12):4K(2)          R=  -0.2  p~= 0.554     normal           
  [Low4/32]BRank(12):6K(1)          R=  -0.7  p~= 0.689     normal           
  [Low4/32]mod3n(5):(0,9-0)         R=  -1.2  p = 0.729     normal           
  [Low4/32]mod3n(5):(1,9-0)         R=  -2.7  p = 0.912     normal           
  [Low4/32]mod3n(5):(2,9-0)         R=  -0.1  p = 0.528     normal           
  [Low4/32]mod3n(5):(3,9-0)         R=  +1.2  p = 0.283     normal           
  [Low4/32]mod3n(5):(4,9-0)         R=  +3.6  p = 0.039     normal           
  [Low4/32]mod3n(5):(5,9-1)         R=  -3.8  p = 0.974     normal           
  [Low4/32]mod3n(5):(6,9-1)         R=  +2.1  p = 0.145     normal           
  [Low4/32]mod3n(5):(7,9-2)         R=  -0.1  p = 0.519     normal           
  [Low4/32]mod3n(5):(8,9-2)         R=  +0.7  p = 0.351     normal           
  [Low4/32]mod3n(5):(9,9-3)         R=  +0.6  p = 0.367     normal           
  [Low4/32]mod3n(5):(10,9-3)        R=  +1.1  p = 0.289     normal           
  [Low4/32]mod3n(5):(11,9-4)        R=  +0.7  p = 0.354     normal           
  [Low4/32]mod3n(5):(12,9-4)        R=  +1.1  p = 0.281     normal           
  [Low4/32]mod3n(5):(13,9-5)        R=  -1.4  p = 0.752     normal           
  [Low4/32]mod3n(5):(14,9-5)        R=  +0.1  p = 0.450     normal           
  [Low4/32]mod3n(5):(15,9-6)        R=  -1.6  p = 0.798     normal           
  [Low4/32]TMFn(2+0):wl             R=  -2.0  p~= 0.8       normal           
  [Low4/32]TMFn(2+1):wl             R=  -0.6  p~= 0.6       normal           
  [Low4/32]TMFn(2+2):wl             R=  +2.0  p~= 0.2       normal           
  [Low4/32]TMFn(2+3):wl             R=  +1.6  p~= 0.3       normal           
  [Low4/32]TMFn(2+4):wl             R=  -3.0  p~= 0.8       normal           
  [Low4/32]TMFn(2+5):wl             R=  -0.0  p~= 0.5       normal           
  [Low4/32]TMFn(2+6):wl             R=  -1.2  p~= 0.7       normal           
  [Low4/32]TMFn(2+7):wl             R=  +1.7  p~= 0.3       normal           
  [Low4/32]TMFn(2+8):wl             R=  +2.5  p~= 0.2       normal           
  [Low4/64]BCFN(2+0,13-0,T)         R=  +1.2  p = 0.307     normal           
  [Low4/64]BCFN(2+1,13-0,T)         R=  -5.5  p = 0.989     normal           
  [Low4/64]BCFN(2+2,13-0,T)         R=  -2.5  p = 0.847     normal           
  [Low4/64]BCFN(2+3,13-0,T)         R=  +1.3  p = 0.294     normal           
  [Low4/64]BCFN(2+4,13-1,T)         R=  +2.1  p = 0.198     normal           
  [Low4/64]BCFN(2+5,13-1,T)         R=  -0.9  p = 0.635     normal           
  [Low4/64]BCFN(2+6,13-2,T)         R=  -3.0  p = 0.894     normal           
  [Low4/64]BCFN(2+7,13-3,T)         R=  -1.7  p = 0.746     normal           
  [Low4/64]BCFN(2+8,13-3,T)         R=  +0.2  p = 0.453     normal           
  [Low4/64]BCFN(2+9,13-4,T)         R=  -3.1  p = 0.908     normal           
  [Low4/64]BCFN(2+10,13-5,T)        R=  -1.7  p = 0.746     normal           
  [Low4/64]BCFN(2+11,13-5,T)        R=  -2.0  p = 0.794     normal           
  [Low4/64]BCFN(2+12,13-6,T)        R=  -1.1  p = 0.651     normal           
  [Low4/64]BCFN(2+13,13-6,T)        R=  -2.0  p = 0.790     normal           
  [Low4/64]BCFN(2+14,13-7,T)        R=  -1.4  p = 0.708     normal           
  [Low4/64]BCFN(2+15,13-8,T)        R=  +1.6  p = 0.213     normal           
  [Low4/64]BCFN(2+16,13-8,T)        R=  -0.4  p = 0.506     normal           
  [Low4/64]BCFN(2+17,13-9,T)        R=  +1.4  p = 0.212     normal           
  [Low4/64]BCFN(2+18,13-9,T)        R=  +4.4  p = 0.046     normal           
  [Low4/64]DC6-9x1Bytes-1           R=  -0.6  p = 0.699     normal           
  [Low4/64]Gap-16:A                 R=  +1.4  p = 0.220     normal           
  [Low4/64]Gap-16:B                 R=  +4.6  p =  7.2e-4   normalish       
  [Low4/64]FPF-14+6/16:(0,14-0)     R=  -0.5  p = 0.635     normal           
  [Low4/64]FPF-14+6/16:(1,14-0)     R=  +1.7  p = 0.121     normal           
  [Low4/64]FPF-14+6/16:(2,14-0)     R=  -1.7  p = 0.892     normal           
  [Low4/64]FPF-14+6/16:(3,14-0)     R=  -0.9  p = 0.731     normal           
  [Low4/64]FPF-14+6/16:(4,14-0)     R=  +3.2  p = 0.012     normal           
  [Low4/64]FPF-14+6/16:(5,14-0)     R=  -1.0  p = 0.764     normal           
  [Low4/64]FPF-14+6/16:(6,14-0)     R=  +1.5  p = 0.145     normal           
  [Low4/64]FPF-14+6/16:(7,14-0)     R=  +1.2  p = 0.208     normal           
  [Low4/64]FPF-14+6/16:(8,14-1)     R=  +0.2  p = 0.461     normal           
  [Low4/64]FPF-14+6/16:(9,14-2)     R=  -1.4  p = 0.845     normal           
  [Low4/64]FPF-14+6/16:(10,14-2)    R=  +1.5  p = 0.143     normal           
  [Low4/64]FPF-14+6/16:(11,14-3)    R=  -0.6  p = 0.678     normal           
  [Low4/64]FPF-14+6/16:(12,14-4)    R=  +0.4  p = 0.382     normal           
  [Low4/64]FPF-14+6/16:(13,14-5)    R=  -2.0  p = 0.928     normal           
  [Low4/64]FPF-14+6/16:(14,14-5)    R=  +0.1  p = 0.464     normal           
  [Low4/64]FPF-14+6/16:(15,14-6)    R=  +0.4  p = 0.392     normal           
  [Low4/64]FPF-14+6/16:(16,14-7)    R=  -0.5  p = 0.633     normal           
  [Low4/64]FPF-14+6/16:(17,14-8)    R=  -1.9  p = 0.917     normal           
  [Low4/64]FPF-14+6/16:(18,14-8)    R=  -2.2  p = 0.950     normal           
  [Low4/64]FPF-14+6/16:(19,14-9)    R=  +0.1  p = 0.437     normal           
  [Low4/64]FPF-14+6/16:(20,14-10)   R=  -0.9  p = 0.721     normal           
  [Low4/64]FPF-14+6/16:(21,14-11)   R=  +0.8  p = 0.248     normal           
  [Low4/64]FPF-14+6/16:(22,14-11)   R=  +3.5  p = 0.023     normal           
  [Low4/64]FPF-14+6/16:all          R=  +1.0  p = 0.261     normal           
  [Low4/64]FPF-14+6/16:cross        R=  -1.1  p = 0.867     normal           
  [Low4/64]BRank(12):128(8)         R=  +0.5  p~= 0.300     normal           
  [Low4/64]BRank(12):256(4)         R=  -0.2  p~= 0.500     normal           
  [Low4/64]BRank(12):384(2)         R=  +0.8  p~= 0.293     normal           
  [Low4/64]BRank(12):512(4)         R=  -1.4  p~= 0.890     normal           
  [Low4/64]BRank(12):768(1)         R=  +0.4  p~= 0.366     normal           
  [Low4/64]BRank(12):1K(2)          R=  -1.0  p~= 0.744     normal           
  [Low4/64]BRank(12):1536(1)        R=  +0.4  p~= 0.366     normal           
  [Low4/64]BRank(12):2K(2)          R=  -1.0  p~= 0.744     normal           
  [Low4/64]BRank(12):3K(1)          R=  +0.4  p~= 0.366     normal           
  [Low4/64]BRank(12):4K(1)          R=  -0.7  p~= 0.689     normal           
  [Low4/64]mod3n(5):(0,9-0)         R=  -0.1  p = 0.514     normal           
  [Low4/64]mod3n(5):(1,9-0)         R=  +0.7  p = 0.358     normal           
  [Low4/64]mod3n(5):(2,9-0)         R=  -0.1  p = 0.515     normal           
  [Low4/64]mod3n(5):(3,9-0)         R=  +0.1  p = 0.489     normal           
  [Low4/64]mod3n(5):(4,9-1)         R=  +1.9  p = 0.173     normal           
  [Low4/64]mod3n(5):(5,9-1)         R=  -0.8  p = 0.654     normal           
  [Low4/64]mod3n(5):(6,9-2)         R=  +0.8  p = 0.346     normal           
  [Low4/64]mod3n(5):(7,9-2)         R=  +3.3  p = 0.052     normal           
  [Low4/64]mod3n(5):(8,9-3)         R=  -2.2  p = 0.870     normal           
  [Low4/64]mod3n(5):(9,9-3)         R=  -1.4  p = 0.760     normal           
  [Low4/64]mod3n(5):(10,9-4)        R=  +1.0  p = 0.290     normal           
  [Low4/64]mod3n(5):(11,9-4)        R=  +2.5  p = 0.108     normal           
  [Low4/64]mod3n(5):(12,9-5)        R=  +1.4  p = 0.216     normal           
  [Low4/64]mod3n(5):(13,9-5)        R=  +1.1  p = 0.258     normal           
  [Low4/64]mod3n(5):(14,9-6)        R=  +0.6  p = 0.323     normal           
  [Low4/64]mod3n(5):(15,9-6)        R=  +0.3  p = 0.376     normal           
  [Low4/64]TMFn(2+0):wl             R=  -1.8  p~= 0.7       normal           
  [Low4/64]TMFn(2+1):wl             R=  +0.1  p~= 0.5       normal           
  [Low4/64]TMFn(2+2):wl             R=  +1.0  p~= 0.4       normal           
  [Low4/64]TMFn(2+3):wl             R=  -3.0  p~= 0.8       normal           
  [Low4/64]TMFn(2+4):wl             R=  -0.3  p~= 0.5       normal           
  [Low4/64]TMFn(2+5):wl             R=  -0.6  p~= 0.6       normal           
  [Low4/64]TMFn(2+6):wl             R=  +2.5  p~= 0.2       normal           
  [Low4/64]TMFn(2+7):wl             R=  +4.3  p~= 0.1       normal           
  [Low8/32]BCFN(2+0,13-0,T)         R=  -2.5  p = 0.846     normal           
  [Low8/32]BCFN(2+1,13-0,T)         R=  +2.1  p = 0.192     normal           
  [Low8/32]BCFN(2+2,13-0,T)         R=  +1.4  p = 0.286     normal           
  [Low8/32]BCFN(2+3,13-0,T)         R=  +1.9  p = 0.217     normal           
  [Low8/32]BCFN(2+4,13-0,T)         R=  +0.3  p = 0.442     normal           
  [Low8/32]BCFN(2+5,13-0,T)         R=  -1.4  p = 0.719     normal           
  [Low8/32]BCFN(2+6,13-1,T)         R=  +1.5  p = 0.267     normal           
  [Low8/32]BCFN(2+7,13-1,T)         R=  +3.1  p = 0.108     normal           
  [Low8/32]BCFN(2+8,13-2,T)         R=  +0.9  p = 0.347     normal           
  [Low8/32]BCFN(2+9,13-3,T)         R=  +5.8  p = 0.013     normal           
  [Low8/32]BCFN(2+10,13-3,T)        R=  -2.3  p = 0.832     normal           
  [Low8/32]BCFN(2+11,13-4,T)        R=  +2.2  p = 0.183     normal           
  [Low8/32]BCFN(2+12,13-5,T)        R=  -2.8  p = 0.881     normal           
  [Low8/32]BCFN(2+13,13-5,T)        R=  -1.1  p = 0.658     normal           
  [Low8/32]BCFN(2+14,13-6,T)        R=  -0.1  p = 0.478     normal           
  [Low8/32]BCFN(2+15,13-6,T)        R=  +2.6  p = 0.142     normal           
  [Low8/32]BCFN(2+16,13-7,T)        R=  -2.3  p = 0.844     normal           
  [Low8/32]BCFN(2+17,13-8,T)        R=  +1.7  p = 0.206     normal           
  [Low8/32]BCFN(2+18,13-8,T)        R=  -0.6  p = 0.538     normal           
  [Low8/32]BCFN(2+19,13-9,T)        R=  -1.9  p = 0.813     normal           
  [Low8/32]BCFN(2+20,13-9,T)        R=  -1.2  p = 0.673     normal           
  [Low8/32]DC6-9x1Bytes-1           R=  -0.0  p = 0.542     normal           
  [Low8/32]Gap-16:A                 R=  -0.2  p = 0.627     normal           
  [Low8/32]Gap-16:B                 R=  +0.1  p = 0.470     normal           
  [Low8/32]FPF-14+6/16:(0,14-0)     R=  +0.8  p = 0.281     normal           
  [Low8/32]FPF-14+6/16:(1,14-0)     R=  -1.6  p = 0.874     normal           
  [Low8/32]FPF-14+6/16:(2,14-0)     R=  -2.7  p = 0.973     normal           
  [Low8/32]FPF-14+6/16:(3,14-0)     R=  +1.4  p = 0.157     normal           
  [Low8/32]FPF-14+6/16:(4,14-0)     R=  +0.3  p = 0.428     normal           
  [Low8/32]FPF-14+6/16:(5,14-0)     R=  -1.1  p = 0.774     normal           
  [Low8/32]FPF-14+6/16:(6,14-0)     R=  +0.7  p = 0.317     normal           
  [Low8/32]FPF-14+6/16:(7,14-0)     R=  -0.1  p = 0.516     normal           
  [Low8/32]FPF-14+6/16:(8,14-0)     R=  -1.9  p = 0.916     normal           
  [Low8/32]FPF-14+6/16:(9,14-0)     R=  +1.5  p = 0.152     normal           
  [Low8/32]FPF-14+6/16:(10,14-1)    R=  -1.1  p = 0.795     normal           
  [Low8/32]FPF-14+6/16:(11,14-2)    R=  +0.1  p = 0.477     normal           
  [Low8/32]FPF-14+6/16:(12,14-2)    R=  -1.6  p = 0.876     normal           
  [Low8/32]FPF-14+6/16:(13,14-3)    R=  -1.5  p = 0.857     normal           
  [Low8/32]FPF-14+6/16:(14,14-4)    R=  -0.5  p = 0.645     normal           
  [Low8/32]FPF-14+6/16:(15,14-5)    R=  +1.2  p = 0.201     normal           
  [Low8/32]FPF-14+6/16:(16,14-5)    R=  +1.2  p = 0.205     normal           
  [Low8/32]FPF-14+6/16:(17,14-6)    R=  -1.8  p = 0.898     normal           
  [Low8/32]FPF-14+6/16:(18,14-7)    R=  -0.1  p = 0.520     normal           
  [Low8/32]FPF-14+6/16:(19,14-8)    R=  +0.8  p = 0.278     normal           
  [Low8/32]FPF-14+6/16:(20,14-8)    R=  -0.4  p = 0.591     normal           
  [Low8/32]FPF-14+6/16:(21,14-9)    R=  +1.9  p = 0.097     normal           
  [Low8/32]FPF-14+6/16:(22,14-10)   R=  -0.1  p = 0.489     normal           
  [Low8/32]FPF-14+6/16:(23,14-11)   R=  +0.5  p = 0.314     normal           
  [Low8/32]FPF-14+6/16:(24,14-11)   R=  +0.0  p = 0.426     normal           
  [Low8/32]FPF-14+6/16:all          R=  -1.3  p = 0.840     normal           
  [Low8/32]FPF-14+6/16:cross        R=  +0.9  p = 0.180     normal           
  [Low8/32]BRank(12):128(8)         R=  +0.5  p~= 0.300     normal           
  [Low8/32]BRank(12):256(8)         R=  +0.6  p~= 0.250     normal           
  [Low8/32]BRank(12):384(2)         R=  -1.0  p~= 0.744     normal           
  [Low8/32]BRank(12):512(4)         R=  -1.4  p~= 0.890     normal           
  [Low8/32]BRank(12):768(2)         R=  -0.2  p~= 0.554     normal           
  [Low8/32]BRank(12):1K(4)          R=  -0.8  p~= 0.670     normal           
  [Low8/32]BRank(12):1536(1)        R=  -0.7  p~= 0.689     normal           
  [Low8/32]BRank(12):2K(2)          R=  +0.8  p~= 0.293     normal           
  [Low8/32]BRank(12):3K(1)          R=  -0.7  p~= 0.689     normal           
  [Low8/32]BRank(12):4K(2)          R=  -0.2  p~= 0.554     normal           
  [Low8/32]BRank(12):6K(1)          R=  -0.7  p~= 0.689     normal           
  [Low8/32]BRank(12):8K(1)          R=  +0.4  p~= 0.366     normal           
  [Low8/32]mod3n(5):(0,9-0)         R=  -0.6  p = 0.615     normal           
  [Low8/32]mod3n(5):(1,9-0)         R=  -0.0  p = 0.512     normal           
  [Low8/32]mod3n(5):(2,9-0)         R=  -0.0  p = 0.504     normal           
  [Low8/32]mod3n(5):(3,9-0)         R=  +0.6  p = 0.377     normal           
  [Low8/32]mod3n(5):(4,9-0)         R=  +2.0  p = 0.163     normal           
  [Low8/32]mod3n(5):(5,9-0)         R=  +0.8  p = 0.339     normal           
  [Low8/32]mod3n(5):(6,9-1)         R=  +0.2  p = 0.455     normal           
  [Low8/32]mod3n(5):(7,9-1)         R=  -0.6  p = 0.612     normal           
  [Low8/32]mod3n(5):(8,9-2)         R=  -1.3  p = 0.744     normal           
  [Low8/32]mod3n(5):(9,9-2)         R=  -0.1  p = 0.510     normal           
  [Low8/32]mod3n(5):(10,9-3)        R=  -1.7  p = 0.796     normal           
  [Low8/32]mod3n(5):(11,9-3)        R=  +2.8  p = 0.087     normal           
  [Low8/32]mod3n(5):(12,9-4)        R=  +0.9  p = 0.315     normal           
  [Low8/32]mod3n(5):(13,9-4)        R=  -1.0  p = 0.676     normal           
  [Low8/32]mod3n(5):(14,9-5)        R=  -3.5  p = 0.982     normal           
  [Low8/32]mod3n(5):(15,9-5)        R=  -1.8  p = 0.827     normal           
  [Low8/32]TMFn(2+0):wl             R=  +0.7  p~= 0.4       normal           
  [Low8/32]TMFn(2+1):wl             R=  +0.5  p~= 0.4       normal           
  [Low8/32]TMFn(2+2):wl             R=  +0.5  p~= 0.4       normal           
  [Low8/32]TMFn(2+3):wl             R=  +2.5  p~= 0.2       normal           
  [Low8/32]TMFn(2+4):wl             R=  +0.3  p~= 0.4       normal           
  [Low8/32]TMFn(2+5):wl             R=  -3.7  p~= 0.9       normal           
  [Low8/32]TMFn(2+6):wl             R=  -0.4  p~= 0.6       normal           
  [Low8/32]TMFn(2+7):wl             R=  +1.1  p~= 0.3       normal           
  [Low8/32]TMFn(2+8):wl             R=  -0.8  p~= 0.6       normal           
  [Low8/32]TMFn(2+9):wl             R=  +1.4  p~= 0.3       normal           
  [Low8/64]BCFN(2+0,13-0,T)         R=  -3.1  p = 0.900     normal           
  [Low8/64]BCFN(2+1,13-0,T)         R=  +0.5  p = 0.419     normal           
  [Low8/64]BCFN(2+2,13-0,T)         R=  -0.3  p = 0.547     normal           
  [Low8/64]BCFN(2+3,13-0,T)         R=  +3.1  p = 0.101     normal           
  [Low8/64]BCFN(2+4,13-0,T)         R=  +2.3  p = 0.170     normal           
  [Low8/64]BCFN(2+5,13-1,T)         R=  -1.8  p = 0.762     normal           
  [Low8/64]BCFN(2+6,13-1,T)         R=  -1.7  p = 0.753     normal           
  [Low8/64]BCFN(2+7,13-2,T)         R=  -0.6  p = 0.592     normal           
  [Low8/64]BCFN(2+8,13-3,T)         R=  +0.3  p = 0.439     normal           
  [Low8/64]BCFN(2+9,13-3,T)         R=  +3.8  p = 0.067     normal           
  [Low8/64]BCFN(2+10,13-4,T)        R=  -0.9  p = 0.630     normal           
  [Low8/64]BCFN(2+11,13-5,T)        R=  -1.1  p = 0.657     normal           
  [Low8/64]BCFN(2+12,13-5,T)        R=  -1.1  p = 0.653     normal           
  [Low8/64]BCFN(2+13,13-6,T)        R=  +0.4  p = 0.406     normal           
  [Low8/64]BCFN(2+14,13-6,T)        R=  -3.6  p = 0.953     normal           
  [Low8/64]BCFN(2+15,13-7,T)        R=  -2.0  p = 0.795     normal           
  [Low8/64]BCFN(2+16,13-8,T)        R=  -2.2  p = 0.837     normal           
  [Low8/64]BCFN(2+17,13-8,T)        R=  -0.5  p = 0.516     normal           
  [Low8/64]BCFN(2+18,13-9,T)        R=  +1.0  p = 0.267     normal           
  [Low8/64]BCFN(2+19,13-9,T)        R=  -0.5  p = 0.506     normal           
  [Low8/64]DC6-9x1Bytes-1           R=  -1.7  p = 0.867     normal           
  [Low8/64]Gap-16:A                 R=  -0.7  p = 0.739     normal           
  [Low8/64]Gap-16:B                 R=  +1.4  p = 0.162     normal           
  [Low8/64]FPF-14+6/16:(0,14-0)     R=  -0.9  p = 0.728     normal           
  [Low8/64]FPF-14+6/16:(1,14-0)     R=  -1.3  p = 0.820     normal           
  [Low8/64]FPF-14+6/16:(2,14-0)     R=  +1.9  p = 0.093     normal           
  [Low8/64]FPF-14+6/16:(3,14-0)     R=  -1.2  p = 0.797     normal           
  [Low8/64]FPF-14+6/16:(4,14-0)     R=  -0.8  p = 0.726     normal           
  [Low8/64]FPF-14+6/16:(5,14-0)     R=  -1.9  p = 0.912     normal           
  [Low8/64]FPF-14+6/16:(6,14-0)     R=  +2.5  p = 0.041     normal           
  [Low8/64]FPF-14+6/16:(7,14-0)     R=  +0.8  p = 0.290     normal           
  [Low8/64]FPF-14+6/16:(8,14-0)     R=  +1.5  p = 0.145     normal           
  [Low8/64]FPF-14+6/16:(9,14-1)     R=  +1.3  p = 0.176     normal           
  [Low8/64]FPF-14+6/16:(10,14-2)    R=  -0.1  p = 0.526     normal           
  [Low8/64]FPF-14+6/16:(11,14-2)    R=  -0.8  p = 0.709     normal           
  [Low8/64]FPF-14+6/16:(12,14-3)    R=  -0.4  p = 0.600     normal           
  [Low8/64]FPF-14+6/16:(13,14-4)    R=  +0.4  p = 0.380     normal           
  [Low8/64]FPF-14+6/16:(14,14-5)    R=  -0.9  p = 0.744     normal           
  [Low8/64]FPF-14+6/16:(15,14-5)    R=  +1.4  p = 0.170     normal           
  [Low8/64]FPF-14+6/16:(16,14-6)    R=  -0.8  p = 0.718     normal           
  [Low8/64]FPF-14+6/16:(17,14-7)    R=  +0.0  p = 0.473     normal           
  [Low8/64]FPF-14+6/16:(18,14-8)    R=  +0.7  p = 0.293     normal           
  [Low8/64]FPF-14+6/16:(19,14-8)    R=  -2.2  p = 0.954     normal           
  [Low8/64]FPF-14+6/16:(20,14-9)    R=  +1.8  p = 0.114     normal           
  [Low8/64]FPF-14+6/16:(21,14-10)   R=  -0.2  p = 0.520     normal           
  [Low8/64]FPF-14+6/16:(22,14-11)   R=  +2.1  p = 0.087     normal           
  [Low8/64]FPF-14+6/16:(23,14-11)   R=  -1.1  p = 0.773     normal           
  [Low8/64]FPF-14+6/16:all          R=  +0.3  p = 0.423     normal           
  [Low8/64]FPF-14+6/16:cross        R=  -1.0  p = 0.856     normal           
  [Low8/64]BRank(12):128(8)         R=  -0.3  p~= 0.500     normal           
  [Low8/64]BRank(12):256(8)         R=  +0.1  p~= 0.450     normal           
  [Low8/64]BRank(12):384(2)         R=  +0.6  p~= 0.322     normal           
  [Low8/64]BRank(12):512(4)         R=  -0.1  p~= 0.490     normal           
  [Low8/64]BRank(12):768(1)         R=  -0.7  p~= 0.689     normal           
  [Low8/64]BRank(12):1K(4)          R=  -0.8  p~= 0.670     normal           
  [Low8/64]BRank(12):1536(1)        R=  -0.7  p~= 0.689     normal           
  [Low8/64]BRank(12):2K(2)          R=  -0.2  p~= 0.554     normal           
  [Low8/64]BRank(12):3K(1)          R=  +0.4  p~= 0.366     normal           
  [Low8/64]BRank(12):4K(2)          R=  -0.2  p~= 0.554     normal           
  [Low8/64]BRank(12):6K(1)          R=  -0.7  p~= 0.689     normal           
  [Low8/64]mod3n(5):(0,9-0)         R=  +4.7  p = 0.011     normal           
  [Low8/64]mod3n(5):(1,9-0)         R=  -1.2  p = 0.727     normal           
  [Low8/64]mod3n(5):(2,9-0)         R=  -0.3  p = 0.553     normal           
  [Low8/64]mod3n(5):(3,9-0)         R=  +0.9  p = 0.327     normal           
  [Low8/64]mod3n(5):(4,9-0)         R=  +1.6  p = 0.219     normal           
  [Low8/64]mod3n(5):(5,9-1)         R=  +0.2  p = 0.462     normal           
  [Low8/64]mod3n(5):(6,9-1)         R=  -1.4  p = 0.760     normal           
  [Low8/64]mod3n(5):(7,9-2)         R=  +1.0  p = 0.302     normal           
  [Low8/64]mod3n(5):(8,9-2)         R=  +2.8  p = 0.088     normal           
  [Low8/64]mod3n(5):(9,9-3)         R=  -2.8  p = 0.925     normal           
  [Low8/64]mod3n(5):(10,9-3)        R=  -0.4  p = 0.575     normal           
  [Low8/64]mod3n(5):(11,9-4)        R=  -1.6  p = 0.784     normal           
  [Low8/64]mod3n(5):(12,9-4)        R=  -0.8  p = 0.642     normal           
  [Low8/64]mod3n(5):(13,9-5)        R=  -0.2  p = 0.500     normal           
  [Low8/64]mod3n(5):(14,9-5)        R=  +1.9  p = 0.158     normal           
  [Low8/64]mod3n(5):(15,9-6)        R=  +1.0  p = 0.261     normal           
  [Low8/64]TMFn(2+0):wl             R=  -0.7  p~= 0.6       normal           
  [Low8/64]TMFn(2+1):wl             R=  -3.3  p~= 0.8       normal           
  [Low8/64]TMFn(2+2):wl             R=  +3.6  p~= 0.1       normal           
  [Low8/64]TMFn(2+3):wl             R=  +2.3  p~= 0.2       normal           
  [Low8/64]TMFn(2+4):wl             R=  -2.4  p~= 0.8       normal           
  [Low8/64]TMFn(2+5):wl             R=  -1.0  p~= 0.6       normal           
  [Low8/64]TMFn(2+6):wl             R=  +0.6  p~= 0.4       normal           
  [Low8/64]TMFn(2+7):wl             R=  +3.0  p~= 0.2       normal           
  [Low8/64]TMFn(2+8):wl             R=  +2.3  p~= 0.2       normal
````
</details>

<br>

<a href="http://pracrand.sourceforge.net/">PracRand</a> was advertised by pcg-random.com and according to it's docs outperforms dieharder and testu01 in it's analysis. I mainly choose it because it was already prebuild for windows and I did not have to switch to linux to build the other tools myself.  You are more than welcome to go ahead and run benchmarks using <a href="http://simul.iro.umontreal.ca/testu01/tu01.html">TestU01</a> or <a href="https://webhome.phy.duke.edu/~rgb/General/dieharder.php">Dieharder</a> and report back.

### Performance
Performed with OpenJDKs <a href="http://openjdk.java.net/projects/code-tools/jmh/">jmh</a> benchmark harness testing the throughput of `nextInt`. Higher numbers are better
Jmh parameters 
- Single: `java -jar benchmarks.jar -f 5 -tu us -w15`
- Multi:  &nbsp;`java -jar benchmarks.jar -f 5 -tu us -w15 -t 4 -e "Fast|Splittable"`

<pre>
wmic:root\cli>cpu get caption, name, numberofcores, maxclockspeed
Caption                               MaxClockSpeed  Name                                     NumberOfCores
Intel64 Family 6 Model 94 Stepping 3  3301           Intel(R) Core(TM) i5-6600 CPU @ 3.30GHz  4
</pre>

#### Single threading

<table>
<tr>	<th>RNG</th>					<th>Mode</th>	<th>Cnt</th>	<th>Score</th>	<th>Error</th>	<th>Units</th>	</tr>
<tr>	<td>JdkRandom</td>				<td>thrpt</td>	<td>25</td>		<td>104,803</td>	<td>± 0,955</td>	<td>ops/us</td>	</tr>
<tr>	<td>JdkSplittable</td>			<td>thrpt</td>	<td>25</td>		<td>307,956</td>	<td>± 6,684</td>	<td>ops/us</td>	</tr>

<tr>	<td>MersenneTwister</td>		<td>thrpt</td>	<td>25</td>		<td>127,690</td>	<td>± 0,854</td>	<td>ops/us</td>	</tr>
<tr>	<td>MersenneTwisterFast</td>	<td>thrpt</td>	<td>25</td>		<td>163,238</td>	<td>± 2,404</td>	<td>ops/us</td>	</tr>

<tr>	<td>PcgRR</td>					<td>thrpt</td>	<td>25</td>		<td>215,295</td>	<td>± 2,442</td>	<td>ops/us</td>	</tr>
<tr>	<td>PcgRS</td>					<td>thrpt</td>	<td>25</td>		<td>216,910</td>	<td>± 1,022</td>	<td>ops/us</td>	</tr>
<tr>	<td>PcgRSFast</td>				<td>thrpt</td>	<td>25</td>		<td>326,687</td>	<td>± 1,587</td>	<td>ops/us</td>	</tr>
<tr>	<td>PcgRSUFast</td>				<td>thrpt</td>	<td>25</td>		<td>335,159</td>	<td>± 1,381</td>	<td>ops/us</td>	</tr>
</table>

<p align="center">
	<img src="https://user-images.githubusercontent.com/9025925/45755053-9aae7c80-bc1d-11e8-88d6-bbcc07386b56.jpeg">
</p>

Be aware of the axis scales. The PCG family performs really well in single threaded environments. In multithreading the CAS instruction is capping the execution speed. Note that while JdkRandom and PCU's are thread safe they rely on Compare And Swap instructions which, if contested (higher contention) result in a lower throughput (In other words the more threads try to access a thread safe instance the slower it will overall get).


#### Multi threading

<p align="center">
<img src="https://user-images.githubusercontent.com/9025925/45755415-acdcea80-bc1e-11e8-91e2-2ce49eb7d669.jpeg">
</p>

Next take a look at the thread safe implementation under high congestion (the synchronized block, lock or cas instructions are constantly contested). 
As you can see under really high stress the locked version performs the best, but does it really warrant the trade off to be more than 4 times slower
if it does not get raced? Probably not but it has to be decided on a case by case basis.

To avoid object creation overhead a good way to implement high throughput rngs for multi threaded environments is to use make use of the PcgRSFast implementation combined with the `Threadlocal` class instance to be used within executor pools. (TODO link to GA for example)


## Note 
The code is provided as is with no guarantee on correct implementation or liability. Please check it for yourself and if you find any errors open an issue and optionally a pull request. It would be nice if you let me know if the code was to use for your.

