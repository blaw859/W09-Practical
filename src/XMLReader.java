import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class XMLReader {
  private static String searchType;
  private static StringBuilder outputString;
  private static int authorCount;
  private static String queryURL;

  /**
   * Reads the XML data for the specific query, the cache is always checked first and then if no file is found a new
   * cache file is created and that is what is used. A switch statement is used depending on the searchType to see
   * what
   * @param queryURL
   * @param searchType
   * @return
   * @throws Exception
   */
  public static String readXML(String queryURL,String searchType) throws Exception{
    XMLReader.queryURL = queryURL;
    outputString = new StringBuilder();
    StringBuilder result = new StringBuilder();
    File fileInCache = new File(W09Practical.getCache(), URLEncoder.encode(queryURL,"UTF-8"));
    BufferedReader rd;
    if (!fileInCache.exists()) {
      query(queryURL);
    }
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(fileInCache);
    doc.getDocumentElement().normalize();
    switch (searchType) {
      case "venue" :
        parseVenueXML(doc);
        break;
      case "publ" :
        parsePublicationXML(doc);
        break;
      case "author" :
        parseAuthorXML(doc);
        break;
    }
    return outputString.toString();
  }

  private static void parseVenueXML(Document doc) {
    NodeList infoNodes = doc.getElementsByTagName("info");
      for (int i = 0; i < infoNodes.getLength(); i++) {
        Element eElement = (Element) infoNodes.item(i);
        outputString.append(eElement.getElementsByTagName("venue").item(0).getTextContent()+"\n");
      }

  }

  private static void parsePublicationXML(Document doc) {
    NodeList infoNodes = doc.getElementsByTagName("info");

      for (int i = 0; i < infoNodes.getLength(); i++) {
        Element eElement = (Element) infoNodes.item(i);
        int authorCount = eElement.getElementsByTagName("author").getLength();
        outputString.append(eElement.getElementsByTagName("title").item(0).getTextContent() + " (number of authors: " + authorCount + ")\n");
    }
  }

  private static void parseAuthorXML(Document doc) {
    NodeList infoNodes = doc.getElementsByTagName("info");
    for (int i = 0; i < infoNodes.getLength(); i++) {
      Element eElement = (Element) infoNodes.item(i);
      int[] authorData = new int[2];
      try {
        authorData = getAuthorData(eElement.getElementsByTagName("url").item(0).getTextContent()+".xml");
      } catch (Exception e) {
        System.out.println("");
      }
      outputString.append(eElement.getElementsByTagName("author").item(0).getTextContent()+ " - " + authorData[0] + " publications with " + authorData[1] + " co-authors.\n");
     }
  }

  private static int[] getAuthorData(String authorURL) throws Exception{
    int[] outputArray = new int[2];
    File fileInCache = new File(W09Practical.getCache(), URLEncoder.encode(authorURL,"UTF-8"));
    if (!fileInCache.exists()) {
      query(authorURL);
    }
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(fileInCache);
    doc.getDocumentElement().normalize();
    NodeList publicationsList = doc.getElementsByTagName("dblpperson");
    NodeList coAuthorList = doc.getElementsByTagName("coauthors");
    Node publications = publicationsList.item(0);
    if (coAuthorList.getLength() >= 1) {
      Node coAuthors = coAuthorList.item(0);
      Element coAuthorsElement = (Element) coAuthors;
      outputArray[1] = Integer.parseInt(coAuthorsElement.getAttribute("n"));
    } else {
      outputArray[1] = 0;
    }
    Element publicationsElement = (Element) publications;
    outputArray[0] = Integer.parseInt(publicationsElement.getAttribute("n"));
    return outputArray;
  }

  private static void query(String queryURL) throws Exception{
    URL url = new URL(queryURL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    W09Practical.addFileToCache(conn,queryURL);
  }
}
