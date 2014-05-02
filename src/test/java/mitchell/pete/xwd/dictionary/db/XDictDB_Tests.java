package mitchell.pete.xwd.dictionary.db;

import java.util.ArrayList;

import mitchell.pete.xwd.dictionary.Word;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.LengthControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.PatternControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.ResearchControl;
import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.UsedControl;
import junit.framework.TestCase;

/*
 * This class is essentially a utility class to test implementers of XDictDB_Interface,
 * so we don't have to duplicate the same tests for each implementation.
 */

public class XDictDB_Tests extends TestCase
{
	// just add this test so JUnit doesn't complain that there are no tests
	public void testTrivial()
	{
		assertTrue(true);
	}
	
	public static void doTestCreate(XDictDB_Interface dict)
	{
		assertEquals( 0, dict.size() );
	}
	
	public static void doTestPut(XDictDB_Interface dict)
	{
		dict.putWord( "foo", Word.AUTO);
		assertEquals( 1, dict.size() );
		dict.putWord( "foo", Word.AUTO);
		assertEquals( 1, dict.size() );	// shouldn't add it a second time
		dict.putWord( "FoO", Word.AUTO);
		assertEquals( 1, dict.size() );	// even in different format
		dict.putWord( "bar", Word.AUTO);
		assertEquals( 2, dict.size() );
		
		Word w1 = dict.getWord("foo");
		assertFalse( w1 == null );
		assertEquals( Word.DEFAULT_RATING, w1.getRating() );
		
		w1 = dict.getWord("FOO");
		assertFalse( w1 == null );
		w1 = dict.getWord("Foo");
		assertFalse( w1 == null );
		w1 = dict.getWord("Fo o!!- ");
		assertFalse( w1 == null );
		w1 = dict.getWord("fo");
		assertTrue( w1 == null );
		w1 = dict.getWord("foo1");
		assertTrue( w1 == null );
		w1 = dict.getWord("bar");
		assertFalse( w1 == null );
		
		dict.putWord("zoology", Word.MANUAL );
		w1 = dict.getWord("zoology");
		assertFalse( w1 == null );
		w1.setRating((short)75);
		assertEquals( (short)75, w1.getRating() );
		Word w2 = dict.getWord("zoology");			// make sure the DB one hasn't changed yet
		assertEquals( (short)50, w2.getRating() );
		dict.putWord( w1, Word.MANUAL );
		w1 = dict.getWord("zoology");
		assertEquals( (short)75, w1.getRating() );
		
		Word w3 = new Word.Builder("xyzzyva").rating((short)45).facility((short)90).sparkle((short)65).build();
		dict.putWord(w3, Word.MANUAL);
		assertEquals( (short)45, w3.getRating() );
		assertEquals( (short)90, w3.getFacility() );
		assertEquals( (short)65, w3.getSparkle() );
		assertEquals( Word.DEFAULT_RATING, w3.getCurrency() );
		assertEquals( Word.DEFAULT_RATING, w3.getTaste() );
	}
	
	public static void doTestDelete(XDictDB_Interface dict)
	{
		dict.putWord("Doc", Word.AUTO);
		dict.putWord("Happy", Word.AUTO);
		dict.putWord("Grumpy", Word.AUTO);
		dict.putWord("Sleepy", Word.AUTO);
		dict.putWord("Droopy", Word.AUTO);
		dict.putWord("Sneezy", Word.AUTO);
		dict.putWord("Bashful", Word.AUTO);
		dict.putWord("Dopey", Word.AUTO);
		assertEquals( 8, dict.size() );
		
		Word w1 = dict.getWord("droopy");
		assertFalse( w1 == null );
		dict.deleteWord("DRooPy");
		assertEquals( 7, dict.size() );
		Word w2 = dict.getWord("droopy");
		assertTrue( w2 == null );
	}
	
	public static void doTestGetLists(XDictDB_Interface dict)
	{
		dict.putWord("Doc", Word.AUTO);
		dict.putWord("Happy", Word.AUTO);
		dict.putWord("Grumpy", Word.AUTO);
		dict.putWord("Sleepy", Word.AUTO);
		dict.putWord("Droopy", Word.AUTO);
		dict.putWord("Sneezy", Word.AUTO);
		dict.putWord("Bashful", Word.AUTO);
		dict.putWord("Dopey", Word.AUTO);
		assertEquals( 8, dict.size() );
		
		ArrayList<Word> list = dict.getAllWords();
		assertEquals( 8, list.size() );
		
		ArrayList<Word> list1 = dict.getWords(LengthControl.EQUALS, 6, PatternControl.ALL, "", UsedControl.ALL, ResearchControl.ALL);
		assertEquals( 4, list1.size() );
		Word w1a = dict.getWord("Sleepy");
		Word w1b = dict.getWord("sleepy");	// shouldn't matter how we query for it
		Word w2 = dict.getWord("Bashful");
		Word w3 = dict.getWord("Happy");
		assertTrue( list1.contains(w1a) );
		assertTrue( list1.contains(w1b) );
		assertFalse( list1.contains(w2) );
		assertFalse( list1.contains(w3) );

		ArrayList<Word> list2 = dict.getWords(LengthControl.ATLEAST, 6, PatternControl.ALL, "", UsedControl.ALL, ResearchControl.ALL);
		assertEquals( 5, list2.size() );
		assertTrue( list2.contains(w1a) );
		assertTrue( list2.contains(w1b) );
		assertTrue( list2.contains(w2) );
		assertFalse( list2.contains(w3) );

		ArrayList<Word> list3 = dict.getWords(LengthControl.ATMOST, 6, PatternControl.ALL, "", UsedControl.ALL, ResearchControl.ALL);
		assertEquals( 7, list3.size() );
		assertTrue( list3.contains(w1a) );
		assertTrue( list3.contains(w1b) );
		assertFalse( list3.contains(w2) );
		assertTrue( list3.contains(w3) );
	}
	
	/*
	public void testSaveLoad()
	*/
}
