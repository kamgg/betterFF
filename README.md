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
## Usage
Gradle is packaged with tasks for the 5 domains on which the planner tested on. To view all gradle tasks, run:
```
gradle tasks --all
```

The most basic gradle task, which runs the baseline implementation of JavaFF is:
```
gradle <domain>-<problem>
```

To specify heuristics, use the following format:
```
gradle <domain>-<heuristic>-<problem>
```
With the following heuristics:
| Description                 | heuristic         |
|-----------------------------|-------------------| 
| Only goal serialisation     | **gs**            | 
| Random                      | **gs-rand**       |  
| Pre-planning RPG Ascending  | **gs-rpg-asc**    | 
| Pre-planning RPG Descending | **gs-rpg-asc**    |
| In-planning RPG Ascending   | **gs-in-rpg-asc** |
| In-planning RPG Ascending   | **gs-in-rpg-asc** |


For most problems, the ``<problem>`` parameter is a number ranging from 1-20, but in the case of the Elevators domain, the ``<problem>`` parameter is specified as two numbers between 1-30 and 0-4 respectively, separated by a hyphen. For example, ``1-0, 1-4, ..., 30-0, 30-4``.

The Freecell domain also has additional problems which can be specified using a similar format, but with numbers between 2-13 and 1-5 respectively. For example, ``2-1, 2-5, ..., 13-1, 13-5``.

---
## Testing

Packaged with the source code is the test script ``run_tests.py`` used to gather performance data. The script has a suite of functions which can be used in varying ways to test domains and record the data produced. 

**Please note that this script is not a command-line tool and must be modified as required prior to execution.**