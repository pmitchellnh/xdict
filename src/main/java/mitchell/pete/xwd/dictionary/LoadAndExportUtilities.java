package mitchell.pete.xwd.dictionary;

import java.sql.Timestamp;
import java.util.StringTokenizer;

public class LoadAndExportUtilities {

	/**
	 * The following parses a text line based on passed delimiter.
	 * First token is interpreted as the word entry.
	 * Second token, if it exists and is numeric, is interpreted as the rating.
	 * @param delims
	 * @return
	 */
	public static Word parseWordAndRating(String line, String delims, byte defaultRating) {
		StringTokenizer st = new StringTokenizer(line, delims, false);
		String entry = "";
		byte rating = -1;
		if (st.hasMoreTokens()) {
			entry = st.nextToken();
		}
		if (st.hasMoreTokens()) {
            try {
                rating = Byte.valueOf(st.nextToken());
            } catch (NumberFormatException e) {
                if (e.getMessage().contains("Value out of range")) {
                    rating = 100;   // value too large -- assume 100 (max)
                } else {
                    rating = defaultRating;    // invalid number -- assume no value present, so set to default
                }
//                System.out.println(entry.toString() + ": " + e.getMessage());
            }
			if (rating > 100) {
                rating = 100;
			}
		}

        if (rating < 0) {
            rating = defaultRating;        // if not set, set to default
        }
//        System.out.println(entry.toString() + ": " + rating);
		Word w = new Word.Builder(entry).rating(rating).build();
	
		return w;
	}

    /**
     * The following parses a text line based on ';'.
     * @return
     */
    public static Word parseBackupEntry(String line) {
        StringTokenizer st = new StringTokenizer(line, ";", false);
        String entry = "";
        byte rating = 0;
        boolean usedAny = false;
        boolean usedNYT = false;
        boolean needsResearch = false;
        boolean manuallyRated = false;
        Timestamp timestamp = null;
        String comment = "";
        if (st.hasMoreTokens()) {
            entry = st.nextToken();
        }
        if (st.hasMoreTokens()) {
            rating = Byte.valueOf(st.nextToken());
        }
        if (st.hasMoreTokens()) {
            Byte b = Byte.valueOf(st.nextToken());
            usedAny = (b == 1);
        }
        if (st.hasMoreTokens()) {
            Byte b = Byte.valueOf(st.nextToken());
            usedNYT = (b == 1);
        }
        if (st.hasMoreTokens()) {
            Byte b = Byte.valueOf(st.nextToken());
            needsResearch = (b == 1);
        }
        if (st.hasMoreTokens()) {
            Byte b = Byte.valueOf(st.nextToken());
            manuallyRated = (b == 1);
        }
        if (st.hasMoreTokens()) {
            timestamp = Timestamp.valueOf(st.nextToken());
        }
        if (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() <3)
                comment = null;
            else
                comment = token.substring(1,token.length() - 1);
        }
        Word w = new Word.Builder(entry).rating(rating).usedAny(usedAny).usedNYT(usedNYT).needsResearch(needsResearch).manuallyRated(manuallyRated).lastModified(timestamp).comment(comment).build();

        return w;
    }

}
