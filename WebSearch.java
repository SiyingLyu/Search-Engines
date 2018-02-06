import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.Comparator;




// You should call this code as follows:
// java WebSearch directoryName searchStrategyName (or jview, in J++)
//
// where <directoryName> is the name of corresponding intranet folder (internet1, internet5, or internet7)
// and <searchStrategyName> is one of {breadth, depth, best, beam}.
// ex: java WebSearch intranet1 breadth

// The PARTIAL code below contains code for FETCHING and PARSING
// the simple web pages we're using, as well as the fragments of
// a solution.  BE SURE TO READ ALL THE COMMENTS.

// the only requirement is that your main class be called WebSearch
// and that it accept the two arguments described above.

public class WebSearch 
{
	static LinkedList<SearchNode> OPEN; // Feel free to choose your own data structures for searching,
	static HashSet<String> CLOSED;      // and be sure to read documentation about them.
	static LinkedList<SearchNode> subOpen;

	static final boolean DEBUGGING = false; // When set to TRUE, the program will report what's happening.
                                                // WARNING: lots of info is printed.

	static int beamWidth = 10; // If searchStrategy = "beam",
	// limit the size of OPEN to this value.
	// The setSize() method in the Vector
	// class can be used to accomplish this.

	static final String START_NODE     = "page1.html";  // The starting file of the search.

	// A web page is a goal node if it includes the following string.        
	static final String GOAL_PATTERN   = "QUERY1 QUERY2 QUERY3 QUERY4";



	public static void main(String args[])
	{ 
		if (args.length != 2)
		{
			System.out.println("You must provide the directoryName and searchStrategyName.  Please try again.");
		}
		else
		{
			String directoryName = args[0]; // Read the folder of webpages to use (put the wepages in directoryName).
			String searchStrategyName = args[1]; // Read the search strategy to use (put the search algorithm name in searchStrategyName).

			if (searchStrategyName.equalsIgnoreCase("breadth") ||
                            searchStrategyName.equalsIgnoreCase("depth")   ||
                            searchStrategyName.equalsIgnoreCase("best")    ||
                            searchStrategyName.equalsIgnoreCase("beam"))
			{
				performSearch(START_NODE, directoryName, searchStrategyName);
			}
			else
			{
				System.out.println("The valid search strategies are:");
				System.out.println(" BREADTH DEPTH BEST BEAM");
			}
		}

		Utilities.waitHere("Press ENTER to exit.");
	}

	static void performSearch(String startNode, String directoryName, String searchStrategy)
	{
		int nodesVisited = 0;

		OPEN   = new LinkedList<SearchNode>();
		CLOSED = new HashSet<String>();
		subOpen = new LinkedList<SearchNode>();


		OPEN.add(new SearchNode(startNode)); //Adding the startNode to the OPEN LinkedList.

		while (!OPEN.isEmpty())
		{
			if (searchStrategy.equalsIgnoreCase("beam")) {
				beamWidth --;
			}
			SearchNode currentNode = pop(OPEN,searchStrategy);
			String currentURL = currentNode.getNodeName();

			nodesVisited++;

			// Go and fetch the contents of this file.
			String contents = Utilities.getFileContents(directoryName + File.separator + currentURL); // Ex: the input could be (Internet1/file1)

			if (isaGoalNode(contents))
			{
                            // Report the solution path found (You might also wish to write a method that
                            // counts the solution-path's length, and then print that number here.)
							System.out.println("goal node has found: " + currentNode.getNodeName());
                            currentNode.reportSolutionPath(currentNode);
                            break;
			}

			// Remember this node was visited.
			CLOSED.add(currentURL);
			addNewChildrenToOPEN(currentNode, contents, searchStrategy);


			// Provide a status report.
			if (DEBUGGING) System.out.println("Nodes visited = " + nodesVisited + " |OPEN| = " + OPEN.size());
		}

		System.out.println(" Visited " + nodesVisited + " nodes, starting @" +
				" " + directoryName + File.separator + startNode +
				", using: " + searchStrategy + " search.");
	}

