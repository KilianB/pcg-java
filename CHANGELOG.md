# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2017-06-20
### Changed
- The previous version of pcgRandom did not utilize the setSeed method found in Random, due to the 
fact that the rng is depended on 2 variables. The stream number and the seed. Override the setSeed 
method to while prominently nothing that this won't produce
the same instance exactly the same as using the seed in the constructor.
- Generalized Junit test cases to extends a base class

### Added
 - Test casese to cover the new setSeed method
 - setSeed and setSeed persistent

## [1.0.0] - 2018-10-05 Initial release
