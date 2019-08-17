Source files included in this folder:
-------------------------------------
Cont2Pres - Content MathML to Presentation MathML
	- Content2Presentation : main objecttakes a Content MathML Document as an arguement
	- conversions : Methods for converting different MathML elements
		* ApplyTags.java      - Converts "apply" elements using the grouping within the text files.
		* Interval.java       - Converts "interval" elements
		* LambdaBind.java     - Converts "lambda" and "bind" elements
		* Numbers.java        - Converts special encodings of "cn" elements (e.g complex, rational etc.)
		* SetsLists.java      - Converts "set" and "list" elements
		* VectorMatrices.java - Converts "vector" and "matrix" elements
	- files : Text files containing list of element names that follow the "apply" element, sorted by mathematical operation groups.
		* BigSymbols          - List of operations that may contatin additional information (e.g. integrals, summations etc.)
		* FenceTags           - List of operations that surround a variable (e.g. absolute values, ceiling etc.)
		* FunctionTags        - List of predefined functions (e.g. sin, log, lowest common multiple etc.)
		* OperationTags       - List of all infix operators (e.g. plus, equals, subset etc.)

Pres2Cont - Presentation MathML to Content MathML
	- Pres2Cont.java              - Main object takes one or more Presentation MathML Documents as arguments 
	- MathInterpretor.java        - Uses the Ambiguities file to produce all interpretations and then converts to Content MathML
	- FindInDoc.java              - Used by MathInterpretor, takes a conversion from conversion file and converts occurances in the Document
	- MatchRow.java               - Used by FindInDoc to match the conversion structure to a subtree in the Document 
	- conversions : 
		* Ambiguities         - List of all Presentation structures with multiple Content MathML interpretations
		* Conversions         - List of all Presentation stuctures and their Content MathML interpretation linked.

*Also included is the Strict2Abstract - Strict Content MathML to Abstract Content MathML - containing the method of conversions.


Libraries included in this folder:
----------------------------------
Cont2Pres.jar - the conversion is completed upon construction of the Content2Presentation object with the arguement of a Document Object Model (DOM):

	Content2Presentation c2p = new Content2Presentation(Document doc);
	
	- To print the result, the command Content2Presentation.print() will return a string of the resulting Presentation MathML.

Pres2Cont.jar - the construction of the Presentation2Content object will take arguments of either a single or list of Document Object Models.

	Presentation2Content p2c = new Presentation2Content(Document doc);
	Presentation2Content p2c = new Presentation2Content(List<Document> docs);

	- To get a List<Document> of the interpretations of the input document use the command Presentation2Content.getInterpretations();
	- To print an interpretation use the command Presentation2Content.print(Document interp);


Using the RunDemo.jar program:
------------------------------
This program is designed to be run in the terminal and demonstrates the use of the two libraries above.
To run the program, you will need to locate the jar file in a command promt terminal. Once located, run 
the command "java -jar RunDemo.java".
You will first be prompted asking which conversion you wish to use.
Next you will be asked whether you wish to use a file upload or a manual text input.
The file upload will bring up a windows explorer where you can select a text file that contains a MathML 
expression. You can select as many files as you wish.

When entering the MathML expression directly into the terminal, you can write it out line by line. Once 
you have finished enter the line :q and the conversion will begin.

The result will then be printed to the terminal.