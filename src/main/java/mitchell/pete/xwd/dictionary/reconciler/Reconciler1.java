package mitchell.pete.xwd.dictionary.reconciler;

import mitchell.pete.xwd.dictionary.Word;

public class Reconciler1 implements ReconcilerInterface
{
	private static final boolean USE_AUTO_TIMESTAMP = true;
	private static final boolean IGNORE_AUTO_TIMESTAMP = false;
	
	public Word Reconcile(Word w1, Word w2)
	{
		// If entries don't match (not the same word), then can't reconcile
		if ( !w1.getEntry().equals( w2.getEntry() ))
		{
			System.out.printf("Error: Cannot reconcile entry [%s] and entry [%s]\n", w1.getEntry(), w2.getEntry());
			return null;
		}
		Word w = new Word(w1);	// default to w1; change as necessary
		
		w.setDisplay( ReconcileDisplay(w1, w2) );
		w.setRating(ReconcileRating(w1, w2) );
		w.setSparkle(ReconcileSparkle(w1, w2) );
		w.setFacility(ReconcileFacility(w1, w2) );
		w.setCurrency(ReconcileCurrency(w1, w2) );
		w.setTaste(ReconcileTaste(w1, w2) );
		w.setUsed_Any(ReconcileUsed_Any(w1, w2) );
		w.setUsed_NYT(ReconcileUsed_NYT(w1, w2) );
		w.setNeedsResearch(ReconcileNeedsResearch(w1, w2) );
		w.setComments(ReconcileComments(w1, w2) );
		
		// For time stamps, override if w2 is more recent
		if ( w2.getTS_Manual().after( w1.getTS_Manual() ))
			w.setTS_Manual( w2.getTS_Manual() );

		if ( w2.getTS_Auto().after( w1.getTS_Auto() ))
			w.setTS_Auto( w2.getTS_Auto() );

		return w;
	}
	
	private String ReconcileDisplay( Word w1, Word w2 )
	{
		// If they're the same, it doesn't matter
		if ( w1.getDisplay().equals(w2.getDisplay() ) )
			return w1.getDisplay();
		
		// If one is the same as the Entry and one is not, prefer the one that is not.
		if ( w2.getDisplay().equals(w1.getEntry()) && !w1.getDisplay().equals(w1.getEntry()) )
			return w1.getDisplay();
		if ( w1.getDisplay().equals(w1.getEntry()) && !w2.getDisplay().equals(w1.getEntry()) )
			return w2.getDisplay();
		
		// If we get here, then they're both different, and neither matches the entry.
		// So, decide based on time stamps
		if ( compareBasedOnTimeStamps( w1, w2, USE_AUTO_TIMESTAMP ) > 0 )
			return w2.getDisplay();
		else
			return w1.getDisplay();
		
	}
	
	private short ReconcileRating(Word w1, Word w2) 
	{
		short val1 = w1.getRating();
		short val2 = w2.getRating();
		
		// Reconcile based on manual time stamps
		if ( compareBasedOnTimeStamps( w1, w2, IGNORE_AUTO_TIMESTAMP ) > 0 )
			return val2;
		
		if ( compareBasedOnTimeStamps( w1, w2, IGNORE_AUTO_TIMESTAMP ) < 0 )
			return val1;
		
		// Else take higher value
		return ( val2 > val1 ? val2 : val1 );
	}

	private short ReconcileSparkle(Word w1, Word w2) 
	{
		short val1 = w1.getSparkle();
		short val2 = w2.getSparkle();
		
		// Reconcile based on manual time stamps
		if ( compareBasedOnTimeStamps( w1, w2, IGNORE_AUTO_TIMESTAMP ) > 0 )
			return val2;
		
		if ( compareBasedOnTimeStamps( w1, w2, IGNORE_AUTO_TIMESTAMP ) < 0 )
			return val1;
		
		// Else take higher value
		return ( val2 > val1 ? val2 : val1 );
	}

