package mitchell.pete.xwd.dictionary;

import java.sql.Timestamp;
import java.util.Calendar;

public class Word 
{
	private final String entry;
	private String display;
	private short rating;
	private short sparkle;
	private short facility;
	private short currency;
	private short taste;
	private boolean used_any;
	private boolean used_nyt;
	private boolean needs_research;
	private String comments;
	private Timestamp ts_manual;
	private Timestamp ts_auto;
	
	private static Calendar calendar = Calendar.getInstance();
	public final static Timestamp NO_DATE = new Timestamp(0);
	public enum TimestampType { NOW, NEVER };
	public static final boolean MANUAL = true;
	public static final boolean AUTO = false;

	private final static short MIN_RATING = 0;
	private final static short MAX_RATING = 100;
	public final static short DEFAULT_RATING = 50;
	
	public static class Builder
	{
		// Required parameter
		private final String word;
		
		// Optional parameters - initialized to default values
		private int rating = DEFAULT_RATING;
		private int sparkle = DEFAULT_RATING;
		private int facility = DEFAULT_RATING;
		private int currency = DEFAULT_RATING;
		private int taste = DEFAULT_RATING;
		private boolean used_any = false;
		private boolean used_nyt = false;
		private boolean needs_research = false;
		private String comments = "";
		private Timestamp ts_manual = new Timestamp(0);		// init to smallest date possible
		private Timestamp ts_auto = new Timestamp(0);		// init to smallest date possible		
		public Builder(String w)
		{
			this.word = w;
		}
		public Builder rating(int val)			{ rating = val; return this; }
		public Builder sparkle(int val)			{ sparkle = val; return this; }
		public Builder facility(int val)		{ facility = val; return this; }
		public Builder currency(int val)		{ currency = val; return this; }
		public Builder taste(int val)			{ taste = val; return this; }
		public Builder used_any(boolean val)	{ used_any = val; return this; }
		public Builder used_nyt(boolean val)	{ used_nyt = val; return this; }
		public Builder needs_research(boolean val)	{ needs_research = val; return this; }
		public Builder comments(String val)		{ comments = val; return this; }
		public Builder ts_manual(Timestamp val)	{ ts_manual = val; return this; }
		public Builder ts_manual(TimestampType val)
		{
			if ( val == TimestampType.NEVER )
				ts_manual = new Timestamp(0);
			
			if ( val == TimestampType.NOW )
			{
				calendar = Calendar.getInstance();
				ts_manual = new Timestamp(calendar.getTime().getTime());	// current timestamp
			}
			
			return this;
		}
		public Builder ts_auto(Timestamp val)	{ ts_auto = val; return this; }
		public Builder ts_auto(TimestampType val)
		{
			if ( val == TimestampType.NEVER )
				ts_auto = new Timestamp(0);
			
			if ( val == TimestampType.NOW )
			{
				calendar = Calendar.getInstance();
				ts_auto = new Timestamp(calendar.getTime().getTime());	// current timestamp
			}
			
			return this;
		}
		
		public Word build()
		{
			return new Word(this);
		}
	}
	
	/*
	 * Copy constructor
	 */
	public Word(Word w)
	{
		entry = w.entry;
		display = w.display;
		rating = w.rating;
		sparkle = w.sparkle;
		facility = w.facility;
		currency = w.currency;
		taste = w.taste;
		used_any = w.used_any;
		used_nyt = w.used_nyt;
		needs_research = w.needs_research;
		comments = w.comments;
		ts_manual = w.ts_manual;
		ts_auto = w.ts_auto;
	}
	
	private Word(Builder builder)
	{
		entry = format(builder.word);
		display = builder.word;
		rating = (short)builder.rating;
		sparkle = (short)builder.sparkle;
		facility = (short)builder.facility;
		currency = (short)builder.currency;
		taste = (short)builder.taste;
		used_any = builder.used_any;
		used_nyt = builder.used_nyt;
		needs_research = builder.needs_research;
		comments = builder.comments;
		ts_manual = builder.ts_manual;
		ts_auto = builder.ts_auto;
		
		if ( used_nyt )
			used_any = true;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) 
	{
		if ( format(display).equals(entry))
			this.display = display;
		else	// this display doesn't match the entry!!
			System.out.printf("Error: Trying to set display [%s] that does not match entry [%s]\n", display, entry);
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) 
	{
		this.comments = comments;
	}

	public short getRating() {
		return rating;
	}

	public void setRating(int rating) 
	{
		if ( rating < MIN_RATING || rating > MAX_RATING )
			System.out.printf("Error: Trying to set rating [%d] outside value range of 0 - 100\n", rating);
		else
			this.rating = (short)rating;
	}

	public short getSparkle() {
		return sparkle;
	}

	public void setSparkle(int sparkle) {
		if ( sparkle < MIN_RATING || sparkle > MAX_RATING )
			System.out.printf("Error: Trying to set sparkle [%d] outside value range of 0 - 100\n", rating);
		else
			this.sparkle = (short)sparkle;
	}

	public short getFacility() {
		return facility;
	}

	public void setFacility(int facility) {
		if ( facility < MIN_RATING || facility > MAX_RATING )
			System.out.printf("Error: Trying to set facility [%d] outside value range of 0 - 100\n", rating);
		else
			this.facility = (short)facility;
	}

	public short getCurrency() {
		return currency;
	}

	public void setCurrency(int currency) {
		if ( currency < MIN_RATING || currency > MAX_RATING )
			System.out.printf("Error: Trying to set currency [%d] outside value range of 0 - 100\n", rating);
		else
			this.currency = (short)currency;
	}

