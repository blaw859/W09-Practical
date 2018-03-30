import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class XMLReader {
  private static String searchType;
  private static StringBuilder outputString;
  private static int authorCount;

  public static String readXML(String queryURL,String searchType) throws Exception{
  outputString = new StringBuilder();
    StringBuilder result = new StringBuilder();
    URL url = new URL(queryURL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
    String line;
    while ((line = rd.readLine()) != null) {
      result.append(line+"\n");
      switch (searchType) {
        case "venue" :
          readVenueData(line);
          break;
        case "publication" :
          readPublicationData(line);
          break;
        case "author" :
          readAuthorData(line);
          break;
      }
    }
    rd.close();
    return outputString.toString();
  }

  public static int[] readAuthorXML(String queryURL) throws Exception{
    int[] outputData = new int[2];
    StringBuilder result = new StringBuilder();
    URL url = new URL(queryURL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
    String line;
    while ((line = rd.readLine()) != null) {
      result.append(line+"\n");
      if (line.contains("<title>") && line.contains("</title>")) {
        outputData[0]++;
      }
      if (line.contains("<co") && line.contains("</co>")) {
        outputData[1]++;
      }
    }
    rd.close();
    return outputData;
  }

  private static void readVenueData(String line) {
    if (line.contains("<venue>") && line.contains("</venue>")) {
      outputString.append(line.substring(line.indexOf("<venue>")+7, line.indexOf("</venue>")) + "\n");
    }
  }

  private static void readPublicationData(String line) {
    if (line.contains("<author>") && line.contains("</author>")) {
      authorCount++;
    }
    if (line.contains("<title>") && line.contains("</title>")) {
      outputString.insert(0, line.substring(line.indexOf("<title>")+7, line.indexOf("</title>")));
      outputString.append(" (number of authors: " + authorCount + ")\n");
      authorCount = 0;
    }
  }

  private static void readAuthorData(String line) {

    if (line.contains("<author>") && line.contains("</author>")) {
      outputString.append(line.substring(line.indexOf("<author>")+8, line.indexOf("</author>")));
    }
    if (line.contains("<url>") && line.contains("</url>") && line.contains("http://dblp.org/pid/")) {
       int[] pubAndCoAuth = new int[2];

      try {
        pubAndCoAuth = readAuthorXML(line.substring(line.indexOf("<url>")+5, line.indexOf("</url>")) + ".xml");
      } catch (Exception e) {
        e.printStackTrace();
      }
      outputString.append(" - " + pubAndCoAuth[0] + " publications with " + pubAndCoAuth[1] + " co-authors.\n");
    }
  }

}
