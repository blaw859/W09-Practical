import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;

public class W09Practical {
  private static boolean argumentsMalformed;
  private static HashMap<String,String> argumentMap;
  private static File cache;
  /**
   * Main method initially validates the arguments, checks they are not malformed then creates the cache
   * @param args Should be of the form --cache x --search y --query z in any order
   */
  public static void main(String[] args) {

    argumentsMalformed = false;
    argumentToMap(args);
    validateArgs();
    initializeCache();
    try {
      System.out.print(XMLReader.readXML(formatQueryURL(),argumentMap.get("--search")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Uses the argument map to format a URL of a query so they match the ones in the given cache
   * @return The url for the given query
   */
  private static String formatQueryURL() {
    String outputString = "http://dblp.org/search/" + argumentMap.get("--search") + "/api?format=xml&c=0&h=40&q=" + argumentMap.get("--query").replace(" ","+");
    return outputString;
  }

  /**
   * When new data is queried this will add it to the cache
   * @param conn The connection required to access the data to be written to the file
   * @param queryURL The URL belonging the data, to be encoded into the name of the file
   */
  public static void addFileToCache(HttpURLConnection conn, String queryURL ) {
    File newCacheFile;
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
      newCacheFile = new File(cache, URLEncoder.encode(queryURL, "UTF-8"));
      newCacheFile.createNewFile();
      BufferedWriter writer = new BufferedWriter(new FileWriter(newCacheFile));
      String line;
      while ((line = reader.readLine()) != null) {
        writer.append(line+"\n");
      }
      writer.close();
    } catch (IOException e) {
      System.out.println("Unable to encode filename");
      System.out.println(queryURL);
      e.printStackTrace();
    }
  }

  /**
   * Checks if the cache actually exists and if the cache does exist the correct error will be outputted
   */
  private static void initializeCache() {
    File tempCache = new File(argumentMap.get("--cache"));
    if (tempCache.isDirectory()) {
      cache = tempCache;
    } else {
      printErrors("Cache directory doesn't exist: "+argumentMap.get("--cache"));
    }
  }

  /**
   * Checks if all of the user inputted arguments are valid and outputs the correct error messages
   * if they are not
   */
  private static void validateArgs() {
    String searchValue = argumentMap.get("--search");
    if (argumentMap.get("--search") == null) {
      printErrors("Missing value for --search");
    } else if (!(argumentMap.get("--search").equals("author") || argumentMap.get("--search").equals("venue") || argumentMap.get("--search").equals("publ"))) {
      if (argumentMap.get("--cache").substring(0,2).equals("--")) {
        argumentsMalformed = true;
      }
      printErrors("Invalid value for --search: " +argumentMap.get("--search"));
    }
    if (argumentMap.get("--cache") == null) {
      printErrors("Missing value for --cache");
    }
    if (argumentMap.get("--query") == null) {
      printErrors("Missing value for --query");
    }
  }

  /**
   * Puts the user defined arguments into a hashmap so that they can be more easily accessed in other places.
   * Also checks a specific case where the argument might be malformed according to stacscheck
   * @param args The input arguments to be put into a hashmap
   */
  private static void argumentToMap(String[] args) {
    argumentMap = new HashMap<>();
    for (int i = 0; i < args.length; i++) {
      if (args[i].substring(0,2).equals("--")) {
        if ((i+1) < args.length) {
          if (args[i].equals("--search") && args[i+1].equals("publication")) {
            args[i+1] = "publ";
          }
          if (args[i+1].substring(0,2).equals("--") && !args[i].equals("--cache")) {
            argumentsMalformed = true;
          }
          argumentMap.put(args[i], args[i + 1]);
        }
      }
    }
  }

  /**
   * Checks if the arguments are malformed by seeing if there are null values in the argumentMap hashmap
   */
  private static void checkArgumentsMalformed() {
    if (argumentMap.get("--search") == null || argumentMap.get("--query") == null) {
      argumentsMalformed = true;
    }
  }

  /**
   * Prints out the correct error message depending on the input and whether or not the inputs are malformed
   * @param errorMsg The error message for the specific error
   */
  private static void printErrors(String errorMsg) {
    checkArgumentsMalformed();
    System.out.println(errorMsg);
    if (argumentsMalformed) {
      System.out.println("Malformed command line arguments.");
    }
    System.exit(0);
  }

  /**
   * Returns the cache File
   * @return The cache File
   */
  public static File getCache() {
        return cache;
    }
}