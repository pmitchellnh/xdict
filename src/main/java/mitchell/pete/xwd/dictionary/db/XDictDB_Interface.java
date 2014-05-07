package mitchell.pete.xwd.dictionary.db;

import java.util.ArrayList;

import mitchell.pete.xwd.dictionary.Word;

public interface XDictDB_Interface 
{
	enum DBControl { CREATE_IF_NEEDED, OPEN_ONLY, TESTING_ONLY };	// TESTING_ONLY will delete/create!!
	
	enum LengthControl { ALL, EQUALS, ATLEAST, ATMOST };		// used to build queries
	enum PatternControl { ALL, EQUALS, STARTSWITH, CONTAINS };	// used to build queries
	enum RatingControl { ALL, ATLEAST, ATMOST };				// used to build queries
	enum UsedControl { ALL, USED_ANY, USED_NYT };			// used to build queries
	enum ResearchControl { ALL, NEEDS_RESEARCH, NO_RESEARCH };	// use to build queries
	enum MethodControl { ALL, MANUAL, AUTOMATIC };			// used to build queries
	enum WORD_STATUS { NEW, EXISTS, DUPLICATE };			// used to distinguish adds from changes and no-ops


	WORD_STATUS putWord( Word w );						// add or update word; return rating
	WORD_STATUS putWord( String s );					// add word with default values, if not there; else no-op
	Word getWord( String s );					// find word and return it
	void deleteWord( String s);					// delete word
	int size();									// return number of entries in DB
	ArrayList<Word> getWords( LengthControl lenCtrl, int len, 
							PatternControl patCtrl, String s, 
							RatingControl ratCtrl, int rat,
							UsedControl useCtrl,
							ResearchControl resCtrl,
							MethodControl methCtrl,
							int start,
							int limit);	// generic query
	int getCount( LengthControl lenCtrl, int len, 
			PatternControl patCtrl, String s, 
			RatingControl ratCtrl, int rat,
			UsedControl useCtrl,
			ResearchControl resCtrl,
			MethodControl methCtrl);	// generic query
	ArrayList<Word> getAllWords();				// return all words
	boolean connect();							// returns false if cannot connect
	void disconnect();
	void clear_AreYouSureYouWantToDoThis();		// delete table and start fresh
}
