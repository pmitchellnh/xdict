package mitchell.pete.xwd.dictionary;

import java.sql.Timestamp;
import java.util.Calendar;

public class Word 
{
	private final String entry;
	private byte rating;
	private boolean usedAny;
	private boolean usedNYT;
	private boolean needsResearch;
	private boolean manuallyRated;
	private Timestamp lastModified;
	private WordInfo info;
	
	private static Calendar calendar = Calendar.getInstance();
	public final static Timestamp NO_DATE = new Timestamp(0);
	public enum TimestampType { NOW, NEVER };
	public static final boolean MANUAL = true;
	public static final boolean AUTO = false;

	private final static byte MIN_RATING = 0;
	private final static byte MAX_RATING = 100;
	public final static byte DEFAULT_RATING = 50;

    public final static boolean WILD_OK = true;
    public final static boolean NO_WILDS = false;
	
	public static class Builder
	{
		// Required parameter
		private final String word;
		
		// Optional parameters - initialized to default values
		private byte rating = DEFAULT_RATING;
		private boolean usedAny = false;
		private boolean usedNYT = false;
		private boolean needsResearch = false;
		private boolean manuallyRated = false;
		private Timestamp lastModified = NO_DATE;
		private String comment = null;
		private WordInfo info = null;
		
		// Builder Constructor
		public Builder(String w)
		{
			this.word = w;
		}
		
		// Builder Optional Parameter Setters
		public Builder rating(byte val)	{
			if (val < MIN_RATING) {
				System.out.printf("Error: Trying to set rating [%d] less than 0. Setting to 0.\n", rating);
				val = MIN_RATING;
			}
			
			if (val > MAX_RATING) {
				System.out.printf("Error: Trying to set rating [%d] greater than 100. Setting to 100.\n", rating);
				val = MAX_RATING;
			}
				
			rating = val; 
			return this; 
		}
		public Builder usedAny(boolean val)			{ usedAny = val; return this; }
		public Builder usedNYT(boolean val)			{ usedNYT = val; return this; }
		public Builder needsResearch(boolean val)	{ needsResearch = val; return this; }
		public Builder manuallyRated(boolean val)	{ manuallyRated = val; return this; }
		public Builder lastModified(Timestamp val)	{ lastModified = val; return this; }
		public Builder wordInfo(WordInfo val)		{ info = val; return this; }
		public Builder comment(String val)			{ comment = val; return this; }
		
		public Word build()
		{
			if (lastModified == NO_DATE) {
				calendar = Calendar.getInstance();
				lastModified = new Timestamp(calendar.getTime().getTime());	// current timestamp
			}
			return new Word(this);
		}
	}
	
	private Word(Builder builder)
	{
		entry = format(builder.word, NO_WILDS);
		rating = builder.rating;
		usedAny = builder.usedAny;
		usedNYT = builder.usedNYT;
		needsResearch = builder.needsResearch;
		manuallyRated = builder.manuallyRated;
		lastModified = builder.lastModified;
		info = builder.info;
		
		if ( usedNYT )
			usedAny = true;
		
		if (builder.comment != null) {
			info = new WordInfo();
			info.setComment(builder.comment);
		}
	}

	/*
	 * Copy constructor
	 */
	public Word(Word w)
	{
		entry = w.entry;
		rating = w.rating;
		usedAny = w.usedAny;
		usedNYT = w.usedNYT;
		needsResearch = w.needsResearch;
		manuallyRated = w.manuallyRated;
		lastModified = w.lastModified;
		info = w.info;
	}
	
	public String getEntry() {
		return entry;
	}
	
	public int length() {
		return entry.length();
	}
	
	public byte getRating() {
		return rating;
	}
	
	public boolean setRating(int rating) 
	{
		if (this.rating == rating)	// No change
			return false;
		
		if ( rating < MIN_RATING || rating > MAX_RATING ) {
			System.out.printf("Error: Trying to set rating [%d] outside value range of 0 - 100\n", rating);
			return false;
		}
		else
			this.rating = (byte)rating;
		
		return true;
	}

	public boolean isUsedNYT() {
		return usedNYT;
	}

	public boolean setUsedNYT(boolean val) {
		if (usedNYT == val)	// No change
			return false;
		
		usedNYT = val;
		if ( usedNYT )
			usedAny = true;
		
		return true;
	}

	public boolean isUsedAny() {
		return usedAny;
	}

	public boolean setUsedAny(boolean val) {
		if (usedAny == val)	// No change
			return false;
		
		usedAny = val;
		
		if ( !usedAny )
			usedNYT = false;
		
		return true;
	}

	public boolean needsResearch() {
		return needsResearch;
	}

	public boolean setNeedsResearch(boolean val) {
		if (needsResearch == val)	// No change
			return false;
		
		needsResearch = val;
		
		return true;
	}
	
	public boolean isManuallyRated() {
		return manuallyRated;
	}

	public boolean setManuallyRated(boolean val) {
		if (manuallyRated == val)	// No change
			return false;
		
		manuallyRated = val;
		
		return true;
	}
	
	public Timestamp getLastModified() {
		return lastModified;
	}
	
