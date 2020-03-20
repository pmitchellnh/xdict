package mitchell.pete.xwd.dictionary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class XDictConfig {

    // The following parameters can be overridden by the config.txt file.
    public static boolean testMode = false;
    public static String TEST_MODE_SUFFIX = "";
    public static String LOAD_FILE_DELIMITERS = ";:";
    public static String EXPORT_FILE_DELIMITER = ";";


    static final String configFileName = "config.txt";

    public static boolean processConfigFile()
    {
        System.out.println("Loading from file: " + configFileName);
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(configFileName));
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            System.out.println(e.toString());
            return false;
        }
        String line;
        int count = 0;

        try {
            while ((line = br.readLine()) != null) {
                if (line.length() < 3) {
                    continue;
                }
                parseConfigLine(line, "=");
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
            System.out.println(e.toString());
            return false;
        }
        try {
            br.close();
        } catch (IOException e) {
            System.out.println("Error closing file.");
            System.out.println(e.toString());
            return false;
        }

        return true;
    }

    private static void parseConfigLine(String line, String delims)
    {
        StringTokenizer st = new StringTokenizer(line, delims, false);
        String key = "";
        String value = "";
        if (st.hasMoreTokens()) {
            key = st.nextToken();
        }

        if (key.startsWith("#")) {      // ignore comment lines
            return;
        }

        if (st.hasMoreTokens()) {
            value = st.nextToken();
        }

        if (key.equals("TEST_MODE")) {
            testMode = true;
            TEST_MODE_SUFFIX = "_TEST";
            System.out.println("Test mode enabled.");
            System.out.println("TEST_MODE_SUFFIX: " + XDictConfig.TEST_MODE_SUFFIX);
        } else if (key.equals("LOAD_FILE_DELIMITERS")) {
            if (!value.isEmpty() && value.startsWith("[") && value.endsWith("]")) {
                LOAD_FILE_DELIMITERS = value.substring(1, (value.length() - 1));
                System.out.println("LOAD_FILE_DELIMTERS: [" + LOAD_FILE_DELIMITERS + "]");
            }
        } else if (key.equals("EXPORT_FILE_DELIMITER")) {
            if (!value.isEmpty() && value.startsWith("[") && value.endsWith("]")) {
                EXPORT_FILE_DELIMITER = value.substring(1, (value.length() - 1));
                System.out.println("EXPORT_FILE_DELIMTER: [" + EXPORT_FILE_DELIMITER + "]");
            }
        } else {
            System.out.println("Unhandled parameter: " + key + "=" + value);
        }

        return;
    }
}
