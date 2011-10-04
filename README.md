# Arduino Command Line Interface

This programs wraps up the Arduino IDE and allows invoking the compile
and deploy functionality from the command line. It is being used in the
[vim-arduino][vim-arduino] plugin. There are some major hacks in here
b/c the IDE isn't really written to be invoked like this. Many of the
utility classes attempt to load swing components. Ultimately things
could be improved by refactoring and contributing to [Arduino][arduino]
itself.

## Building

This project uses maven. To a full build you can run:

```
mvn clean install
```

## Usage

``` sh
java \
  -Djava.library.path=/Applications/Arduino.app/Contents/Resources/Java \
  -d32 \
  -Darduino.sketchbook=<sketchbook> \
  -Djava.awt.headless=true \
  -jar vim-arduino-cli.jar (-c|-d) <pde_file> <port> <board>
```
### Required Parameters

`sketchbook :` Location of your Arduino sketchbook (used for finding custom libraries).

`-c         :` Compile the sketch.

`-d         :` Compile and deploy the sketch.

`pde_file   :` the \*.pde arduino sketch.

`port       :` the comm port to talk to the board over (`ls /dev/tty.\*` to
see yours)

`board      :` one of the boards defined in boards.txt (part of the Arduino install)

## Requirements

You must have the [Arduino][a] IDE installed. Currently only
Version 0022 is supported.


[vim-arduino]: https://github.com/tclem/vim-arduino
[arduino]: https://github.com/arduino/Arduino
[a]: http://arduino.cc/en/Main/Software