	public short getTaste() {
		return taste;
	}

	public void setTaste(int taste) {
		if ( taste < MIN_RATING || taste > MAX_RATING )
			System.out.printf("Error: Trying to set taste [%d] outside value range of 0 - 100\n", rating);
		else
			this.taste = (short)taste;
	}

	public boolean isUsed_NYT() {
		return used_nyt;
	}

	public void setUsed_NYT(boolean usedNyt) {
		used_nyt = usedNyt;
		if ( used_nyt )
			used_any = true;
	}

	public boolean isUsed_Any() {
		return used_any;
	}

	public void setUsed_Any(boolean usedAny) {
		used_any = usedAny;
		
		if ( !used_any )
			used_nyt = false;
	}

	public boolean needsResearch() {
		return needs_research;
	}

	public void setNeedsResearch(boolean val) {
		needs_research = val;
	}

	public Timestamp getTS_Manual() {
		return ts_manual;
	}
	
	public boolean isTS_Manual() {
		return ( getTS_Manual().equals(NO_DATE) ? false : true );
	}

	public void setTS_Manual(Timestamp tsManual) {
		ts_manual = tsManual;
	}

	public void setTS_Manual(TimestampType val)
	{
		if ( val == TimestampType.NEVER )
			ts_manual = new Timestamp(0);
		
		if ( val == TimestampType.NOW )
		{
			calendar = Calendar.getInstance();
			ts_manual = new Timestamp(calendar.getTime().getTime());	// current timestamp
		}
	}
	
	public Timestamp getTS_Auto() {
		return ts_auto;
	}

	public void setTS_Auto(Timestamp tsAuto) {
		ts_auto = tsAuto;
	}

	public void setTS_Auto(TimestampType val)
	{
		if ( val == TimestampType.NEVER )
			ts_auto = new Timestamp(0);
		
		if ( val == TimestampType.NOW )
		{
			calendar = Calendar.getInstance();
			ts_auto = new Timestamp(calendar.getTime().getTime());	// current timestamp
		}
	}
	
	public String getEntry() {
		return entry;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((comments == null) ? 0 : comments.hashCode());
		result = prime * result + currency;
		result = prime * result + ((display == null) ? 0 : display.hashCode());
		result = prime * result + ((entry == null) ? 0 : entry.hashCode());
		result = prime * result + facility;
		result = prime * result + (needs_research ? 1231 : 1237);
		result = prime * result + rating;
		result = prime * result + sparkle;
		result = prime * result + taste;
		result = prime * result + ((ts_auto == null) ? 0 : ts_auto.hashCode());
		result = prime * result
				+ ((ts_manual == null) ? 0 : ts_manual.hashCode());
		result = prime * result + (used_any ? 1231 : 1237);
		result = prime * result + (used_nyt ? 1231 : 1237);
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
		if (comments == null) {
			if (other.comments != null)
				return false;
		} else if (!comments.equals(other.comments))
			return false;
		if (currency != other.currency)
			return false;
		if (display == null) {
			if (other.display != null)
				return false;
		} else if (!display.equals(other.display))
			return false;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
			return false;
		if (facility != other.facility)
			return false;
		if (needs_research != other.needs_research)
			return false;
		if (rating != other.rating)
			return false;
		if (sparkle != other.sparkle)
			return false;
		if (taste != other.taste)
			return false;
		if (ts_auto == null) {
			if (other.ts_auto != null)
				return false;
		} else if (!ts_auto.equals(other.ts_auto))
			return false;
		if (ts_manual == null) {
			if (other.ts_manual != null)
				return false;
		} else if (!ts_manual.equals(other.ts_manual))
			return false;
		if (used_any != other.used_any)
			return false;
		if (used_nyt != other.used_nyt)
			return false;
		return true;
	}

	/**
	 * This is the format that will be stored in wordlists (word:rating)
	 */
	public String toString()
	{
		String s = new String( entry + ":" + rating );
		
		return s;
	}
	
	public void dump()
	{
		String s = new String ( entry + ","
				+ display + ","
				+ rating + ","
				+ sparkle + ","
				+ facility + ","
				+ currency + ","
				+ taste + ","
				+ ( used_any ? "any" : "" ) + ","
				+ ( used_nyt ? "nyt" : "" ) + ","
				+ ( needs_research ? "research" : "" ) + ","
				+ comments + ","
				+ ts_manual.toString() + ","
				+ ts_auto.toString() );
		
		System.out.println(s);
	}
	
	/*
	 * Format passed string -- sanitize and convert to upper case
	 */
	public static String format(String text)
	{
		text = clean(text);
		text = text.toUpperCase();
		
		return text;
	}
	
	/*
	 * Sanitize passed string, removing any invalid characters
	 */
	private static String clean(String text)
	{
		StringBuffer sb = new StringBuffer(text);

		// work backwards, so deletes don't screw up the index
		for ( int i = text.length()-1; i >= 0; --i )
		{
			if ( !isValid( sb.charAt(i) ) )
			{
				sb.deleteCharAt(i);
			}
		}
		return sb.toString();
	}

	/*
	 * Check character for valid crossword entry (alphanumeric only)
	 */
	private static boolean isValid( char c )
	{
		if ( (c >= 'A' && c <= 'Z') ||
			 (c >= 'a' && c <= 'z') ||
			 (c >= '0' && c <= '9') )
		{
			return true;
		}
		else
			return false;
	}

}

