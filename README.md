# Wavelet Tree Compression & Querying System

A Java application implementing a Wavelet Tree for sequence compression and efficient querying, with a graphical interface for processing log files and signal data.

## Features

- Log file processing
- Signal data quantization and processing
- Wavelet Tree construction and structure visualization
- Access, Rank, Select, Quantile, and Range Count queries
- Java Swing graphical user interface
- Compression statistics and data information

## Tech Stack

- Java
- Java Swing
- Data Structures and Algorithms

## Source Files

- `RealTimeWaveletApp.java` — Application entry point and core Wavelet Tree implementation.
- `WaveletController.java` — Coordinates data processing, query operations, and GUI interactions.
- `WaveletGUI.java` — Provides the graphical user interface.

## Prerequisites

- Java JDK installed
- Eclipse IDE or another Java IDE

## How to Run

### Using Eclipse

1. Download the three Java source files from this repository.
2. Create a Java project in Eclipse.
3. Inside the `src` folder, create a package.
4. Place all three `.java` files inside that package.
5. Ensure the source files retain their `package <package name>;` declarations.
6. Open `RealTimeWaveletApp.java`.
7. Right-click and select **Run As → Java Application**.

### Using the Command Line

After downloading the files, place them in a folder, inside a parent source folder.

Compile:

```bash
javac -d out src/<package name>/*.java
```

Run:

```bash
java -cp out <package name>.RealTimeWaveletApp
```

## Supported Queries

| Query | Description |
|---|---|
| Access | Retrieves the element at a given index. |
| Rank | Counts occurrences of a symbol up to a specified index. |
| Select | Finds the position of the k-th occurrence of a symbol. |
| Quantile | Finds the k-th smallest element in a specified range. |
| Range Count | Counts elements within a specified symbol range. |

## Notes

The application uses Java Swing for its GUI. All source files required by the application must be downloaded and compiled together.