	// This method reads the page's contents and collects the 'children' nodes (ie, the hyperlinks on this page).
	// The parent node is also passed in so that 'backpointers' can be created (in order to later extract solution paths).
	static void addNewChildrenToOPEN(SearchNode parent, String contents, String searchStrategy)
	{
		// StringTokenizer's are a nice class built into Java.
		// Be sure to read about them in some Java documentation.
		// They are useful when one wants to break up a string into words (tokens).
		StringTokenizer st = new StringTokenizer(contents);
//		String allContents = "";
//		double totalHyperTextH = 0.0;

		while (st.hasMoreTokens())
		{
			String token = st.nextToken();

			// Look for the hyperlinks on the current page.

			// (Lots a print statments and error checks are in here, both as a form of documentation
			// and as a debugging tool should you create your own intranets.)

			// At the start of some hypertext?  Otherwise, ignore this token.
			if (token.equalsIgnoreCase("<A"))
			{
				String hyperlink; // The name of the child node.


				if (DEBUGGING) System.out.println("Encountered a HYPERLINK");

				// Read: HREF = page#.html >

				token = st.nextToken();
				if (!token.equalsIgnoreCase("HREF"))
				{
					System.out.println("Expecting 'HREF' and got: " + token);
				}

				token = st.nextToken();
				if (!token.equalsIgnoreCase("="))
				{
					System.out.println("Expecting '=' and got: " + token);
				}

				// Now we should be at the name of file being linked to.
				hyperlink = st.nextToken();
				if (!hyperlink.startsWith("page"))
				{
					System.out.println("Expecting 'page#.html' and got: " + hyperlink);
				}

				token = st.nextToken();
				if (!token.equalsIgnoreCase(">"))
				{
					System.out.println("Expecting '>' and got: " + token);
				}

				if (DEBUGGING) System.out.println(" - found a link to " + hyperlink);

				//////////////////////////////////////////////////////////////////////
				// Have collected a child node; now have to decide what to do with it.
				//////////////////////////////////////////////////////////////////////



				if (alreadyInOpen(hyperlink))
				{ // If already in OPEN, we'll ignore this hyperlink
					// (Be sure to read the "Technical Note" below.)
					if (DEBUGGING) System.out.println(" - this node is in the OPEN list.");
				}
				else if (CLOSED.contains(hyperlink))
				{ // If already in CLOSED, we'll also ignore this hyperlink.
					if (DEBUGGING) System.out.println(" - this node is in the CLOSED list.");
				} else if (alreadyInsubOpen(hyperlink)) {
					if (DEBUGGING) System.out.println(" - this node is in the subOpen list.");
				}
				else
				{
					// Collect the hypertext if this is a previously unvisited node.
					// (This is only needed for HEURISTIC SEARCH, but collect in
					// all cases for simplicity.)
					String hypertext = ""; // The text associated with this hyperlink.

					do
					{
						token = st.nextToken();
						if (!token.equalsIgnoreCase("</A>")) hypertext += " " + token;
					}
					while (!token.equalsIgnoreCase("</A>"));

					if (DEBUGGING) System.out.println("   with hypertext: " + hypertext);


					//////////////////////////////////////////////////////////////////////
					// At this point, you have a new child (hyperlink) and you have to
					// insert it into OPEN according to the search strategy being used.
					// Your heuristic function for best-first search should accept as 
					// arguments both "hypertext" (ie, the text associated with this 
					// hyperlink) and "contents" (ie, the full text of the current page).
					//////////////////////////////////////////////////////////////////////
					//////////////////////
					//Sally: Children node is already recognized as hyperlink
					//////////////////////
					SearchNode child = new SearchNode(hyperlink);
					child.setParent(parent);
					child.sethypertext(hypertext);


					//System.out.println("here is hyperlink " + hyperlink + " in node: " + parent.getNodeName());
					//System.out.println("This is hypertext (" + hypertext + ") in node: "+ parent.getNodeName());
					//System.out.println("This is hypertext (" + hypertext + ") heuristic "+ parent.hyperTextH(hypertext));
//					child.setH(child.hypertextH(hypertext));
//					child.setH(child.contentH(contents));
					child.setH((child.hypertextH(hypertext)+child.contentH(contents)));
//					System.out.println("This is h(n) in node "+ child.getNodeName()+" "+child.getHvalue() + " With" + child.gethypertext() + " and parent is " + child.getParent().getNodeName());

					// Technical note: in best-first search,
					// if a page contains TWO (or more) links to the SAME page,
					// it is acceptable if only the FIRST one is inserted into OPEN,
					// rather than the better-scoring one.  For simplicity, once a node
					// has been placed in OPEN or CLOSED, we won't worry about the
					// possibility of later finding of higher score for it.
					// Since we are scoring the hypertext POINTING to a page,
					// rather than the web page itself, we are likely to get
					// different scores for given web page.  Ideally, we'd
					// take this into account when sorting OPEN, but you are
					// NOT required to do so (though you certainly are welcome
					// to handle this issue).

					// HINT: read about the insertElementAt() and addElement()
					// methods in the Vector class.
					if (searchStrategy.equalsIgnoreCase("beam")) {
						subOpen.add(child);
					} else {
						OPEN.add(child); //Adding the new child to the OPEN LinkedList.
					}
				}
			}
		}
		if (beamWidth == 0 || OPEN.isEmpty()) {
			OPEN.clear();
			beamWidth = 10;
		}

		if (OPEN.isEmpty() && searchStrategy.equalsIgnoreCase("beam")) {

			for (int i = 0; i < subOpen.size(); i++) {
				OPEN.add(subOpen.get(i));
			}
			subOpen.clear();
		}
		//System.out.println("Here is out while loop print: " + parent.getNodeName());
		//System.out.println(allContents);
		//System.out.println("This is totalhypertextH in node "+ parent.getNodeName()+" "+totalHyperTextH);
		//System.out.println("This is total H in node: "+parent.getNodeName()+ (parent.allContentsH(allContents)+totalHyperTextH));
//		parent.setH((parent.contentH(allContents)+totalHyperTextH));
//		System.out.println("This is Hvalue for node " + parent.getNodeName() + " : " + parent.getHvalue());
	}

