package mitchell.pete.xwd.dictionary.db;

import java.io.*;
import java.util.*;

import mitchell.pete.xwd.dictionary.Word;
import mitchell.pete.xwd.dictionary.reconciler.Reconciler1;

public class XDictDB_Memory implements XDictDB_Interface 
{
	private static DBControl dbControl = DBControl.TESTING_ONLY;	// default to most restrictive mode
	private TreeMap<String, Word> wordMap;
	private static String FILENAME = "";
	private Reconciler1 reconciler = new Reconciler1();

	public XDictDB_Memory(String path, DBControl flag )
	{
		wordMap = new TreeMap<String, Word>();
		FILENAME = path;
	}
	
	@Override
	public boolean connect() 
	{
		try {
			
			if ( dbControl == DBControl.TESTING_ONLY )		// if testing; delete existing DB
				clear_AreYouSureYouWantToDoThis();

			load(FILENAME);
			return true;
		} catch (IOException e) {
			System.out.printf("Error opening or reading from file [%s]\n", FILENAME );
		}
		
		return false;
	}

	@Override
	public void disconnect() 
	{
		try {
			save(FILENAME);
		} catch (IOException e) {
			System.out.printf("Error writing to file [%s]\n", FILENAME );
		}
	}

	@Override
	public void clear_AreYouSureYouWantToDoThis()		// delete table and start fresh
	{
		File f = new File(FILENAME);

	    if (f.exists() && f.canWrite() && !f.isDirectory())
	    	f.delete();
	}

	@Override
	public ArrayList<Word> getAllWords() 
	{
		ArrayList<Word> words = new ArrayList<Word>();

		for ( Word w : wordMap.values() )
		{
			words.add(w);
		}
		
		return words;
	}

	@Override
	public Word getWord(String s) 
	{
		String key = Word.format(s);
		
		Word w = wordMap.get(key);
		
		if ( w == null )
			return null;
		else
			return new Word(w);
	}

	@Override
	public ArrayList<Word> getWords(LengthControl lc, int l, PatternControl pc,	String s, UsedControl uc, ResearchControl rc) 
	{
		ArrayList<Word> words = new ArrayList<Word>();
		String key = Word.format(s);
		
		if ( pc == PatternControl.EQUALS )
		{
			Word w = wordMap.get(key);
			if ( w != null )
				words.add(w);
			return words;
		}

		for ( Word w : wordMap.values() )
		{
			if ( (	lc == LengthControl.ALL || 
					( lc == LengthControl.ATLEAST && w.getEntry().length() >= l ) ||
					( lc == LengthControl.ATMOST && w.getEntry().length() <= l ) ||
					( lc == LengthControl.EQUALS && w.getEntry().length() == l ) ) &&
				 (  pc == PatternControl.ALL ||
				 	( pc == PatternControl.EQUALS && w.getEntry().equals(key) ) ||
				 	( pc == PatternControl.STARTSWITH && w.getEntry().startsWith(key) ) ||
				 	( pc == PatternControl.CONTAINS && w.getEntry().contains(key) ) ) &&
				 (	uc == UsedControl.ALL || 
					( uc == UsedControl.USED_ANY && w.isUsed_Any() ) ||
					( uc == UsedControl.USED_NYT && w.isUsed_NYT() ) ) &&
				 (	rc == ResearchControl.ALL ||
					( rc == ResearchControl.NEEDS_RESEARCH && w.needsResearch() ) ||
					( rc == ResearchControl.NO_RESEARCH && !w.needsResearch() ) ))
				words.add(w);
		}
		
		return words;
	}

	@Override
	public int putWord(Word w, boolean isManual ) 
	{
		String key = w.getEntry();
		
		if ( wordMap.containsKey(key) )
		{
			Word mergedWord = reconciler.Reconcile( wordMap.get(key), w);
			wordMap.put(key, mergedWord);
			return mergedWord.getRating();
		}
		else
		{
			wordMap.put(key, w);
			return w.getRating();
		}
	}

	@Override
	public int putWord( String s, boolean isManual ) 
	{
		String key = Word.format(s);
		
		if ( wordMap.containsKey(key) )
		{
			Word w = wordMap.get(key);
			return w.getRating();
		}
		else
		{
			Word w = new Word.Builder(key).build();
			wordMap.put(key, w);
			return w.getRating();
		}
	}

	@Override
	public void deleteWord(String s) 
	{
		String key = Word.format(s);
		
		wordMap.remove(key);
	}

	@Override
	public int size() 
	{
		return wordMap.size();
	}

	public void save( String filename ) throws IOException
	{
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter( filename ));
		try
		{
			for ( Word w : wordMap.values() )
			{
				bufferedWriter.write( w.getEntry() );
				bufferedWriter.write( ";" );
				bufferedWriter.write( Integer.toString( w.getRating() ) );
				bufferedWriter.newLine();
			}
		}
		finally
		{
			bufferedWriter.close();
		}
	}
	
	public void load( String filename ) throws IOException
	{
		BufferedReader bufferedReader = new BufferedReader(new FileReader( filename ));
		
		try
		{
			String s;
			short value;
			while ( ( s = bufferedReader.readLine() ) != null )
			{
				StringTokenizer st = new StringTokenizer( s, ";");
				String text = st.nextToken();
				try
				{
					value = Short.parseShort( st.nextToken() );
				} catch ( NoSuchElementException e ) {
					value = Word.DEFAULT_RATING;
				}
				Word w = new Word.Builder(text).rating(value).build();
				putWord( w, Word.AUTO );
			}
		}
		finally
		{
			bufferedReader.close();
		}
		
	}
	
}

