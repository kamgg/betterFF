# gsFF: Goal Serialisation and Heuristic-Based Goal Ordering

An extension of JavaFF 2.1 exploring how the order in which goals are pursued affects automated planning. Developed by **Kamal Asmatpoor** for a BSc Computer Science dissertation at **King's College London**, supervised by **Dr Amanda Coles**.

**[Read the dissertation (PDF)](http://kamal.codes/static/dissertation.pdf)** — *AI Planning and Goal Serialisation with Heuristic-based Goal Ordering*

## Research overview

An automated planner takes a description of a domain, an initial state and a set of goals, then searches for a sequence of actions that achieves those goals. This project investigates **goal serialisation**: pursuing goals in an ordered sequence, and comparing ways to choose that order.

The implementation extends JavaFF with:

- Goal serialisation with the existing goal order or a random order.
- Ascending and descending goal ordering based on a **Relaxed Planning Graph (RPG)** heuristic.
- Ordering computed before planning, or recomputed during serialisation.
- Benchmark tasks across five planning domains: Rovers, Elevators, Satellite, Driverlog and Freecell.
- A Python experiment script and recorded performance data for comparing planning time and plan length.

The main extension is in [`GoalWrapper.java`](gsFF/src/main/java/javaff/serialisation/GoalWrapper.java), with the available strategies defined in [`Heuristic.java`](gsFF/src/main/java/javaff/serialisation/Heuristic.java). See the dissertation for the experimental methodology, results and limitations; the recorded data is a historical research artefact, not a benchmark of current hardware or Java versions.

## Repository layout

| Path | Contents |
| --- | --- |
| [`gsFF/`](gsFF/) | Java planner, Gradle build and experiment runner |
| [`gsFF/test_problems/`](gsFF/test_problems/) | PDDL benchmark domains and problems |
| [`performance_data/`](performance_data/) | Recorded results, graphs and analysis workbook |
| [`doc/`](doc/) | Planning and PDDL reference documents |
| [`res/`](res/) | Bundled planning and validation tools |

## Build

The original project documents **Java 11** and **Gradle 6.4** as prerequisites. There is no Gradle wrapper in this repository, so install Gradle separately. Python 3 is needed only for the experiment script.

From the repository root:

```sh
cd gsFF
gradle clean build
```

The build produces the executable JAR at **`build/libs/gsFF.jar`**, relative to `gsFF/`. It includes the runtime dependencies and uses `javaff.JavaFF` as its entry point.

This is the original research build, including legacy dependency declarations; compatibility with newer Java or Gradle versions is not established here.

## Run a planning problem

Run these examples from the `gsFF/` directory after building.

Baseline JavaFF planning, without goal serialisation:

```sh
java -jar build/libs/gsFF.jar \
  -o test_problems/rovers/domain.pddl \
  -f test_problems/rovers/problems/pfile1.pddl
```

Enable goal serialisation with ascending RPG-based ordering:

```sh
java -jar build/libs/gsFF.jar \
  -g -h RPGASCENDING \
  -o test_problems/rovers/domain.pddl \
  -f test_problems/rovers/problems/pfile1.pddl \
  -w rovers-1.plan
```

### Command-line options

| Option | Description |
| --- | --- |
| `-o`, `--domain <path>` | Required: PDDL domain file |
| `-f`, `--problem <path>` | Required: PDDL problem file |
| `-w`, `--output <path>` | Optional: output plan file |
| `-g`, `--goal-serialisation` | Enable goal serialisation |
| `-h`, `--heuristic <name>` | Goal-ordering strategy; defaults to `NONE` |

Here, `-h` selects a heuristic; it is **not** a help flag. Running without arguments prints usage information. Supply `-g` when using a goal-ordering heuristic.

### Goal-ordering strategies

| CLI value | Strategy | Gradle task suffix |
| --- | --- | --- |
| `NONE` | Existing goal order | `gs` |
| `RANDOM` | Random goal order | `gs-rand` |
| `RPGASCENDING` | RPG-based ascending order, computed before serialisation | `gs-rpg-asc` |
| `RPGDESCENDING` | RPG-based descending order, computed before serialisation | `gs-rpg-desc` |
| `INRPGASCENDING` | RPG-based ascending order, recomputed during serialisation | `gs-in-rpg-asc` |
| `INRPGDESCENDING` | RPG-based descending order, recomputed during serialisation | `gs-in-rpg-desc` |

Heuristic names are case-insensitive in the command-line parser. Random ordering uses an unseeded shuffle; repeated runs can differ.

## Run the benchmark tasks

The Gradle build defines tasks for individual benchmark problems. Run them from `gsFF/`:

```sh
gradle tasks --all
gradle rovers-1
gradle rovers-gs-1
gradle rovers-gs-rpg-asc-1
gradle rovers-gs-rpg-desc-1
gradle elevators-gs-in-rpg-desc-1-0
```

Task names follow these patterns:

```text
<domain>-<problem>                 # baseline, without serialisation
<domain>-<strategy>-<problem>      # serialisation strategy from the table above
```

| Domain | Problem identifiers |
| --- | --- |
| `rovers`, `satellite`, `driverlog` | `1`–`20` |
| `freecell` | `1`–`20`; additional problems `<n>-<m>` with `n` in `2`–`13` and `m` in `1`–`5` |
| `elevators` | `<n>-<m>` with `n` in `1`–`30` and `m` in `0`–`4` |

## Experiment data

[`gsFF/run_tests.py`](gsFF/run_tests.py) is a research experiment runner, not a general-purpose command-line test suite. It invokes Gradle tasks and records plan length and planning times as CSV files.

Before running it:

1. Review the selected domains and strategies at the bottom of the script. The checked-in selection runs the Rovers baseline.
2. Adjust the output parsing in `run_domain` for the Gradle output in your environment; it relies on fixed line positions.
3. Check the result and error directories. The script writes under `test_results/` and `test_errors/`, and can overwrite existing results.

Then, from `gsFF/`:

```sh
python3 run_tests.py
```

The script's configured strategies cover the baseline, existing order, random order and pre-planning RPG variants. The in-planning variants are available through the Java CLI and Gradle tasks but are not included in that script's current strategy map.

Historical analysis is available in [`performance_data/`](performance_data/), including [graphs](performance_data/graphs/) and the [analysis workbook](performance_data/Worksheet.xlsx).

## Attribution and licence

This project builds on the open-source **JavaFF 2.1** planner. Existing source attribution is retained. The planner's licence is included in [`gsFF/LICENSE`](gsFF/LICENSE).
