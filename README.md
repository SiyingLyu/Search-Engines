# Search-Engines

After setting up Java environment (JDK), the file can be compiled.

To compile the code, type:
javac WebSearch.java

After compiling, to run the code, type:
java WebSearch directoryName searchStrategyName

where <directoryName> is the name of corresponding intranet folder (intranet1, intranet5, or intranet7)
and <searchStrategyName> is one of {breadth, depth, best, beam}.
ex: java WebSearch intranet1 breadth
