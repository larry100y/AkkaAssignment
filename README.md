Akka Assignment
==============

Update
-----------
- Replaced most of the system.out by Akka built-in logger.
- Allow FileScanner to create multiple FileParser actors.
- Added a Manager actor to manage lifecycle and log result to file (it has a predefined name 'parse-result').
- Finished the test case. But the actors lack acknowledgement message, and 
are not loose coupled enough (they have the object reference of the manager actor).
So the test case does not cover many situations yet.  

Installation
------------
Use '$gradle build' to build an executable jar in path-to-project/build/libs

How to use
-------
- Place the text files and the executable jar in the same folder.
- The text files must be with file extensions of '.txt'.
- Run the program using 'java -jar <jar-name>.jar'. It requires a Java 8 runtime.
- The program will search the directory where you type your CLI command.

Some of my concerns
---------
There are some places are not done or done well.
- How to determine whether it is a text file, and check of the charset and encoding. 
And the user should be allowed to define the directory where text files are placed by 
some CLI interaction.
- I assume there are multiple FileScanner and FileParser Actors in the program at first.
But it seems they end up in one Aggregator Actor because it should hold the file stream writting 
result to file. There may be a dedicated stream writer actor for this job.
- I think there should be a manager actor managing the scanner, parser and aggregator actors. 
It can manage its children actors' behaviors and lifecycle by emitting or receiving some special messages.
- The "writing result to a file" and the test case are not finished yet. Plan to do some refactoring for 
the program along with implementing these features.
