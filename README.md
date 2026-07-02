# OpenEastTester

Open source desktop software for East Tester LCR meters.

<p align="center">
  <img src="screenshots/openet.gif" alt="ET4 LCR Controller in Action" width="700">
</p>
<p align="center">
<b>Real-time bidirectional synchronization between the PC application and the instrument.</b>
</p>

## Why?

The official software works, but it feels outdated and lacks several features
that make everyday use more comfortable.

This project started as a personal tool for my ET431, but it has been
designed with the idea of supporting other East Tester LCR meters using the
same serial protocol.

## Features

- Real-time measurements
- Bidirectional synchronization
- Automatic hardware state synchronization
- Serial communication
- Modern desktop interface

## Supported devices

### Tested

- ET431

### Expected to work

- ET43x series
- ET44x series

If you own another model, please let me know whether it works.


## Installation

Download the latest release from GitHub.

Linux:

```bash
sudo apt install ./OpenEastTester.deb
```

Cross-platform:

```bash
java -jar OpenEastTester.jar
```

## Building

Requirements:

- Java 17 or newer
- IntelliJ IDEA (recommended)

Clone the repository and build normally.

## Roadmap

- [x] Real-time measurements
- [x] Bidirectional synchronization
- [x] Automatic hardware state synchronization
- [x] Cross-platform support
- [ ] CSV data logging
- [ ] Measurement graph
- [ ] Support for additional ET4x models
- [ ] Native installers for Windows


## License

Licensed under GPL v3.

If you distribute a modified version of this software, please make your
changes available under the same license.

## Notes

This project is not affiliated with East Tester.

Bug reports and pull requests are welcome.
