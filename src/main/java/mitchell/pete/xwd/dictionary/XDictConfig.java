package mitchell.pete.xwd.dictionary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class XDictConfig {

    // The following parameters can be overridden by the config.txt file.
    public static boolean testMode = false;
    public static String DB_MODE_SUFFIX = "";
    public static final String TEST_MODE_SUFFIX = "_TEST";
    public static String LOAD_FILE_DELIMITERS = ";:";
    public static String EXPORT_FILE_DELIMITER = ";";
    public static String LOAD_FILE_DEFAULT_DIR = "";
    public static String EXPORT_FILE_DEFAULT_DIR = "";
    public static int EXPORT_DEFAULT_MINIMUM_RATING = 0;
    public static int APP_WIDTH = 1300;     // value in pixels
    public static int APP_HEIGHT = 850;     // value in pixels

    public static final int KILL = 0;       // kill means kill -- no reason to configure this
    public static int TERRIBLE = 5;
    public static int TERRIBLE_6 = -1;
    public static int TERRIBLE_5 = -1;
    public static int TERRIBLE_4 = -1;
    public static int TERRIBLE_3 = -1;
    public static int POOR = 30;
    public static int POOR_6 = -1;
    public static int POOR_5 = -1;
    public static int POOR_4 = -1;
    public static int POOR_3 = -1;
    public static int LAME = 50;
    public static int LAME_6 = -1;
    public static int LAME_5 = -1;
    public static int LAME_4 = -1;
    public static int LAME_3 = -1;
    public static int OK = 60;
    public static int OK_3 = -1;
    public static int OK_4 = -1;
    public static int OK_5 = -1;
    public static int OK_6 = -1;
    public static int GOOD_3 = -1;
    public static int GOOD_4 = -1;
    public static int GOOD_5 = -1;
    public static int GOOD_6 = -1;
    public static int GOOD = 70;
    public static int EXCELLENT_3 = -1;
    public static int EXCELLENT_4 = -1;
    public static int EXCELLENT_5 = -1;
    public static int EXCELLENT_6 = -1;
    public static int EXCELLENT = 80;

    public static int TERRIBLE_MIN = 0;
    public static int TERRIBLE_MAX = 1000;
    public static int POOR_MIN = 0;
    public static int POOR_MAX = 100;
    public static int LAME_MIN = 0;
    public static int LAME_MAX = 100;
    public static int OK_MIN = 0;
    public static int OK_MAX = 100;
    public static int GOOD_MIN = 0;
    public static int GOOD_MAX = 100;
    public static int EXCELLENT_MIN = 0;
    public static int EXCELLENT_MAX = 100;


    public enum RATINGS { KILL, TERRIBLE, POOR, LAME, OK, GOOD, EXCELLENT, MANUAL, RESEARCH, SKIP }

    static final String configFileName = "config.txt";
    static final String origConfigFileName = "config.orig";

    public static boolean processConfigFile()
    {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(configFileName));
            System.out.println("Loading from file: " + configFileName);
            System.out.println("----------------------------");
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + configFileName);
//            System.out.println(e.toString());
            br = null;
        }
        if (br == null) {       // if no config.txt, default to config.orig
            try {
                br = new BufferedReader(new FileReader(origConfigFileName));
                System.out.println("Loading from file: " + origConfigFileName);
                System.out.println("----------------------------");
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + origConfigFileName);
//                System.out.println(e.toString());
                return false;
            }
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
        } catch (NumberFormatException e) {
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

        System.out.println("----------------------------");

        setNonconfiguredRatingValues();

        return true;
    }

    private static void parseConfigLine(String line, String delims) throws NumberFormatException
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
            DB_MODE_SUFFIX = TEST_MODE_SUFFIX;
            System.out.println("Test mode enabled.");
            System.out.println("DB_MODE_SUFFIX: " + XDictConfig.DB_MODE_SUFFIX);
        } else if (key.equals("LOAD_FILE_DELIMITERS")) {
            if (!value.isEmpty() && value.startsWith("[") && value.endsWith("]")) {
                LOAD_FILE_DELIMITERS = value.substring(1, (value.length() - 1));
                System.out.println("LOAD_FILE_DELIMITERS: [" + LOAD_FILE_DELIMITERS + "]");
            }
        } else if (key.equals("EXPORT_FILE_DELIMITER")) {
            if (!value.isEmpty() && value.startsWith("[") && value.endsWith("]")) {
                EXPORT_FILE_DELIMITER = value.substring(1, (value.length() - 1));
                System.out.println("EXPORT_FILE_DELIMITER: [" + EXPORT_FILE_DELIMITER + "]");
            }
        } else if (key.equals("LOAD_FILE_DEFAULT_DIR")) {
            if (!value.isEmpty()) {
                LOAD_FILE_DEFAULT_DIR = value;
                System.out.println("LOAD_FILE_DEFAULT_DIR: " + LOAD_FILE_DEFAULT_DIR);
            }
        } else if (key.equals("EXPORT_FILE_DEFAULT_DIR")) {
            if (!value.isEmpty()) {
                EXPORT_FILE_DEFAULT_DIR = value;
                System.out.println("EXPORT_FILE_DEFAULT_DIR: " + EXPORT_FILE_DEFAULT_DIR);
            }
        } else if (key.equals("EXPORT_DEFAULT_MINIMUM_RATING")) {
            System.out.print("EXPORT_DEFAULT_MINIMUM_RATING: ");
            EXPORT_DEFAULT_MINIMUM_RATING = parseRating(value);
            System.out.println(EXPORT_DEFAULT_MINIMUM_RATING);
        } else if (key.equals("APP_WIDTH")) {
            System.out.print("App Width: ");
            APP_WIDTH = parsePixels(value);
            System.out.println(APP_WIDTH);
        } else if (key.equals("APP_HEIGHT")) {
            System.out.print("App Height: ");
            APP_HEIGHT = parsePixels(value);
            System.out.println(APP_HEIGHT);
        } else if (key.equals("TERRIBLE")) {
            System.out.print("TERRIBLE: ");
            TERRIBLE = parseRating(value);
            System.out.println(TERRIBLE);
        } else if (key.equals("TERRIBLE_3")) {
            System.out.print("TERRIBLE_3: ");
            TERRIBLE_3 = parseRating(value);
            System.out.println(TERRIBLE_3);
        } else if (key.equals("TERRIBLE_4")) {
            System.out.print("TERRIBLE_4: ");
            TERRIBLE_4 = parseRating(value);
            System.out.println(TERRIBLE_4);
        } else if (key.equals("TERRIBLE_5")) {
            System.out.print("TERRIBLE_5: ");
            TERRIBLE_5 = parseRating(value);
            System.out.println(TERRIBLE_5);
        } else if (key.equals("TERRIBLE_6")) {
            System.out.print("TERRIBLE_6: ");
            TERRIBLE_6 = parseRating(value);
            System.out.println(TERRIBLE_6);
        } else if (key.equals("POOR")) {
            System.out.print("POOR: ");
            POOR = parseRating(value);
            System.out.println(POOR);
        } else if (key.equals("POOR_3")) {
            System.out.print("POOR_3: ");
            POOR_3 = parseRating(value);
            System.out.println(POOR_3);
        } else if (key.equals("POOR_4")) {
            System.out.print("POOR_4: ");
            POOR_4 = parseRating(value);
            System.out.println(POOR_4);
        } else if (key.equals("POOR_5")) {
            System.out.print("POOR_5: ");
            POOR_5 = parseRating(value);
            System.out.println(POOR_5);
        } else if (key.equals("POOR_6")) {
            System.out.print("POOR_6: ");
            POOR_6 = parseRating(value);
            System.out.println(POOR_6);
        } else if (key.equals("LAME")) {
            System.out.print("LAME: ");
            LAME = parseRating(value);
            System.out.println(LAME);
        } else if (key.equals("LAME_3")) {
            System.out.print("LAME_3: ");
            LAME_3 = parseRating(value);
            System.out.println(LAME_3);
        } else if (key.equals("LAME_4")) {
            System.out.print("LAME_4: ");
            LAME_4 = parseRating(value);
            System.out.println(LAME_4);
        } else if (key.equals("LAME_5")) {
            System.out.print("LAME_5: ");
            LAME_5 = parseRating(value);
            System.out.println(LAME_5);
        } else if (key.equals("LAME_6")) {
            System.out.print("LAME_6: ");
            LAME_6 = parseRating(value);
            System.out.println(LAME_6);
        } else if (key.equals("OK")) {
            System.out.print("OK: ");
            OK = parseRating(value);
            System.out.println(OK);
        } else if (key.equals("OK_3")) {
            System.out.print("OK_3: ");
            OK_3 = parseRating(value);
            System.out.println(OK_3);
        } else if (key.equals("OK_4")) {
            System.out.print("OK_4: ");
            OK_4 = parseRating(value);
            System.out.println(OK_4);
        } else if (key.equals("OK_5")) {
            System.out.print("OK_5: ");
            OK_5 = parseRating(value);
            System.out.println(OK_5);
        } else if (key.equals("OK_6")) {
            System.out.print("OK_6: ");
            OK_6 = parseRating(value);
            System.out.println(OK_6);
        } else if (key.equals("GOOD")) {
            System.out.print("GOOD: ");
            GOOD = parseRating(value);
            System.out.println(GOOD);
        } else if (key.equals("GOOD_3")) {
            System.out.print("GOOD_3: ");
            GOOD_3 = parseRating(value);
            System.out.println(GOOD_3);
        } else if (key.equals("GOOD_4")) {
            System.out.print("GOOD_4: ");
            GOOD_4 = parseRating(value);
            System.out.println(GOOD_4);
        } else if (key.equals("GOOD_5")) {
            System.out.print("GOOD_5: ");
            GOOD_5 = parseRating(value);
            System.out.println(GOOD_5);
        } else if (key.equals("GOOD_6")) {
            System.out.print("GOOD_6: ");
            GOOD_6 = parseRating(value);
            System.out.println(GOOD_6);
        } else if (key.equals("EXCELLENT")) {
            System.out.print("EXCELLENT: ");
            EXCELLENT = parseRating(value);
            System.out.println(EXCELLENT);
        } else if (key.equals("EXCELLENT_3")) {
            System.out.print("EXCELLENT_3: ");
            EXCELLENT_3 = parseRating(value);
            System.out.println(EXCELLENT_3);
        } else if (key.equals("EXCELLENT_4")) {
            System.out.print("EXCELLENT_4: ");
            EXCELLENT_4 = parseRating(value);
            System.out.println(EXCELLENT_4);
        } else if (key.equals("EXCELLENT_5")) {
            System.out.print("EXCELLENT_5: ");
            EXCELLENT_5 = parseRating(value);
            System.out.println(EXCELLENT_5);
        } else if (key.equals("EXCELLENT_6")) {
            System.out.print("EXCELLENT_6: ");
            EXCELLENT_6 = parseRating(value);
            System.out.println(EXCELLENT_6);
        } else {
            System.out.println("Unhandled parameter: " + key + "=" + value);
        }

        return;
    }

    private static int parseRating(String s) throws NumberFormatException
    {
        int rat = 0;
        if (!s.isEmpty()) {
            rat = Integer.valueOf(s);
            if (rat < 0) rat = 0;
            if (rat > 100) rat = 100;
        }
        return rat;
    }

    // Make sure pixel values are at least 100, so the app is visibile
    private static int parsePixels(String s) throws NumberFormatException
    {
        int val = 100;
        if (!s.isEmpty()) {
            val = Integer.valueOf(s);
            if (val < 100) val = 100;
        }
        return val;
    }

    public static String getRateButtonName(RATINGS r) {
        String name = "";
        switch (r) {
            case KILL:
                name = "Kill";
                break;
            case TERRIBLE:
                name = "Terrible";
                break;
            case POOR:
                name = "Poor";
                break;
            case LAME:
                name = "Lame";
                break;
            case OK:
                name = "Ok";
                break;
            case GOOD:
                name = "Good";
                break;
            case EXCELLENT:
                name = "Excellent";
                break;
            case MANUAL:
                name = "Manual";
                break;
            case RESEARCH:
                name = "Research";
                break;
            case SKIP:
                name = "Skip";
                break;
        }
        return name;
    }

    public static String getRateButtonDesc(RATINGS r)
    {
        String desc = "";
        switch (r) {
            case KILL:
                desc = "Not a word. Kill it. (0)";
                break;
            case TERRIBLE:
                desc = "You're joking, right? (" + TERRIBLE_MIN + (TERRIBLE_MIN != TERRIBLE_MAX ? "-" + TERRIBLE_MAX : "") + ")";
                break;
            case POOR:
                desc = "I suppose. In a pinch. (" + POOR_MIN + (POOR_MIN != POOR_MAX ? "-" + POOR_MAX : "") + ")";
                break;
            case LAME:
                desc =  "Not crazy about it. (" + LAME_MIN + (LAME_MIN != LAME_MAX ? "-" + LAME_MAX : "") + ")";
                break;
            case OK:
                desc =  "Nothing wrong with it. (" + OK_MIN + (OK_MIN != OK_MAX ? "-" + OK_MAX : "") + ")";
                break;
            case GOOD:
                desc = "Yeah, that's pretty good. (" + GOOD_MIN + (GOOD_MIN != GOOD_MAX ? "-" + GOOD_MAX : "") + ")";
                break;
            case EXCELLENT:
                desc = "Now that's what I'm talking about. (" + EXCELLENT_MIN + (EXCELLENT_MIN != EXCELLENT_MAX ? "-" + EXCELLENT_MAX : "") + ")";
                break;
            case MANUAL:
                desc = "Assign the rating value from the manual slider.";
                break;
            case RESEARCH:
                desc = "Flag it for later research.";
                break;
            case SKIP:
                desc = "Skip for now.";
                break;
        }

        return desc;
    }

    public static int getRating(RATINGS r, int wordLength) {
        int rat = 0;
        if (r == XDictConfig.RATINGS.EXCELLENT) {
            switch (wordLength) {
                case 3:
                    rat = XDictConfig.EXCELLENT_3;
                    break;
                case 4:
                    rat = XDictConfig.EXCELLENT_4;
                    break;
                case 5:
                    rat = XDictConfig.EXCELLENT_5;
                    break;
                case 6:
                    rat = XDictConfig.EXCELLENT_6;
                    break;
                default:
                    rat = XDictConfig.EXCELLENT;
                    break;
            }
        } else if (r == XDictConfig.RATINGS.GOOD) {
            switch (wordLength) {
                case 3:
                    rat = XDictConfig.GOOD_3;
                    break;
                case 4:
                    rat = XDictConfig.GOOD_4;
                    break;
                case 5:
                    rat = XDictConfig.GOOD_5;
                    break;
                case 6:
                    rat = XDictConfig.GOOD_6;
                    break;
                default:
                    rat = XDictConfig.GOOD;
                    break;
            }
        } else if (r == XDictConfig.RATINGS.OK) {
            switch (wordLength) {
                case 3:
                    rat = XDictConfig.OK_3;
                    break;
                case 4:
                    rat = XDictConfig.OK_4;
                    break;
                case 5:
                    rat = XDictConfig.OK_5;
                    break;
                case 6:
                    rat = XDictConfig.OK_6;
                    break;
                default:
                    rat = XDictConfig.OK;
                    break;
            }
        } else if (r == XDictConfig.RATINGS.LAME) {
            switch (wordLength) {
                case 3:
                    rat = XDictConfig.LAME_3;
                    break;
                case 4:
                    rat = XDictConfig.LAME_4;
                    break;
                case 5:
                    rat = XDictConfig.LAME_5;
                    break;
                case 6:
                    rat = XDictConfig.LAME_6;
                    break;
                default:
                    rat = XDictConfig.LAME;
                    break;
            }
        } else if (r == XDictConfig.RATINGS.POOR) {
            switch (wordLength) {
                case 3:
                    rat = XDictConfig.POOR_3;
                    break;
                case 4:
                    rat = XDictConfig.POOR_4;
                    break;
                case 5:
                    rat = XDictConfig.POOR_5;
                    break;
                case 6:
                    rat = XDictConfig.POOR_6;
                    break;
                default:
                    rat = XDictConfig.POOR;
                    break;
            }
        } else if (r == XDictConfig.RATINGS.TERRIBLE) {
            switch (wordLength) {
                case 3:
                    rat = XDictConfig.TERRIBLE_3;
                    break;
                case 4:
                    rat = XDictConfig.TERRIBLE_4;
                    break;
                case 5:
                    rat = XDictConfig.TERRIBLE_5;
                    break;
                case 6:
                    rat = XDictConfig.TERRIBLE_6;
                    break;
                default:
                    rat = XDictConfig.TERRIBLE;
                    break;
            }
        } else if (r == XDictConfig.RATINGS.KILL) {
            rat = XDictConfig.KILL;
        }

        return rat;
    }

    public static void setNonconfiguredRatingValues() {
        if (TERRIBLE_3 < 0) TERRIBLE_3 = TERRIBLE;
        if (TERRIBLE_4 < 0) TERRIBLE_4 = TERRIBLE;
        if (TERRIBLE_5 < 0) TERRIBLE_5 = TERRIBLE;
        if (TERRIBLE_6 < 0) TERRIBLE_6 = TERRIBLE;
        if (POOR_3 < 0) POOR_3 = POOR;
        if (POOR_4 < 0) POOR_4 = POOR;
        if (POOR_5 < 0) POOR_5 = POOR;
        if (POOR_6 < 0) POOR_6 = POOR;
        if (LAME_3 < 0) LAME_3 = LAME;
        if (LAME_4 < 0) LAME_4 = LAME;
        if (LAME_5 < 0) LAME_5 = LAME;
        if (LAME_6 < 0) LAME_6 = LAME;
        if (OK_3 < 0) OK_3 = OK;
        if (OK_4 < 0) OK_4 = OK;
        if (OK_5 < 0) OK_5 = OK;
        if (OK_6 < 0) OK_6 = OK;
        if (GOOD_3 < 0) GOOD_3 = GOOD;
        if (GOOD_4 < 0) GOOD_4 = GOOD;
        if (GOOD_5 < 0) GOOD_5 = GOOD;
        if (GOOD_6 < 0) GOOD_6 = GOOD;
        if (EXCELLENT_3 < 0) EXCELLENT_3 = EXCELLENT;
        if (EXCELLENT_4 < 0) EXCELLENT_4 = EXCELLENT;
        if (EXCELLENT_5 < 0) EXCELLENT_5 = EXCELLENT;
        if (EXCELLENT_6 < 0) EXCELLENT_6 = EXCELLENT;

        TERRIBLE_MIN = min5(TERRIBLE, TERRIBLE_3, TERRIBLE_4, TERRIBLE_5, TERRIBLE_6);
        TERRIBLE_MAX = max5(TERRIBLE, TERRIBLE_3, TERRIBLE_4, TERRIBLE_5, TERRIBLE_6);
        POOR_MIN = min5(POOR, POOR_3, POOR_4, POOR_5, POOR_6);
        POOR_MAX = max5(POOR, POOR_3, POOR_4, POOR_5, POOR_6);
        LAME_MIN = min5(LAME, LAME_3, LAME_4, LAME_5, LAME_6);
        LAME_MAX = max5(LAME, LAME_3, LAME_4, LAME_5, LAME_6);
        OK_MIN = min5(OK, OK_3, OK_4, OK_5, OK_6);
        OK_MAX = max5(OK, OK_3, OK_4, OK_5, OK_6);
        GOOD_MIN = min5(GOOD, GOOD_3, GOOD_4, GOOD_5, GOOD_6);
        GOOD_MAX = max5(GOOD, GOOD_3, GOOD_4, GOOD_5, GOOD_6);
        EXCELLENT_MIN = min5(EXCELLENT, EXCELLENT_3, EXCELLENT_4, EXCELLENT_5, EXCELLENT_6);
        EXCELLENT_MAX = max5(EXCELLENT, EXCELLENT_3, EXCELLENT_4, EXCELLENT_5, EXCELLENT_6);

    }

    public static int min5(int a, int b, int c, int d, int e) {
        int val = a;
        if (b < val) val = b;
        if (c < val) val = c;
        if (d < val) val = d;
        if (e < val) val = e;

        return val;
    }

    public static int max5(int a, int b, int c, int d, int e) {
        int val = a;
        if (b > val) val = b;
        if (c > val) val = c;
        if (d > val) val = d;
        if (e > val) val = e;

        return val;
    }
}