	// A GOAL is a page that contains the goalPattern set above.
	static boolean isaGoalNode(String contents)
	{
            return (contents != null && contents.indexOf(GOAL_PATTERN) >= 0);
	}

	// Is this hyperlink already in the OPEN list?
	// This isn't a very efficient way to do a lookup,
	// but its fast enough for this homework.
	// Also, this for-loop structure can be
	// adapted for use when inserting nodes into OPEN
	// according to their heuristic score.
	static boolean alreadyInOpen(String hyperlink)
	{
		int length = OPEN.size();

		for(int i = 0; i < length; i++)
		{
			SearchNode node = OPEN.get(i);
			String oldHyperlink = node.getNodeName();

			if (hyperlink.equalsIgnoreCase(oldHyperlink)) return true;  // Found it.
		}

		return false;  // Not in OPEN.    
	}

	static boolean alreadyInsubOpen(String hyperlink)
	{
		int length = subOpen.size();

		for(int i = 0; i < length; i++)
		{
			SearchNode node = subOpen.get(i);
			String oldHyperlink = node.getNodeName();

			if (hyperlink.equalsIgnoreCase(oldHyperlink)) return true;  // Found it.
		}

		return false;  // Not in subOpen.
	}

	// You can use this to remove the first element from OPEN.
	static SearchNode pop(LinkedList<SearchNode> list, String searchStrategy)
	{
            SearchNode result;
            if (searchStrategy.equalsIgnoreCase("breadth")) {
				result = list.removeFirst();
			} else if (searchStrategy.equalsIgnoreCase("depth")) {
				result = list.removeLast();
			} else if (searchStrategy.equalsIgnoreCase("best")) {
				Collections.sort(list,new Comparable());
				result = list.removeFirst();
			} else {
				Collections.sort(subOpen,new Comparable());
            	result = list.removeFirst();
			}
		return result;
	}
}

//For comparison. Return greatest to smallest.
class Comparable implements Comparator<SearchNode>{

	@Override
	public int compare(SearchNode s1, SearchNode s2) {
		if (s1.getHvalue() < s2.getHvalue()) {
			return 1;
		} else if (s1.getHvalue() == s2.getHvalue()) {
			return 0;
		} else {
			return -1;
		}
	}
}



/////////////////////////////////////////////////////////////////////////////////

// You'll need to design a Search node data structure.

// Note that the above code assumes there is a method called getHvalue()
// that returns (as a double) the heuristic value associated with a search node,
// a method called getNodeName() that returns (as a String)
// the name of the file (eg, "page7.html") associated with this node, and
// a (void) method called reportSolutionPath() that prints the path
// from the start node to the current node represented by the SearchNode instance.
class SearchNode {
	final String nodeName;
	private SearchNode parentNode;
	private double Hvalue;
	private String hypertext;

