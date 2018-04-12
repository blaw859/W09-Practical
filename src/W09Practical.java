import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;

public class W09Practical {
    private static boolean argumentsMalformed;
    private static HashMap<String,String> argumentMap;
    private static File cache;

    public static void main(String[] args) {
        argumentsMalformed = false;
        argumentToMap(args);
        validateArgs();
        initializeCache();
        try {
            System.out.println(Reader.readXML(formatQueryURL(),argumentMap.get("--search")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String formatQueryURL() {
        String outputString = "http://dblp.org/search/" + argumentMap.get("--search") + "/api?q=" + argumentMap.get("--query").replace(" ","%20") + "/";
      System.out.println(outputString);
        return outputString;
    }

    public static void addFileToCache(HttpURLConnection conn, String queryURL ) {
      File newCacheFile;
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
        if (queryURL.substring(queryURL.length()-4,queryURL.length()).equals(".xml")) {
          newCacheFile = new File(cache, URLEncoder.encode(queryURL, "UTF-8"));
        } else {
          newCacheFile = new File(cache, URLEncoder.encode(queryURL, "UTF-8")+".xml");

        }
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
        newCacheFile = null;
      } }

    private static void initializeCache() {
        File tempCache = new File(argumentMap.get("--cache"));
        if (tempCache.isDirectory()) {
            cache = tempCache;
        } else {
            printErrors("Cache directory doesn't exist: "+argumentMap.get("--cache"));
        }
    }

    private static void validateArgs() {
        String searchValue = argumentMap.get("--search");
        if (argumentMap.get("--search") == null) {
            printErrors("Missing value for --search");
        } else if (!(argumentMap.get("--search").equals("author") || argumentMap.get("--search").equals("venue") || argumentMap.get("--search").equals("publ"))) {
            printErrors("Invalid value for --search: " +argumentMap.get("--search"));
        }
        if (argumentMap.get("--cache") == null) {
            printErrors("Missing value for --cache");
        }
        if (argumentMap.get("--query") == null) {
            printErrors("Missing value for --query");
        }
    }

    private static void argumentToMap(String[] args) {
        argumentMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].substring(0,2).equals("--")) {
                if ((i+1) < args.length) {
                  if (args[i].equals("--search") && args[i+1].equals("publication")) {
                    args[i+1] = "publ";
                  }
                    argumentMap.put(args[i],args[i+1]);
                }
            }
        }
    }

    private static void checkArgumentsMalformed() {
        if (argumentMap.get("--search") == null || argumentMap.get("--cache") == null || argumentMap.get("--query") == null) {
            argumentsMalformed = true;
        }
    }

    private static void printErrors(String errorMsg) {
        checkArgumentsMalformed();
        System.out.println(errorMsg);
        if (argumentsMalformed) {
            System.out.println("Malformed command line arguments.");
        }
        System.exit(0);
    }

  public static File getCache() {
    return cache;
  }
}