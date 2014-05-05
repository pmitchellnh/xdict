package mitchell.pete.xwd.dictionary.reconciler;

import junit.framework.TestCase;
import mitchell.pete.xwd.dictionary.Word;

public class Reconciler1Test extends TestCase 
{
	public Reconciler1 r = new Reconciler1();
	
	public static Word w1 = new Word.Builder("Foo")
									.rating((byte)50)
									.usedAny(true)
									.usedNYT(false)
									.needsResearch(true)
									.build();
	public static Word w2 = new Word.Builder("Foo")
									.rating((byte)75)
									.usedAny(false)
									.usedNYT(false)
									.needsResearch(false)
									.build();
	public static Word wManual = new Word.Builder("Foo")
									.rating((byte)33)
									.usedAny(false)
									.usedNYT(false)
									.manuallyRated(true)
									.needsResearch(false)
									.build();

	public static Word wDifferent = new Word.Builder("Food")
									.rating((byte)33)
									.usedAny(false)
									.usedNYT(false)
									.needsResearch(false)
									.build();
	
	public void testDifferentWords()
	{
		boolean w = r.Reconcile(w1, wDifferent);
		assertFalse(w);
	}
	
	public void testByValue()
	{
		boolean w = r.Reconcile(w1, w2);
		assertTrue(w);
		assertEquals(75, w1.getRating());	// get higher value
		assertTrue(w1.isUsedAny());
		assertFalse(w1.isUsedNYT());
		assertTrue(w1.needsResearch());
	}

	public void testWithManual()
	{
		boolean w = r.Reconcile(w1, wManual);
		assertTrue(w);
		assertEquals(33, w1.getRating());	// get manually entered value
		assertFalse(w1.isUsedAny());
		assertFalse(w1.isUsedNYT());
		assertFalse(w1.needsResearch());
	}
}
