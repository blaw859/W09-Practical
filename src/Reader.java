import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class Reader {
  private static String searchType;
  private static StringBuilder outputString;
  private static int authorCount;

  public static String read(String queryURL, String searchType) throws Exception{
  outputString = new StringBuilder();
    StringBuilder result = new StringBuilder();
    File fileInCache = new File(W09Practical.getCache(), URLEncoder.encode(queryURL,"UTF-8"));
    BufferedReader rd;
    if (!fileInCache.exists()) {
      URL url = new URL(queryURL);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      W09Practical.addFileToCache(conn,queryURL);
    }
    JsonReader reader = Json.createReader(new BufferedReader(new FileReader(fileInCache)));
    JsonObject jo = reader.readObject();
    System.out.println(jo.getJsonObject("result").getString("query"));
    switch (searchType) {
      case "venue" :
        readVenueData(jo);
        break;
      case "publ" :
        readPublicationData(jo);
        break;
      case "author" :
        //readAuthorData(line);
        break;
    }
    /*while ((line = rd.readLine()) != null) {
      result.append(line+"\n");
      switch (searchType) {
        case "venue" :
          readVenueData(line);
          break;
        case "publ" :
          readPublicationData(line);
          break;
        case "author" :
          readAuthorData(line);
          break;
      }
    }
    rd.close();*/
    return outputString.toString();
  }

  public static int[] readAuthorXML(String queryURL) throws Exception{
    int[] outputData = new int[2];
    StringBuilder result = new StringBuilder();
    File fileInCache = new File(W09Practical.getCache(), URLEncoder.encode(queryURL,"UTF-8"));
    BufferedReader rd;
    if (!fileInCache.exists()) {
      URL url = new URL(queryURL);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      W09Practical.addFileToCache(conn,queryURL);
    }
    rd = new BufferedReader(new FileReader(fileInCache));
    String line;
    while ((line = rd.readLine()) != null) {
      result.append(line+"\n");
      if (line.contains("<title>") && line.contains("</title>")) {
        outputData[0]++;
      }
      if (line.contains("<co>") && line.contains("</co>")) {
        outputData[1]++;
      }
    }
    rd.close();
    return outputData;
  }

  private static void readVenueData(JsonObject jsonData) {
    JsonArray hits = jsonData.getJsonObject("result").getJsonObject("hits").getJsonArray("hit");
    for (int i = 0; i < hits.size(); i++) {
      outputString.append(hits.getJsonObject(i).getJsonObject("info").getString("venue")+"\n");
    }
    /*if (line.contains("<venue>") && line.contains("</venue>")) {
      outputString.append(line.substring(line.indexOf("<venue>")+7, line.indexOf("</venue>")) + "\n");
    }*/
  }

  private static void readPublicationData(JsonObject jsonData) {
    JsonArray hits = jsonData.getJsonObject("result").getJsonObject("hits").getJsonArray("hit");
    for (int i = 0; i < hits.size(); i++) {
      outputString.append(hits.getJsonObject(i).getJsonObject("info").getString("venue")+"\n");
    }
    /*if (line.contains("<title>") && line.contains("</title>")) {
      outputString.append(line.substring(line.indexOf("<title>")+7, line.indexOf("</title>")));
      outputString.append(" (number of authors: " + countNumberOfAuthors(line) + ")\n");
    }*/
  }

  public static int countNumberOfAuthors(String line) {
    int lastIndex = 0;
    int count = 0;

    while (lastIndex != -1) {
      lastIndex = line.indexOf("<author>",lastIndex);

      if (lastIndex != -1) {
        count++;
        lastIndex += "<author>".length();
      }
    }
    return count;
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
