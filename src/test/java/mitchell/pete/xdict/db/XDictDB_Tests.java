package mitchell.pete.xdict.db;

import junit.framework.TestCase;
import mitchell.pete.xdict.Word;
import mitchell.pete.xdict.db.XDictDB_Interface.LengthControl;
import mitchell.pete.xdict.db.XDictDB_Interface.MethodControl;
import mitchell.pete.xdict.db.XDictDB_Interface.PatternControl;
import mitchell.pete.xdict.db.XDictDB_Interface.RatingControl;
import mitchell.pete.xdict.db.XDictDB_Interface.ResearchControl;
import mitchell.pete.xdict.db.XDictDB_Interface.UsedControl;
import org.junit.Test;

import java.util.ArrayList;


/*
 * This class is essentially a utility class to test implementers of XDictDB_Interface,
 * so we don't have to duplicate the same tests for each implementation.
 */

public class XDictDB_Tests extends TestCase
{
	private static final int QUERY_START = 0;
	private static final int QUERY_LIMIT = 999999999;
	// just add this test so JUnit doesn't complain that there are no tests
	@Test
	public void testTrivial()
	{
		assertTrue(true);
	}
	
	public static void doTestCreate(XDictDB_Interface dict)
	{
        try {
            assertEquals(0, dict.size());
        } catch (XDictSQLException e) {
            fail(e.toString());
        }
	}
	
	public static void doTestPut(XDictDB_Interface dict)
	{
        try {
            dict.putWord("foo");
            assertEquals(1, dict.size());
            dict.putWord("foo");
            assertEquals(1, dict.size());    // shouldn't add it a second time
            dict.putWord("FoO");
            assertEquals(1, dict.size());    // even in different format
            dict.putWord("bar");
            assertEquals(2, dict.size());

            Word w1 = dict.getWord("foo");
            assertFalse(w1 == null);
            assertEquals(Word.DEFAULT_RATING, w1.getRating());

            w1 = dict.getWord("FOO");
            assertFalse(w1 == null);
            w1 = dict.getWord("Foo");
            assertFalse(w1 == null);
            w1 = dict.getWord("Fo o!!- ");
            assertFalse(w1 == null);
            w1 = dict.getWord("fo");
            assertTrue(w1 == null);
            w1 = dict.getWord("foo1");
            assertTrue(w1 == null);
            w1 = dict.getWord("bar");
            assertFalse(w1 == null);

            dict.putWord("zoology");
            w1 = dict.getWord("zoology");
            assertFalse(w1 == null);
            w1.setRating((short) 75);
            assertEquals((short) 75, w1.getRating());
            Word w2 = dict.getWord("zoology");            // make sure the DB one hasn't changed yet
            assertEquals((short) 50, w2.getRating());
            dict.putWord(w1);
            w1 = dict.getWord("zoology");
            assertEquals((short) 75, w1.getRating());

            Word w3 = new Word.Builder("xyzzyva").rating((byte) 45).build();
            dict.putWord(w3);
            assertEquals((short) 45, w3.getRating());
        } catch (XDictSQLException e) {
            fail(e.toString());
        }
	}

