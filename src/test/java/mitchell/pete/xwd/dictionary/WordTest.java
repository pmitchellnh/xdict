package mitchell.pete.xwd.dictionary;

import junit.framework.TestCase;


public class WordTest extends TestCase
{
	private final static String text1 = "dogfish";
	private final static String text2 = "fishsticks";
	private final static int defaultValue = 50;
	private final static int goodValue = 75;
	private final static int badValue = 25;
	private final static int bestValue = 100;
	private final static int worstValue = 0;
	private final static int invalidValue1 = -1;
	private final static int invalidValue2 = 101;
	
	public void testCreate()
	{
		
		// Test Constructors
		Word w1 = new Word.Builder(text1).build();
		assertEquals( text1.toUpperCase(), w1.getEntry());
		Word w2 = new Word.Builder(text2).build();
		assertEquals( text2.toUpperCase(), w2.getEntry());
		// Make sure w1 didn't change
		assertEquals( text1.toUpperCase(), w1.getEntry());
	}
	
	public void testRatings()
	{
		// Test Value sets/gets
		Word w1 = new Word.Builder(text1).build();
		assertEquals( defaultValue, w1.getRating() );

		w1.setRating( goodValue );
		assertEquals( goodValue, w1.getRating() );
		
		w1.setRating( badValue );
		assertEquals( badValue, w1.getRating() );
		
		w1.setRating( invalidValue1 );
		assertEquals( badValue, w1.getRating() );	// should not have changed!
		
		w1.setRating( invalidValue2 );
		assertEquals( badValue, w1.getRating() );	// should not have changed!
		
		w1.setRating( bestValue );
		assertEquals( bestValue, w1.getRating() );	
		
		w1.setRating( worstValue );
		assertEquals( worstValue, w1.getRating() );	
	}
	
	public void testToString()
	{
		Word w1 = new Word.Builder(text1).build();
		assertEquals( "DOGFISH:50", w1.toString() );
		w1.setRating(badValue);
		assertEquals( "DOGFISH:25", w1.toString() );

	}
	
	public void testEquals()
	{
		Word w1 = new Word.Builder(text1).rating((byte)80).usedAny(true).usedNYT(false).build();
		Word w2 = new Word.Builder(text1).rating((byte)80).usedAny(true).usedNYT(false).build();
		assertTrue( w1.equals(w2));
		assertTrue( w2.equals(w1));
		w1.setUsedNYT(true);
		assertFalse( w1.equals(w2));
		assertFalse( w2.equals(w1));
	}

}
