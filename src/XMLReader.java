import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

public class XMLReader {
  private static StringBuilder outputString;
  private static String queryURL;

  /**
   * Reads the XML data for the specific query, the cache is always checked first and then if no file is found a new
   * cache file is created and that is what is used. A switch statement is used depending on the searchType to see
   * what search type the user has input and therefore what should be outputted
   * @param queryURL The URL formatted earlier that is used to retrieve data from the api
   * @param searchType Either venue,author or publication to be used to find out what to return
   * @return The output string appended to by the parsing method called
   * @throws Exception thrown by encoding, newDocumentBuilding and parsing to doc
   */
  public static String readXML(String queryURL,String searchType) throws Exception{
    XMLReader.queryURL = queryURL;
    outputString = new StringBuilder();
    File fileInCache = new File(W09Practical.getCache(), URLEncoder.encode(queryURL,"UTF-8"));
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

  /**
   * Uses the DOM parser to get the relevant information for the venue query and format it onto the output string
   * @param doc Document containing the XML information
   */
  private static void parseVenueXML(Document doc) {
    NodeList infoNodes = doc.getElementsByTagName("info");
      for (int i = 0; i < infoNodes.getLength(); i++) {
        Element element = (Element) infoNodes.item(i);
        outputString.append(element.getElementsByTagName("venue").item(0).getTextContent()+"\n");
      }

  }

  /**
   * Uses the DOM parser to get the relevant information for the publication query and format it onto the output string
   * @param doc Document containing the XML information
   */
  private static void parsePublicationXML(Document doc) {
    NodeList infoNodes = doc.getElementsByTagName("info");

      for (int i = 0; i < infoNodes.getLength(); i++) {
        Element eElement = (Element) infoNodes.item(i);
        int authorCount = eElement.getElementsByTagName("author").getLength();
        outputString.append(eElement.getElementsByTagName("title").item(0).getTextContent() + " (number of authors: " + authorCount + ")\n");
    }
  }

  /**
   * Uses the DOM parser to get the relevant information for the author query and format it onto the output string. Another
   * method call is made to get the information from the other url contained within doc about the author
   * @param doc Document containing the XML information
   */
  private static void parseAuthorXML(Document doc) {
    NodeList infoNodes = doc.getElementsByTagName("info");
    for (int i = 0; i < infoNodes.getLength(); i++) {
      Element element = (Element) infoNodes.item(i);
      int[] authorData = new int[2];
      try {
        authorData = getAuthorData(element.getElementsByTagName("url").item(0).getTextContent()+".xml");
      } catch (Exception e) {
        System.out.println("Unable to get author data");
      }
      outputString.append(element.getElementsByTagName("author").item(0).getTextContent()+ " - " + authorData[0] + " publications with " + authorData[1] + " co-authors.\n");
     }
  }

  /**
   * This method makes the second XML query that is necessary for the publication and co-author information to be returned
   * regarding some authors
   * @param authorURL The secondary URL needed to get all the information about the author
   * @return returns the number of publications and the number of co authors in an array with two elements
   * @throws Exception Exception due to URL encoding, and making the Document
   */
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
    //This is necessary because if coAuthorList.item(0) is equal to 0 this causes strange problems when outputting the result
    // i.e. it doesn't just return 0 or null
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

  /**
   * This method actually gets the XML from the api and then calls necessary methods to store it in the cache. Also
   * checks if DBLP can actually be accessed before trying to do so
   * @param queryURL The URL that needs to be queried
   * @throws Exception Exceptions thrown for connecting to the api and creating a URL
   */
  private static void query(String queryURL) throws Exception{
    URL url = new URL(queryURL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    try {
      conn.getResponseCode();
    } catch (UnknownHostException e) {
      System.out.println("Unable to connect to DBLP check your internet connection");
      System.exit(0);
    }
    W09Practical.addFileToCache(conn,queryURL);

  }
}