	private short ReconcileFacility(Word w1, Word w2) 
	{
		short val1 = w1.getFacility();
		short val2 = w2.getFacility();
		
		// Reconcile based on manual time stamps
		if ( compareBasedOnTimeStamps( w1, w2, IGNORE_AUTO_TIMESTAMP ) > 0 )
			return val2;
		
		if ( compareBasedOnTimeStamps( w1, w2, IGNORE_AUTO_TIMESTAMP ) < 0 )
			return val1;
		
		// Else take lower value (err on side of more difficult)
		return ( val2 < val1 ? val2 : val1 );
	}

	private short ReconcileCurrency(Word w1, Word w2) 
	{
		short val1 = w1.getCurrency();
		short val2 = w2.getCurrency();
		
		// Reconcile based on manual time stamps
		if ( compareBasedOnTimeStamps( w1, w2, IGNORE_AUTO_TIMESTAMP ) > 0 )
			return val2;
		
		if ( compareBasedOnTimeStamps( w1, w2, IGNORE_AUTO_TIMESTAMP ) < 0 )
			return val1;
		
		// Else take higher value
		return ( val2 > val1 ? val2 : val1 );
	}

	private short ReconcileTaste(Word w1, Word w2) 
	{
		short val1 = w1.getTaste();
		short val2 = w2.getTaste();
		
		// Reconcile based on manual time stamps
		if ( compareBasedOnTimeStamps( w1, w2, IGNORE_AUTO_TIMESTAMP ) > 0 )
			return val2;
		
		if ( compareBasedOnTimeStamps( w1, w2, IGNORE_AUTO_TIMESTAMP ) < 0 )
			return val1;
		
		// Else take lower value (be safer on matters of taste)
		return ( val2 < val1 ? val2 : val1 );
	}

	private String ReconcileComments( Word w1, Word w2 )
	{
		// If they're the same, it doesn't matter
		if ( w1.getComments().equals(w2.getComments() ) )
			return w1.getComments();
		
		// If one is empty, prefer the one that is not.
		if ( w2.getComments().isEmpty() && !w1.getComments().isEmpty() )
			return w1.getComments();
		if ( !w2.getComments().isEmpty() && w1.getComments().isEmpty() )
			return w2.getComments();
		
		// If we get here, then they're both different
		// So, concatenate them
		return w1.getComments() + " ; " + w2.getComments();
	}
	
	private boolean ReconcileUsed_Any(Word w1, Word w2) 
	{
		// If either is set, persist it
		if ( w1.isUsed_Any() || w2.isUsed_Any() )
			return true;
		else
			return false;
	}

	private boolean ReconcileUsed_NYT(Word w1, Word w2) {
		// If either is set, persist it
		if ( w1.isUsed_NYT() || w2.isUsed_NYT() )
			return true;
		else
			return false;
	}

	private boolean ReconcileNeedsResearch(Word w1, Word w2) {
		// If either is set, persist it
		if ( w1.needsResearch() || w2.needsResearch() )
			return true;
		else
			return false;
	}

	/*
	 * Return -1 if w1 is preferred; +1 if w2 is preferred; else 0 (no preference)
	 * useAutoTS = express a preference based on timestamp of automated add or change (else no preference)
	 */
	private short compareBasedOnTimeStamps( Word w1, Word w2, boolean useAutoTS )
	{
		// If both were manually edited, prefer the more recently edited one 
		if ( !w1.getTS_Manual().equals(Word.NO_DATE) && !w2.getTS_Manual().equals(Word.NO_DATE) )
		{
			if (w1.getTS_Manual().after( w2.getTS_Manual() ))
				return -1;
			else
				return 1;
		}
		
		// If one was manually edited and one was not, prefer the one that is
		if ( !w1.getTS_Manual().equals(Word.NO_DATE) )
			return -1;
		
		if ( !w2.getTS_Manual().equals(Word.NO_DATE) )
			return 1;
		
		if ( useAutoTS )
		{
			// If neither were manually edited, prefer the more recently added (or auto-modified) one 
			if (w1.getTS_Manual().after( w2.getTS_Manual() ))
				return -1;
			else if (w1.getTS_Manual().before( w2.getTS_Manual() ))
				return 1;
		}
		
		return 0;	// no preference
	}
}
