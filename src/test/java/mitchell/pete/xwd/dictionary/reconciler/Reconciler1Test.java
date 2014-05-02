package mitchell.pete.xwd.dictionary.reconciler;

import mitchell.pete.xwd.dictionary.Word;
import mitchell.pete.xwd.dictionary.Word.TimestampType;
import junit.framework.TestCase;

public class Reconciler1Test extends TestCase 
{
	public Reconciler1 r = new Reconciler1();
	
	public static Word w1 = new Word.Builder("Foo")
									.rating(50)
									.sparkle(50)
									.facility(50)
									.currency(50)
									.taste(50)
									.used_any(true)
									.used_nyt(false)
									.needs_research(true)
									.ts_auto(TimestampType.NOW)
									.ts_manual(TimestampType.NEVER)
									.comments("This is comment #1")
									.build();
	public static Word w2 = new Word.Builder("Foo")
									.rating(25)
									.sparkle(25)
									.facility(75)
									.currency(75)
									.taste(10)
									.used_any(false)
									.used_nyt(false)
									.needs_research(false)
									.ts_auto(TimestampType.NOW)
									.ts_manual(TimestampType.NEVER)
									.comments("")
									.build();
	public static Word wManual = new Word.Builder("Foo")
									.rating(33)
									.sparkle(33)
									.facility(66)
									.currency(66)
									.taste(99)
									.used_any(false)
									.used_nyt(false)
									.needs_research(false)
									.ts_auto(TimestampType.NOW)
									.ts_manual(TimestampType.NOW)
									.comments("This is comment #3")
									.build();

	public static Word wDifferent = new Word.Builder("Food")
									.rating(33)
									.sparkle(33)
									.facility(66)
									.currency(66)
									.taste(99)
									.used_any(false)
									.used_nyt(false)
									.needs_research(false)
									.ts_auto(TimestampType.NOW)
									.ts_manual(TimestampType.NOW)
									.comments("This is comment #4")
									.build();
	
	public void testDifferentWords()
	{
		Word w = r.Reconcile(w1, wDifferent);
		assertNull(w);
	}
	
	public void testByValue()
	{
		Word w = r.Reconcile(w1, w2);
		assertEquals(50, w.getRating());	// get higher value
		assertEquals(50, w.getSparkle());	// get higher value
		assertEquals(50, w.getFacility());	// get lower value
		assertEquals(75, w.getCurrency());	// get higher value
		assertEquals(10, w.getTaste());		// get lower value
		assertTrue(w.isUsed_Any());
		assertFalse(w.isUsed_NYT());
		assertTrue(w.needsResearch());
		assertTrue( w1.getComments().equals(w.getComments()) );
	}

	public void testWithManual()
	{
		Word w = r.Reconcile(w1, wManual);
		assertEquals(33, w.getRating());	// get manually entered value
		assertEquals(33, w.getSparkle());	// get manually entered value
		assertEquals(66, w.getFacility());	// get manually entered value
		assertEquals(66, w.getCurrency());	// get manually entered value
		assertEquals(99, w.getTaste());		// get manually entered value
		assertTrue(w.isUsed_Any());
		assertFalse(w.isUsed_NYT());
		assertTrue(w.needsResearch());
		assertTrue( w.getComments().equals(w1.getComments() + " ; " + wManual.getComments()) );
	}
}