	public void updateLastModified() {
		calendar = Calendar.getInstance();
		lastModified = new Timestamp(calendar.getTime().getTime());	// current timestamp
	}
	
	public void clearLastModified() {
		lastModified = NO_DATE;
	}
	
	public void setLastModified(Timestamp ts) {
		lastModified = ts;
	}
	
	public boolean hasComment() {
		if (getComment().isEmpty())
			return false;
		else
			return true;
	}
	
	public String getComment() {
		String c = (info == null ? null : info.getComment());

		return (c == null ? "" : c);	// Return empty string, not null
	}

	public boolean setComment(String comment) 
	{
		if (getComment().equals(comment))		// No change
			return false;
		
		if (info == null) {
			info = new WordInfo();
		}
		info.setComment(comment);
		
		return true;
	}

	public WordInfo.Sparkle getSparkle() {
		if (info != null) {
			return info.getSparkle();
		} else {
			return WordInfo.Sparkle.UNDEFINED;
		}
	}

	public WordInfo.Facility getFacility() {
		if (info != null) {
			return info.getFacility();
		} else {
			return WordInfo.Facility.UNDEFINED;
		}
	}

	public WordInfo.Currency getCurrency() {
		if (info != null) {
			return info.getCurrency();
		} else {
			return WordInfo.Currency.UNDEFINED;
		}
	}

	public WordInfo.Taste getTaste() {
		if (info != null) {
			return info.getTaste();
		} else {
			return WordInfo.Taste.UNDEFINED;
		}
	}

	/**
	 * This is the format that will be stored in exported wordlists (word:rating)
	 */
	public String toString()
	{
		String s = new String( entry + XDictConfig.EXPORT_FILE_DELIMITER + rating );
		
		return s;
	}
	
	/**
	 *	This is the format that will used for query display 
	 */
	public String toStringQuery()
	{
		String s = new String ( entry + "(" + entry.length() + "), "
				+ rating
				+ ( usedAny ? ", any " : "" )
				+ ( usedNYT ? ", nyt, " : "" )
				+ ( needsResearch ? ", research " : "" )
				+ ( manuallyRated ? ", manual " : "" )
				+ ( getComment().isEmpty() ? "" : " (" + getComment() + ")") );
		return s;
	}
	
	/**
	 *	This is the format that will used for backups (full data) 
	 */
	public String fullInfo()
	{
		String s = new String ( entry + ";" 
				+ rating + ";"
				+ ( usedAny ? "1;" : "0;" )
				+ ( usedNYT ? "1;" : "0;" )
				+ ( needsResearch ? "1;" : "0;" )
				+ ( manuallyRated ? "1;" : "0;" )
				+ lastModified.toString() + ";"
				+ ( getComment().isEmpty() ? "" : "[" + getComment() + "]") );
		return s;
	}
	
	public void dump(boolean dumpInfo)
	{
		String s = toStringQuery();
		
		System.out.println(s);

		if (dumpInfo && info != null) {
			info.dump();
		}
	}
	
	/*
	 * Format passed string -- sanitize and convert to upper case
	 */
	public static String format(String text, boolean wildOk)
	{
		text = clean(text, wildOk);
		text = text.toUpperCase();
		
		return text;
	}
	
	/*
	 * Sanitize passed string, removing any invalid characters
	 */
	private static String clean(String text, boolean wildOk)
	{
		StringBuffer sb = new StringBuffer(text);

		// work backwards, so deletes don't screw up the index
		for ( int i = text.length()-1; i >= 0; --i )
		{
			if ( !isValid( sb.charAt(i), wildOk ) )
			{
				sb.deleteCharAt(i);
			}
            if ( wildOk && sb.charAt(i) == '?' ) {
                sb.replace(i, i+1, "_");
            }
		}
		return sb.toString();
	}

	/*
	 * Check character for valid crossword entry (alphanumeric only)
	 */
	private static boolean isValid( char c, boolean wildOk )
	{
		if ( (c >= 'A' && c <= 'Z') ||
			 (c >= 'a' && c <= 'z') ||
			 (c >= '0' && c <= '9') ||
                (wildOk && (c == '_' || c == '?')))
		{
			return true;
		}
		else
			return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entry == null) ? 0 : entry.hashCode());
		result = prime * result
				+ ((lastModified == null) ? 0 : lastModified.hashCode());
		result = prime * result + (manuallyRated ? 1231 : 1237);
		result = prime * result + (needsResearch ? 1231 : 1237);
		result = prime * result + rating;
		result = prime * result + (usedAny ? 1231 : 1237);
		result = prime * result + (usedNYT ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Word other = (Word) obj;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
			return false;
		if (lastModified == null) {
			if (other.lastModified != null)
				return false;
		} else if (!lastModified.equals(other.lastModified))
			return false;
		if (manuallyRated != other.manuallyRated)
			return false;
		if (needsResearch != other.needsResearch)
			return false;
		if (rating != other.rating)
			return false;
		if (usedAny != other.usedAny)
			return false;
		if (usedNYT != other.usedNYT)
			return false;
		return true;
	}

}

