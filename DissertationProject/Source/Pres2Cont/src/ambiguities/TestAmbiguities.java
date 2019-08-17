package ambiguities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestAmbiguities {

	public static void main(String[] args) {
		//singlePres2Cont();
		//runAllFiles();
		//getConversions();
		testConversions();
		//changeHex();
	}


	private static void changeHex() {
		File allinput = new File("input");
		for(File inputfolder : allinput.listFiles()) {
			if(inputfolder.getPath().equals("input\\openmath")) {
			inputfolder = new File(inputfolder.getPath()+"/Presentation");
			//File inputfolder = new File("inputOM/Presentation");
			for(File dir : inputfolder.listFiles()) {
				//File dir = new File("input/w3c/Presentation/arith1");
					for(File f : dir.listFiles()) {
						try {
							System.out.println(f.getPath());
							Charset charset = StandardCharsets.UTF_8;
							String content = new String(Files.readAllBytes(f.toPath()), charset);
							
							int start = content.indexOf("&");
							int end = 0;
							while(start > 0) {
								end = content.indexOf(";",start);
								String intStr = content.substring(start+2,end);
								try {
									int num = Integer.parseInt(intStr);
									String hex = "&#x" + Integer.toHexString(num).toUpperCase();
									String pre = content.substring(0, start);
									String post = content.substring(end);
									content = pre + hex + post;
								}catch(Exception e2) {
									
								}
								start = content.indexOf("&", end); 
							}
							
							
							Files.write(f.toPath(), content.getBytes(charset));
						}catch(Exception e) {
							e.printStackTrace();
						}
					}
			}
			}
		}
		
	}


	private static void runAllFiles() {
		String html = "<!DOCTYPE html>\n"  
					+ "<html>\n"
					+ "<head>\n"
					+ "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"></script>\n"
					+ "<style>\n"
					+ "td, th {\n" 
					+ "  border: 1px solid #dddddd;whitespace:pre;}"
					+ "</style>"
					+ "</head>"
					+ "<body>"
					+ "<table>\n";
		File allinput = new File("input");
		for(File inputfolder : allinput.listFiles()) {
		inputfolder = new File(inputfolder.getPath()+"/Presentation");
		//File inputfolder = new File("inputOM/Presentation");
		for(File dir : inputfolder.listFiles()) {
			//File dir = new File("input/w3c/Presentation/arith1");
				for(File f : dir.listFiles()) {
					try {
						if(f.getParentFile().getPath().equals("input\\w3c\\Presentation\\AAAAA")) {
							System.out.println(f.getPath());
							byte[] encoded = Files.readAllBytes(f.toPath());
							String input = new String(encoded, Charset.defaultCharset());
							input = input.replaceAll("&", "&amp;");
							
							DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
							InputSource is = new InputSource();
							is.setCharacterStream(new StringReader(input));
							Document doc = db.parse(is);							
							
							Pres2Cont p2c = new Pres2Cont(doc);
							List<Document> interpretations = p2c.getInterpretations();
							String vars = "";
							
							// For adding conversions to the conversions file
							boolean adding2conversion = false;
							for(Document interp : interpretations) {
								if(adding2conversion) {
									String newConversion = getNewConversion(interp,p2c);
									System.out.println(newConversion);
									if(newConversion != null) {
										Writer output;
										output = new BufferedWriter(new FileWriter("Conversions", true));
										output.append(newConversion);
										output.close();
									}
								}
								
								String v = p2c.print(interp).replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;");
								vars += "<pre>" + v + "</pre>";
							}
							html += "<tr><td>"+p2c.print(p2c.getOriginal())+"</td><td><code>"+ vars +"</code></td></tr>";						
						}
						
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		html += "</table>\n"
				+ "</body>\n"
				+ "</html>";
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter("Demo.html"));
		    	writer.write(html);
			    writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	
	private static String getNewConversion(Document interp, Pres2Cont p2c) {
		Node parent = interp.getFirstChild();
		if(parent.getFirstChild().getNodeName().equals("mrow")&&parent.getChildNodes().getLength() == 1) {
			Node mrow = parent.getFirstChild();
			while(mrow.getFirstChild() != null) {
				parent.appendChild(mrow.getFirstChild());
			}
			parent.removeChild(mrow);
		}
		int varcount = 0;
		List<Node> allNodes = getAllNodes(parent);
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		for(int i = 0; i < allNodes.size(); i ++) {
			Node n = allNodes.get(i);
			System.out.println(n.getNodeName() + " : " + n.getTextContent() + " var?");
			String in = sc.nextLine();
			if(in.equals("y")) {
				varcount++;
				Node child = n.getFirstChild();
				while(child != null) {
					n.removeChild(child);
					child = n.getFirstChild();
				}
				interp.renameNode(n, null, "var");
				n.setTextContent(varcount + "");
				allNodes = getAllNodes(parent);
			}else if (in.equals("x")) {
				return null;
			}
		}
		interp.renameNode(interp.getFirstChild(), null, "pres");
		String pres = p2c.print(interp);
		pres = pres.replaceAll("\\s{2,}", "");
		pres = pres.replaceAll("\n", "");
		pres = pres.replaceAll(" xmlns=\"http://www.w3.org/1998/Math/MathML\" display=\"inline\"", "");
		pres = pres.replaceAll("&", "&amp;");
		pres = presRemoveComments(pres);
		System.out.println(pres + "\nreplacement: ");
		String cont = sc.nextLine().trim();
		if(cont.equals("x")) {
			return null;
		}
		return pres + "%<cont>" + cont + "</cont>%\n";
	}
	
	private static String presRemoveComments(String pres) {
		String r = "";
		int start = pres.indexOf("<!--");
		int end = pres.indexOf(">",start);
		if(pres.indexOf("<!--") < 0) {
			return pres;
		}else {
			r += pres.substring(0, start);
			r += pres.substring(end + 1);
			return presRemoveComments(r);
		}
	}

	private static List<Node> getAllNodes(Node root) {
		List<Node> allNodes = new ArrayList<Node>();
		NodeList rootChildren = root.getChildNodes();
		for(int i = 0; i < rootChildren.getLength(); i++) {
			Node newRoot = rootChildren.item(i);
			if(!newRoot.getNodeName().substring(0, 1).equals("#")) {
				allNodes.add(newRoot);
			}if(newRoot.hasChildNodes()) {
				allNodes.addAll(getAllNodes(newRoot));
			}
		}		
		return allNodes;
	}

	private static void singlePres2Cont() {
		String html = "<!DOCTYPE html>\n"  
					+ "<html>\n"
					+ "<head>\n"
					+ "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"></script>\n"
					+ "<style>\n"
					+ "td, th {\n" 
					+ "  border: 1px solid #dddddd;whitespace:pre;}"
					+ "</style>"
					+ "</head>"
					+ "<body>"
					+ "<table>\n";
		File allinput = new File("input");
		List<Document> docs = new  ArrayList<Document>();
		for(File inputfolder : allinput.listFiles()) {
		inputfolder = new File(inputfolder.getPath()+"/Presentation");
		//File inputfolder = new File("inputOM/Presentation");
		for(File dir : inputfolder.listFiles()) {
			//File dir = new File("input/w3c/Presentation/arith1");
				for(File f : dir.listFiles()) {
					try {
						if(f.getParentFile().getPath().equals("input\\w3c\\Presentation\\arith1")) {
							//System.out.println(f.getPath());
							byte[] encoded = Files.readAllBytes(f.toPath());
							String input = new String(encoded, Charset.defaultCharset());
							input = input.replaceAll("&", "&amp;");
							
							DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
							InputSource is = new InputSource();
							is.setCharacterStream(new StringReader(input));
							Document doc = db.parse(is);							
							docs.add(doc);						
						}
						
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		

		Pres2Cont p2c = new Pres2Cont(docs);
		List<Document> interpretations = p2c.getInterpretations();
		String vars = "";
		for(Document interp : interpretations) {			
			String v = p2c.print(interp).replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;");
			vars += "<pre>" + v + "</pre>";
		}		
		html += "<tr><td>"+p2c.print(p2c.getOriginal())+"</td><td><code>"+ vars +"</code></td></tr>";
		html = html.replace("inline", "block");
		
		html += "</table>\n"
				+ "</body>\n"
				+ "</html>";
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter("Demo.html"));
		    	writer.write(html);
			    writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	

	private static void testConversions() {
		String html = "<!DOCTYPE html>\n"  
				+ "<html>\n"
				+ "<head>\n"
				+ "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"></script>\n"
				+ "<style>\n"
				+ "td, th {\n" 
				+ "  border: 1px solid #dddddd;whitespace:pre;}"
				+ "td.green {\n" 
				+ "  background:GREEN}"
				+ "td.yellow {\n" 
				+ "  background:YELLOW}"
				+ "td.red {\n" 
				+ "  background:RED}"
				+ "/*Now the CSS*/\r\n" + 
				"\r\n" + 
				"* {\r\n" + 
				"  margin: 0;\r\n" + 
				"  padding: 0;\r\n" + 
				"}\r\n" + 
				"/*added*/\r\n" + 
				"\r\n" + 
				".tree {\r\n" + 
				"  white-space: nowrap;\r\n" + 
				"  overflow: auto;\r\n" + 
				"  \r\n" + 
				"}\r\n" + 
				"\r\n" + 
				".tree ul {\r\n" + 
				"  padding-top: 20px;\r\n" + 
				"  position: relative;\r\n" + 
				"  transition: all 0.5s;\r\n" + 
				"  -webkit-transition: all 0.5s;\r\n" + 
				"  -moz-transition: all 0.5s;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				".tree li {\r\n" + 
				"  text-align: center;\r\n" + 
				"  list-style-type: none;\r\n" + 
				"  position: relative;\r\n" + 
				"  padding: 20px 5px 0 5px;\r\n" + 
				"  transition: all 0.5s;\r\n" + 
				"  -webkit-transition: all 0.5s;\r\n" + 
				"  -moz-transition: all 0.5s;\r\n" + 
				"  /*added for long names*/\r\n" + 
				"  \r\n" + 
				"  float: none;\r\n" + 
				"  display: inline-block;\r\n" + 
				"  vertical-align: top;\r\n" + 
				"  white-space: nowrap;\r\n" + 
				"  margin: 0 -2px 0 -2px;\r\n" + 
				"}\r\n" + 
				"/*We will use ::before and ::after to draw the connectors*/\r\n" + 
				"\r\n" + 
				".tree li::before,\r\n" + 
				".tree li::after {\r\n" + 
				"  content: '';\r\n" + 
				"  position: absolute;\r\n" + 
				"  top: 0;\r\n" + 
				"  right: 50%;\r\n" + 
				"  border-top: 1px solid #ccc;\r\n" + 
				"  width: 50%;\r\n" + 
				"  height: 20px;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				".tree li::after {\r\n" + 
				"  right: auto;\r\n" + 
				"  left: 50%;\r\n" + 
				"  border-left: 1px solid #ccc;\r\n" + 
				"}\r\n" + 
				"/*We need to remove left-right connectors from elements without \r\n" + 
				"any siblings*/\r\n" + 
				"\r\n" + 
				".tree li:only-child::after,\r\n" + 
				".tree li:only-child::before {\r\n" + 
				"  display: none;\r\n" + 
				"}\r\n" + 
				"/*Remove space from the top of single children*/\r\n" + 
				"\r\n" + 
				".tree li:only-child {\r\n" + 
				"  padding-top: 0;\r\n" + 
				"}\r\n" + 
				"/*Remove left connector from first child and \r\n" + 
				"right connector from last child*/\r\n" + 
				"\r\n" + 
				".tree li:first-child::before,\r\n" + 
				".tree li:last-child::after {\r\n" + 
				"  border: 0 none;\r\n" + 
				"}\r\n" + 
				"/*Adding back the vertical connector to the last nodes*/\r\n" + 
				"\r\n" + 
				".tree li:last-child::before {\r\n" + 
				"  border-right: 1px solid #ccc;\r\n" + 
				"  border-radius: 0 5px 0 0;\r\n" + 
				"  -webkit-border-radius: 0 5px 0 0;\r\n" + 
				"  -moz-border-radius: 0 5px 0 0;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				".tree li:first-child::after {\r\n" + 
				"  border-radius: 5px 0 0 0;\r\n" + 
				"  -webkit-border-radius: 5px 0 0 0;\r\n" + 
				"  -moz-border-radius: 5px 0 0 0;\r\n" + 
				"}\r\n" + 
				"/*Time to add downward connectors from parents*/\r\n" + 
				"\r\n" + 
				".tree ul ul::before {\r\n" + 
				"  content: '';\r\n" + 
				"  position: absolute;\r\n" + 
				"  top: 0;\r\n" + 
				"  left: 50%;\r\n" + 
				"  border-left: 1px solid #ccc;\r\n" + 
				"  width: 0;\r\n" + 
				"  height: 20px;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				".tree li a {\r\n" + 
				"  border: 1px solid #ccc;\r\n" + 
				"  padding: 5px 10px;\r\n" + 
				"  text-decoration: none;\r\n" + 
				"  color: #666;\r\n" + 
				"  font-family: arial, verdana, tahoma;\r\n" + 
				"  font-size: 11px;\r\n" + 
				"  display: inline-block;\r\n" + 
				"  border-radius: 5px;\r\n" + 
				"  -webkit-border-radius: 5px;\r\n" + 
				"  -moz-border-radius: 5px;\r\n" + 
				"  transition: all 0.5s;\r\n" + 
				"  -webkit-transition: all 0.5s;\r\n" + 
				"  -moz-transition: all 0.5s;\r\n" + 
				"}\r\n" + 
				"/*Time for some hover effects*/\r\n" + 
				"/*We will apply the hover effect the the lineage of the element also*/\r\n" + 
				"\r\n" + 
				".tree li a:hover,\r\n" + 
				".tree li a:hover+ul li a {\r\n" + 
				"  background: #c8e4f8;\r\n" + 
				"  color: #000;\r\n" + 
				"  border: 1px solid #94a0b4;\r\n" + 
				"}\r\n" + 
				"/*Connector styles on hover*/\r\n" + 
				"\r\n" + 
				".tree li a:hover+ul li::after,\r\n" + 
				".tree li a:hover+ul li::before,\r\n" + 
				".tree li a:hover+ul::before,\r\n" + 
				".tree li a:hover+ul ul::before {\r\n" + 
				"  border-color: #94a0b4;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				".husband {\r\n" + 
				"  float: left;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				".wife {\r\n" + 
				"  margin-left: 10px;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				".wife::before {\r\n" + 
				"  /* pseudo CSS, will need to be modified */\r\n" + 
				"  \r\n" + 
				"  content: '';\r\n" + 
				"  position: absolute;\r\n" + 
				"  top: 0;\r\n" + 
				"  right: 50%;\r\n" + 
				"  border-top: 1px solid #ccc;\r\n" + 
				"  width: 50%;\r\n" + 
				"  height: 20px;\r\n" + 
				"}\r\n" + 
				""
				+ "</style>"
				+ "</head>"
				+ "<body>"
				+ "<table>\n";
		File allinput = new File("input");
		int red = 0;
		int yellow = 0;
		int green = 0;
		for(File inputfolder : allinput.listFiles()) {
			if(inputfolder.getPath().equals("input\\w3c")) {
			inputfolder = new File(inputfolder.getPath()+"/Presentation");
			//File inputfolder = new File("inputOM/Presentation");
			for(File dir : inputfolder.listFiles()) {
				//File dir = new File("input/w3c/Presentation/arith1");
					for(File f : dir.listFiles()) {
						try {
							/*if(f.getPath().equals("input\\w3c\\Presentation\\arith1\\plus1"))*/ {
							System.out.println(f.getPath());
							Document presInput = buildDoc(f.getPath());
							List<Node> expected = new ArrayList<Node>();
							Document expectedContent = buildDoc(f.getPath());
							try {
							expectedContent = buildDoc(f.getPath().replaceAll("Presentation", "Content"));
							expected = getAllNodes(expectedContent);
							}catch(NoSuchFileException e) {								
							}
							Pres2Cont p2c = new Pres2Cont(presInput);
							List<Document> docs = p2c.getInterpretations();
							html += "<tr><td>INPUT: " + f.getPath() + "</td><td>OUTPUT</td><td>TREE REPRESENTATION</td><td>EXPECTED EXAMPLE</td><td>ERRORS</td></tr>";

							for(Document interp : docs) {
								List<Node> output = getAllNodes(interp);
								String error = "<td class=\"yellow\">";
								if(output.size() != expected.size()) {
									error = "<td class=\"red\">Number of nodes do not match</td>";
									red ++;
								}else {
									for(int i = 0; i < output.size(); i ++) {
										if(!output.get(i).getNodeName().equals(expected.get(i).getNodeName())) {
											error += output.get(i).getNodeName() + " != " + expected.get(i).getNodeName() + "<br/>";
										}
									}									
									if(error.equals("<td class=\"yellow\">")) {
										error = "<td class=\"green\">Match</td>";
										green ++;
									}else {
										error += "</td>";
										yellow ++;
									}
								}
								
								String tree = getTree(interp);
								html += "<tr><td>" + p2c.print(p2c.getOriginal()) + "</td><td><pre>"
										+ p2c.print(interp).replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;")
										+ "</pre></td><td><div class=\"tree\">" + tree + "</div></td><td><pre>"
										+ p2c.print(expectedContent).replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;")
										+ "</pre></td>" + error + "</tr>";
							}
							}
						}catch(Exception e) {
							e.printStackTrace();
						}
					}
			}
		}	
		}
		html += "</table>\n"
				+ "</body>\n"
				+ "</html>";
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter("Demo.html"));
		    	writer.write(html);
			    writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("Perfect matches      : " + green);
			System.out.println("Total mismatches     : " + (red+yellow));
			System.out.println("Node name mismatches : " + yellow);

	}
	
	private static String getTree(Node node) {
		String tree = "<ul>";
		int s = 0;
		if(node.getNodeName().equals("apply"))s = 1;
		NodeList nl = node.getChildNodes();
		for(int i = s; i < nl.getLength(); i++) {
			String nodeName = nl.item(i).getNodeName();
			if(nl.item(i).getNodeName().equals("apply"))
				nodeName = nl.item(i).getFirstChild().getNodeName();
			if(nl.item(i).hasChildNodes()&&nl.item(i).getFirstChild().getNodeName().equals("#text"))
				nodeName = nl.item(i).getFirstChild().getTextContent();
			tree += "<li>" + nodeName;
			if(nl.item(i).hasChildNodes()&&!nl.item(i).getFirstChild().getNodeName().equals("#text"))
				tree += getTree(nl.item(i));
			tree += "</li>";
		}
		tree += "</ul>";
		return tree;
	}


	private static Document buildDoc(String fileName) throws SAXException, IOException, ParserConfigurationException {
		File f = new File(fileName);
		byte[] encoded = Files.readAllBytes(f.toPath());
		String input = new String(encoded, Charset.defaultCharset());
		input = input.replaceAll("&", "&amp;");
		
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(input));
		Document doc = db.parse(is);
		return doc;
	}

}
