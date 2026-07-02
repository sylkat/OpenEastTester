# OpenEastTester

Open source desktop software for East Tester LCR meters.

<p align="center">
  <img src="screenshots/openet.gif" alt="ET4 LCR Controller in Action" width="700">
</p>

## Why?

The official software works, but it feels outdated and lacks several features
that make everyday use more comfortable.

This project started as a personal tool for my own ET431, but it has been
designed with the idea of supporting other East Tester LCR meters using the
same serial protocol.

## Features

- Real-time measurements
- Bidirectional synchronization
- Automatic hardware state detection
- Cross-platform (Java)
- Serial communication

## Supported devices

Tested:

- ET431

Expected to work:

- ET43x series
- ET44x series (needs confirmation)

If you own another model, feel free to test it and open an issue.

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
- [ ] Auto-connect to last serial port
- [ ] Support for additional ET4x models
- [ ] Binary releases for Windows/Linux

## License

GPL v3

If you improve the software and distribute your version, please contribute
those improvements back to the community.

## Notes

This project is not affiliated with East Tester.

Bug reports and pull requests are welcome.
