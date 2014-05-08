package mitchell.pete.xwd.dictionary;

import java.util.StringTokenizer;

public class LoadAndExportUtilities {
	
	/**
	 * The theory is that, in general, short words can't be too good or too bad.  In other words,
	 * when rating a grid, pay more attention to the longer words, as they are what people notice more.
	 * This won't preclude re-rating any words manually, but will help control file loads.
	 */
	public static final byte MIN_5_LETTER_RATING = 25;
	public static final byte MIN_4_LETTER_RATING = 35;
	public static final byte MIN_3_LETTER_RATING = 45;
	public static final byte MAX_3_LETTER_RATING = 55;
	public static final byte MAX_4_LETTER_RATING = 60;
	public static final byte MAX_5_LETTER_RATING = 65;
	

	/**
	 * The following parses a text line based on passed delimiter.
	 * First token is interpreted as the word entry.
	 * Second token, if it exists and is numeric, is interpreted as the rating.
	 * @param delims
	 * @return
	 */
	public static Word parseWordAndRating(String line, String delims) {
		StringTokenizer st = new StringTokenizer(line, delims, false);
		String entry = "";
		byte rating = 0;
		if (st.hasMoreTokens()) {
			entry = st.nextToken();
		}
		if (st.hasMoreTokens()) {
			rating = Byte.valueOf(st.nextToken());
			if (rating > 0) {
				rating = normalizeRating(entry, rating);
			}
		}
		Word w = new Word.Builder(entry).rating(rating).build();
	
		return w;
	}
	
	/*
	 * Adjust rating based on word length.
	 */
	public static byte normalizeRating(String entry, byte rating) {
		if (entry.length() == 3 && rating < MIN_3_LETTER_RATING)
			return MIN_3_LETTER_RATING;
		if (entry.length() == 3 && rating > MAX_3_LETTER_RATING)
			return MAX_3_LETTER_RATING;
		
		if (entry.length() == 4 && rating < MIN_4_LETTER_RATING)
			return MIN_4_LETTER_RATING;
		if (entry.length() == 4 && rating > MAX_4_LETTER_RATING)
			return MAX_4_LETTER_RATING;

		if (entry.length() == 5 && rating < MIN_5_LETTER_RATING)
			return MIN_5_LETTER_RATING;
		if (entry.length() == 5 && rating > MAX_5_LETTER_RATING)
			return MAX_5_LETTER_RATING;
		
		return rating;
	}
}
