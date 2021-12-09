package ca.gc.cra.ice;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Tests the generated pom.xml for validity
 */
public class PomTest
{
	private static Document build(DocumentBuilder db, String fileName) throws SAXException, IOException
	{
		try (InputStream is = Files.newInputStream(Paths.get("target", fileName)))
		{
			return db.parse(is);
		}
	}
	
	/**
	 * Wraps a {@link NodeList} as a {@link java.util.List List&lt;Node&gt;}
	 */
	private static final class NodeListList extends AbstractList<Node>
	{
		private final NodeList nl;
		
		public NodeListList(NodeList nl)
		{
			this.nl = nl;
		}

		@Override
		public Node get(int index)
		{
			Node r = nl.item(index);
			if (r == null)
			{
				throw new IndexOutOfBoundsException();
			}
			return r;
		}

		@Override
		public int size()
		{
			return nl.getLength();
		}
		
	}
	
	/**
	 * Wraps a {@link NamedNodeMap} as a {@link java.util.List List&lt;Node&gt;}
	 */
	private static final class NamedNodeMapList extends AbstractList<Node>
	{
		private final NamedNodeMap nnm;
		
		public NamedNodeMapList(NamedNodeMap nnm)
		{
			this.nnm = nnm;
		}

		@Override
		public Node get(int index)
		{
			Node r = nnm.item(index);
			if (r == null)
			{
				throw new IndexOutOfBoundsException();
			}
			return r;
		}

		@Override
		public int size()
		{
			return nnm.getLength();
		}
	}
	
	/**
	 * Converts an element's attributes to a {@link java.util.Map Map&lt;String, String&gt;}
	 * 
	 * @param e
	 * The element for which the attribute map will be returned
	 *  
	 * @return
	 * The element's attribute map
	 */
	private static Map<String, String> attributeMap(Element e)
	{
		return new NamedNodeMapList(e.getAttributes())
				.stream()
				.collect(Collectors.toMap(Node::getNodeName, Node::getNodeValue));
	}
	
	/**
	 * Returns a parent element's immediate child elements
	 * 
	 * @param e
	 * A parent element for which child elements will be returned
	 * 
	 * @return
	 * The parent element's immediate child elements
	 */
	private static Iterator<Element> childElements(Element e)
	{
		return new NodeListList(e.getChildNodes())
				.stream()
				.filter(Element.class::isInstance)
				.map(Element.class::cast)
				.iterator();
	}
	
	/**
	 * Compares two elements for equality.  To be considered equal, the elements must have the same name and attributes
	 * when disregarding attribute ordering.  If both elements have no child elements, their text contents are compared.
	 *  
	 * @param e1
	 * The first element to compare
	 * 
	 * @param e2
	 * The second element to compare
	 * 
	 * @return
	 * {@code true} if the elements are considered equal, otherwise {@code false}
	 */
	private static boolean equalElements(Element e1, Element e2)
	{
		if (!e1.getNodeName().equals(e2.getNodeName()))
		{
			return false;
		}
		if (!attributeMap(e1).equals(attributeMap(e2)))
		{
			return false;
		}
		
		Iterator<Element> e1Iter = childElements(e1);
		Iterator<Element> e2Iter = childElements(e2);
		
		if (!e1Iter.hasNext())
		{
			if (e2Iter.hasNext())
			{
				return false;
			}
			
			/*
			 * When both elements have no child elements, we'll just compare their text contents
			 */
			return Objects.equals(e1.getTextContent(), e2.getTextContent());
		}
		
		do
		{
			if (!e2Iter.hasNext())
			{
				return false;
			}
			if (!equalElements(e1Iter.next(), e2Iter.next()))
			{
				return false;
			}
		} while (e1Iter.hasNext());
		
		return !e2Iter.hasNext();
	}
	
	/**
	 * Ensures there are relevant changes between the old pom.xml and the newly generated one
	 * 
	 * @throws Exception
	 */
	@Test
	public void comparePoms() throws Exception
	{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document original = build(db, "original-pom.xml");
		Document generated = build(db, "generated-pom.xml");

		/*
		 * We want to ignore the <version> elements, so we'll just remove them from the DOMs
		 */
		XPath xp = XPathFactory.newInstance().newXPath();
		XPathExpression versionExp = xp.compile("/project/version");
		removeNodes(original, versionExp);
		removeNodes(generated, versionExp);
		
		Assert.assertFalse(
				"original and generated models are equal (no version updates or other relevant changes)", 
				equalElements(original.getDocumentElement(), generated.getDocumentElement()));
	}

	/**
	 * Removes all nodes matching an XPath expression
	 * 
	 * @param item
	 * The context node in which the XPath expression will be executed
	 * 
	 * @param xpExp
	 * The XPath expression that will produce a node list of nodes to remove
	 * 
	 * @throws XPathExpressionException
	 */
	private static void removeNodes(Node item, XPathExpression xpExp) throws XPathExpressionException
	{
		NodeList nodeList = (NodeList) xpExp.evaluate(item, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			node.getParentNode().removeChild(node);
		}
	}
}