	public SearchNode(String name) {
		nodeName = name;
	}

	public void setParent(SearchNode parentNode) {
		this.parentNode = parentNode;
	}

	public SearchNode getParent() {
		return parentNode;
	}

	public void reportSolutionPath(SearchNode goalNode) { //Recursion
		System.out.print(goalNode.getNodeName() + "[ QUERY1 QUERY2 QUERY3 QUERY4 ]");
		while (goalNode.getParent() != null) {
			System.out.print(" <-- " + goalNode.getParent().getNodeName());
			goalNode = goalNode.getParent();
		}
	}

	public String getNodeName() {
		return nodeName;
	}


	public double contentH(String content) {
		int allwords = 0;
		int countQuery = 0;
		int firstOccurence;
		if (!content.contains("QUERY")) {
			firstOccurence = 0;
		} else {
			firstOccurence = content.indexOf("QUERY");
		}
		StringTokenizer subst = new StringTokenizer(content);
		while (subst.hasMoreTokens()) {
			allwords++;
			String subtoken = subst.nextToken();
			if (subtoken.contains("QUERY")) {
				countQuery++;
			}
		}
		double heuristic = (double) countQuery / allwords + firstOccurence * 0.1;
		return heuristic;
	}

	public double hypertextH(String hypertext) {
		int allwords = 0;
		int countQuery = 0;
		StringTokenizer subst = new StringTokenizer(hypertext);
		while (subst.hasMoreTokens()) {
			allwords++;
			String subtoken = subst.nextToken();
			if (subtoken.contains("QUERY")) {
				countQuery++;
			}
		}
		double heuristic = (double) countQuery / allwords;
		if (hypertext.contains("QUERY1 QUERY2 QUERY3") || hypertext.contains("QUERY2 QUERY3 QUERY4")) {
			heuristic += 0.4; //Number is arbitory
		} else if (hypertext.contains("QUERY1 QUERY2") || hypertext.contains("QUERY2 QUERY3") || hypertext.contains("QUERY3 QUERY4")) {
			heuristic += 0.3;
		} else if (hypertext.contains("QUERY1 QUERY3") || hypertext.contains("QUERY2 QUERY4")) {
			heuristic += 0.2;
		} else if (hypertext.contains("QUERY1 QUERY4")) {
			heuristic += 0.1;
		}
		return heuristic;
	}

	//The heuristic for the content of each node.
	public void setH(double num) {
		Hvalue = num;
	}

	public double getHvalue() {
		return Hvalue;
	}

	public void sethypertext(String text) {
		this.hypertext = text;
	}

	public String gethypertext() {
		return hypertext;
	}
}


/////////////////////////////////////////////////////////////////////////////////

// Some 'helper' functions follow.  You don't need to understand their internal details.
// Feel free to move this to a separate Java file if you wish.
class Utilities
{
	// In J++, the console window can close up before you read it,
	// so this method can be used to wait until you're ready to proceed.
	public static void waitHere(String msg)
	{
		System.out.println("");
		System.out.println(msg);
		try { System.in.read(); } catch(Exception e) {} // Ignore any errors while reading.
	}

	// This method will read the contents of a file, returning it
	// as a string.  (Don't worry if you don't understand how it works.)
	public static synchronized String getFileContents(String fileName)
	{
		File file = new File(fileName);
		String results = null;

		try
		{
			int length = (int)file.length(), bytesRead;
			byte byteArray[] = new byte[length];

			ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(length);
			FileInputStream       inputStream = new FileInputStream(file);
			bytesRead = inputStream.read(byteArray);
			bytesBuffer.write(byteArray, 0, bytesRead);
			inputStream.close();

			results = bytesBuffer.toString();
		}
		catch(IOException e)
		{
			System.out.println("Exception in getFileContents(" + fileName + "), msg=" + e);
		}

		return results;
	}
}

////////////////////////////////////////////////////////////////////////////////

//For the extract of text in WWW//
//public static String getPlainText(String str) {
//	try {
//		Parser parser = new Parser();
//		parser.setInputHTML(str);
//
//		StringBean sb = new StringBean();
//		sb.setLinks(false);
//		sb.setReplaceNonBreakingSpace(true);
//		sb.setCollapse(true);
//		parser.visitAllNodesWith(sb);
//		str = sb.getStrings();
//	} catch (ParserException e) {
//		log.error(e);
//	}
//	return str;
//}