    public static void doTestCommentLengths(XDictDB_Interface dict)
    {
        String commentString98 = "00000000011111111112222222222333333333344444444445555555555666666666677777777778888888888999999999";
        String commentString99 = "000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999";
        String commentString100 = "0000000001111111111222222222233333333334444444444555555555566666666667777777777888888888899999999990";
        String commentString101 = "00000000011111111112222222222333333333344444444445555555555666666666677777777778888888888999999999900";
        String commentString110 = "00000000011111111112222222222333333333344444444445555555555666666666677777777778888888888999999999900000000001";

        assertEquals("Expected length of 98", 98, commentString98.length());
        assertEquals("Expected length of 99", 99, commentString99.length());
        assertEquals("Expected length of 100", 100, commentString100.length());
        assertEquals("Expected length of 101", 101, commentString101.length());
        assertEquals("Expected length of 110", 110, commentString110.length());

        try {
            Word w1 = new Word.Builder("foo").manuallyRated(true).build();
            w1.setComment(commentString98);
            dict.putWord(w1);
            Word w2 = dict.getWord("foo");
            assertEquals("Expected length of 98", commentString98.length(), w1.getComment().length());
            assertEquals("Expected length of 98", w1.getComment().length(), w2.getComment().length());

            w1.setComment(commentString99);
            dict.putWord(w1);
            w2 = dict.getWord("foo");
            assertEquals("Expected length of 99", commentString99.length(), w1.getComment().length());
            assertEquals("Expected length of 99", w1.getComment().length(), w2.getComment().length());

            w1.setComment(commentString100);
            dict.putWord(w1);
            w2 = dict.getWord("foo");
            assertEquals("Expected length of 100", commentString100.length(), w1.getComment().length());
            assertEquals("Expected length of 100", w1.getComment().length(), w2.getComment().length());

            w1.setComment(commentString101);
            dict.putWord(w1);
            w2 = dict.getWord("foo");
            assertEquals("Expected length of 101", commentString101.length(), w1.getComment().length());
            assertEquals("Expected length of 100", commentString100.length(), w2.getComment().length());

            w1.setComment(commentString110);
            dict.putWord(w1);
            w2 = dict.getWord("foo");
            assertEquals("Expected length of 110", commentString110.length(), w1.getComment().length());
            assertEquals("Expected length of 100", commentString100.length(), w2.getComment().length());

        } catch (XDictSQLException e) {
            fail(e.toString());
        }
    }
	
	public static void doTestDelete(XDictDB_Interface dict)
	{
        try {
            dict.putWord("Doc");
            dict.putWord("Happy");
            dict.putWord("Grumpy");
            dict.putWord("Sleepy");
            dict.putWord("Droopy");
            dict.putWord("Sneezy");
            dict.putWord("Bashful");
            dict.putWord("Dopey");
            assertEquals(8, dict.size());

            Word w1 = dict.getWord("droopy");
            assertFalse(w1 == null);
            dict.deleteWord("DRooPy");
            assertEquals(7, dict.size());
            Word w2 = dict.getWord("droopy");
            assertTrue(w2 == null);
        } catch (XDictSQLException e) {
            fail(e.toString());
        }
	}
	
	public static void doTestGetLists(XDictDB_Interface dict)
	{
        try {
            dict.putWord("Doc");
            dict.putWord("Happy");
            dict.putWord("Grumpy");
            dict.putWord("Sleepy");
            dict.putWord("Droopy");
            dict.putWord("Sneezy");
            dict.putWord("Bashful");
            dict.putWord("Dopey");
            assertEquals(8, dict.size());

            ArrayList<Word> list = dict.getAllWords();
            assertEquals(8, list.size());

            ArrayList<Word> list1 = dict.getWords(LengthControl.EQUALS, 6, PatternControl.ALL, "", RatingControl.ALL, 0, UsedControl.ALL, ResearchControl.ALL, MethodControl.ALL, QUERY_START, QUERY_LIMIT, false);
            assertEquals(4, list1.size());
            Word w1a = dict.getWord("Sleepy");
            Word w1b = dict.getWord("sleepy");    // shouldn't matter how we query for it
            Word w2 = dict.getWord("Bashful");
            Word w3 = dict.getWord("Happy");
            assertTrue(list1.contains(w1a));
            assertTrue(list1.contains(w1b));
            assertFalse(list1.contains(w2));
            assertFalse(list1.contains(w3));

            ArrayList<Word> list2 = dict.getWords(LengthControl.ATLEAST, 6, PatternControl.ALL, "", RatingControl.ALL, 0, UsedControl.ALL, ResearchControl.ALL, MethodControl.ALL, QUERY_START, QUERY_LIMIT, false);
            assertEquals(5, list2.size());
            assertTrue(list2.contains(w1a));
            assertTrue(list2.contains(w1b));
            assertTrue(list2.contains(w2));
            assertFalse(list2.contains(w3));

            ArrayList<Word> list3 = dict.getWords(LengthControl.ATMOST, 6, PatternControl.ALL, "", RatingControl.ALL, 0, UsedControl.ALL, ResearchControl.ALL, MethodControl.ALL, QUERY_START, QUERY_LIMIT, false);
            assertEquals(7, list3.size());
            assertTrue(list3.contains(w1a));
            assertTrue(list3.contains(w1b));
            assertFalse(list3.contains(w2));
            assertTrue(list3.contains(w3));
        } catch (XDictSQLException e) {
            fail(e.toString());
        }

	}
	
	/*
	public void testSaveLoad()
	*/
}
