package mitchell.pete.xwd.dictionary.reconciler;

import mitchell.pete.xwd.dictionary.Word;

public class Reconciler1 implements ReconcilerInterface
{
	
	/**
	 * w1 is existing Word.  w2 is from new source.
	 * Returns true if w1 was modified, else false.
	 */
	public boolean Reconcile(Word w1, final Word w2)
	{
		boolean wasChangedDuringReconcile = false;
		// If entries don't match (not the same word), then can't reconcile
		if (!w1.getEntry().equals(w2.getEntry()))
		{
			System.out.printf("Error: Cannot reconcile entry [%s] and entry [%s]\n", w1.getEntry(), w2.getEntry());
			return false;
		}
		
		wasChangedDuringReconcile |= w1.setRating(ReconcileRating(w1, w2));
		wasChangedDuringReconcile |= w1.setUsedAny(ReconcileUsedAny(w1, w2));
		wasChangedDuringReconcile |= w1.setUsedNYT(ReconcileUsedNYT(w1, w2));
		wasChangedDuringReconcile |= w1.setNeedsResearch(ReconcileNeedsResearch(w1, w2));
		wasChangedDuringReconcile |= w1.setManuallyRated(ReconcileManuallyRated(w1, w2));
		
		// If anything was changed, update the timestamp and return true
		if (wasChangedDuringReconcile) {
			w1.updateLastModified();
			return true;
		} 
		
		return false;
	}
	
	/*
	 * If applying word is manual, favor it (whether existing is manual or not)
	 * Else if existing is manual, favor it (don't override a manual with an auto)
	 * Else favor the higher value (both auto)
	 */
	private byte ReconcileRating(Word w1, Word w2) 
	{
		byte val1 = w1.getRating();
		byte val2 = w2.getRating();
		
		if (w2.isManuallyRated())
			return val2;
		else if (w1.isManuallyRated())
			return val1;
		else	// Else take higher value
			return ( val2 > val1 ? val2 : val1 );
	}

	private boolean ReconcileUsedAny(Word w1, Word w2) 
	{
		// Allow a manually-rated word override the setting, else there would be no way to clear the flag.
		// This shouldn't be an issue, since any manually-rated entry will have any flags already set, so
		// if it's gone then it must have been removed for a reason.
		if (w2.isManuallyRated())
			return w2.isUsedAny();
		// Else if either is set, persist it
		else if ( w1.isUsedAny() || w2.isUsedAny() )
			return true;
		else
			return false;
	}

	private boolean ReconcileUsedNYT(Word w1, Word w2) {
		// Allow a manually-rated word override the setting, else there would be no way to clear the flag.
		// This shouldn't be an issue, since any manually-rated entry will have any flags already set, so
		// if it's gone then it must have been removed for a reason.
		if (w2.isManuallyRated())
			return w2.isUsedNYT();
		// Else, if either is set, persist it
		else if ( w1.isUsedNYT() || w2.isUsedNYT() )
			return true;
		else
			return false;
	}

	private boolean ReconcileNeedsResearch(Word w1, Word w2) {
		// Allow a manually-rated word override the setting, else there would be no way to clear the flag.
		// This shouldn't be an issue, since any manually-rated entry will have any flags already set, so
		// if it's gone then it must have been removed for a reason.
		if (w2.isManuallyRated())
			return w2.needsResearch();
		// Else, if either is set, persist it
		if ( w1.needsResearch() || w2.needsResearch() )
			return true;
		else
			return false;
	}

	private boolean ReconcileManuallyRated(Word w1, Word w2) {
		// If either is set, persist it. Once manually-rated, always manually-rated.
		if ( w1.isManuallyRated() || w2.isManuallyRated() )
			return true;
		else
			return false;
	}

	public boolean ReconcileComment(Word w1, Word w2) 
	{
		// Overwrite comments for manually-rated word.  Else keep what's there.
		// Should never auto-generate comments.
		if (w2.isManuallyRated()) {
			w1.setComment(w2.getComment());
			return true;
		}
		return false;
	}


}
