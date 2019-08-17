package ambiguities;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MatchRow {

	public Node startNode;
	private List<Node> rowList;
	public List<Node> nodesToRemove;
	public List<Node> vars;
	public List<Node> vartexts;
	public List<Node> stretchvars;
	private int stretchint;

	public MatchRow(List<Node> rowList, Node startNode) {
		this.startNode = startNode;
		this.rowList = rowList;
		this.nodesToRemove = new ArrayList<Node>();
		this.vars = new ArrayList<Node>();
		this.vartexts = new ArrayList<Node>();
		this.stretchvars = new ArrayList<Node>();
		this.stretchint = 0;
	}

	// return true if doc contains a matching row
	public boolean matches() {
		Node nodeToCheck = startNode;
		for(Node expectedNode : rowList) {
			if(nodesMatch(nodeToCheck, expectedNode) && matchAttr(nodeToCheck, expectedNode)) {
				nodesToRemove.add(nodeToCheck);
				if(stretchint < 1) {
					nodeToCheck = nodeToCheck.getNextSibling();
				}else {
					for(int i = 0; i < stretchint; i++) {
						nodeToCheck = nodeToCheck.getNextSibling();
						nodesToRemove.add(nodeToCheck);
					}
					stretchint = 0;
				}
				System.out.print("");
			}else {
				//remove all repl nodes from rowList doc
				return false;
			}
		}		
		return true;
	}


	private boolean nodesMatch(Node nodeToCheck, Node expectedNode) {
		if(nodeToCheck == null) {
			return false;
		}else if(expectedNode.getNodeName().equals("var")){
			vars.add(nodeToCheck);
			return true;
		}else if(expectedNode.getNodeName().equals("stretchvar")){
			while (nodeToCheck != null){
				stretchvars.add(nodeToCheck);
				nodeToCheck = nodeToCheck.getNextSibling();
				stretchint ++;

				if(expectedNode.getNextSibling()!= null && 
						nodesMatch(nodeToCheck, expectedNode.getNextSibling())) {
					nodeToCheck = null;
				}
			}
			if(expectedNode.getNextSibling()!= null && 
					nodesMatch(stretchvars.get(stretchint-1), expectedNode.getNextSibling())) {
				stretchint --;
				stretchvars.remove(stretchint);
			}
			return true;
		}else if(expectedNode.getNodeName().equals("vartext") &&
				expectedNode.getTextContent().equals(nodeToCheck.getNodeName())){
			vartexts.add(nodeToCheck);
			return true;
		}else if(expectedNode.getNodeName().equals(nodeToCheck.getNodeName())){
			if(!expectedNode.hasChildNodes()) {
				return !nodeToCheck.hasChildNodes();
			}else if(expectedNode.getFirstChild().getNodeName().equals("#text")) {
				return expectedNode.getTextContent().equalsIgnoreCase(nodeToCheck.getTextContent());
			}else if(expectedNode.hasChildNodes() && nodeToCheck.hasChildNodes()) {
				if(!nodeToCheck.getFirstChild().getNodeName().equals("#text")) {
					NodeList expectedChildNodes = expectedNode.getChildNodes();
					List<Node> nextRow = new ArrayList<Node>();
					for(int i = 0; i < expectedChildNodes.getLength(); i ++) {
						nextRow.add(expectedChildNodes.item(i));
					}
					MatchRow mr = new MatchRow(nextRow, nodeToCheck.getFirstChild());
					boolean r = mr.matches();
					vars.addAll(mr.vars);
					vartexts.addAll(mr.vartexts);
					stretchvars.addAll(mr.stretchvars);
					return r;
				}
			}			
		}
		
		return false;
	}
	
	
	
	private boolean matchAttr(Node nodeToCheck, Node expectedNode) {
		Element eNode = (Element)expectedNode;
		Element cNode = (Element)nodeToCheck;
				
		NamedNodeMap expected = eNode.getAttributes();
		for(int i = 0; i < expected.getLength(); i++) {
			Node att = expected.item(i);
			if(!cNode.getAttribute(att.getNodeName()).equals(att.getTextContent())) {
				return false;
			}
		}
		
		return true;
	}

}
