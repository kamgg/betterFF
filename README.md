# JavaFF with Goal Serialisation (gsFF)
Built off of the open-source JavaFF2.1 project as a part of my individual project venturing into Goal Serialisation and Heuristic-based goal ordering.

---
## Building
In order to build the artefact, first install [Gradle 6.4](https://github.com/gradle/gradle/releases/tag/v6.4.0). Instructions can be found [here](https://gradle.org/install/).

In order to build a fresh copy of the artefact, move into the ``/gsFF`` subdirectory:
```
cd gsFF
```
And run:
```
gradle clean && gradle build
```

This will build an executable jar into ``/build/lib``.

---
## Testing

Packaged with the source code is the test script ``run_tests.py`` used to gather performance data. The script has a suite of functions which can be used in varying ways to test domains and record the data produced. Please note that this script is not a command-line tool and must be modified as required prior to execution.