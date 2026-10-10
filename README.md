# Threaded Prime Number Search

Finds all primes from 2 to y using x threads. Four variants, one per folder:

| Folder         | Printing                                         | Task division                                       |
|----------------|--------------------------------------------------|-----------------------------------------------------|
| variant1-a1-b1 | [A1] Print immediately (timestamp + thread id)   | [B1] Straight division of range                     |
| variant2-a2-b1 | [A2] Wait for all threads, then print everything | [B1] Straight division of range                     |
| variant3-a1-b2 | [A1] Print immediately (timestamp + thread id)   | [B2] Linear search, threads split divisibility test |
| variant4-a2-b2 | [A2] Wait for all threads, then print everything | [B2] Linear search, threads split divisibility test                     |

Every run prints a timestamp at START and END. 
Each folder is self-contained and builds on its own.

## Requirements
JDK 17 or newer.

## Build and run
Double-click `run-all.bat` to run all four, or `run.bat` inside a variant folder.

Manually, from inside a variant folder:
```
javac --release 17 -d out src\primesearch\*.java
java -cp out primesearch.PrimeSearch
```

## Config
Edit `config.txt` in the variant folder:
```
x = 4
y = 1000
```
x = number of threads (1 to 2147483647), y = inclusive upper limit (2 to 9223372036854775807).