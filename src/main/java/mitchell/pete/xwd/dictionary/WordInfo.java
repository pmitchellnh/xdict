package mitchell.pete.xwd.dictionary;

public class WordInfo
{
	enum Sparkle { UNDEFINED, DULL, AVERAGE, BRIGHT };
	enum Facility { UNDEFINED, EASY, MEDIUM, HARD };
	enum Currency { UNDEFINED, STALE, MODERN, FRESH };
	enum Taste { UNDEFINED, IFFY, CAUTION, EXPLICIT };
	
	private Sparkle sparkle;
	private Facility facility;
	private Currency currency;
	private Taste taste;
	private String comment;

	public WordInfo() {
		sparkle = Sparkle.UNDEFINED;
		facility = Facility.UNDEFINED;
		currency = Currency.UNDEFINED;
		taste = Taste.UNDEFINED;
		comment = null;
	}
	
	public Sparkle getSparkle() {
		return sparkle;
	}

	public void setSparkle(Sparkle sparkle) {
		this.sparkle = sparkle;
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Taste getTaste() {
		return taste;
	}

	public void setTaste(Taste taste) {
		this.taste = taste;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		if (comment.isEmpty())
			this.comment = null;	// Don't store an empty string
		else
			this.comment = comment;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (sparkle != Sparkle.UNDEFINED) {
			sb.append("Sparkle: ");
			sb.append(sparkle.toString());
		}
		if (facility != Facility.UNDEFINED) {
			sb.append("Facility: ");
			sb.append(facility.toString());
		}
		if (currency != Currency.UNDEFINED) {
			sb.append("Currency: ");
			sb.append(currency.toString());
		}
		if (taste != Taste.UNDEFINED) {
			sb.append("Taste: ");
			sb.append(taste.toString());
		}
		if (comment != null) {
			sb.append("Comments: ");
			sb.append(comment);
		}
		
		return sb.toString();
	}
	
	public void dump()
	{
				
		System.out.println(this.toString());
	}
	
}


