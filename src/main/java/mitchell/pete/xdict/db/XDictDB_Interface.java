package mitchell.pete.xdict.db;

import mitchell.pete.xdict.Word;

import java.util.ArrayList;

public interface XDictDB_Interface 
{
	enum LengthControl { ALL, EQUALS, ATLEAST, ATMOST };		// used to build queries
	enum PatternControl { ALL, EQUALS, STARTSWITH, CONTAINS };	// used to build queries
	enum RatingControl { ALL, ATLEAST, ATMOST, EQUALS };		// used to build queries
	enum UsedControl { ALL, ANY, NYT, OTHER, NOT_NYT, NOT_OTHER, NOT_USED, NONE };		// used to build queries
	enum ResearchControl { ALL, NEEDS_RESEARCH, NO_RESEARCH };	// use to build queries
	enum MethodControl { ALL, MANUAL, RANKED, AUTOMATIC };				// used to build queries
	enum WORD_STATUS { NEW, EXISTS, DUPLICATE, COMMENT, ERROR };	// used to distinguish adds from changes and no-ops


	WORD_STATUS putWord( Word w ) throws XDictSQLException;				// add or update word; return rating
	WORD_STATUS putWord( String s ) throws XDictSQLException;			// add word with default values, if not there; else no-op
	Word getWord( String s ) throws XDictSQLException;					// find word and return it
	void deleteWord( String s) throws XDictSQLException;				// delete word
	int size()  throws XDictSQLException;								// return number of entries in DB
	ArrayList<Word> getWords( LengthControl lenCtrl, int len, 
							PatternControl patCtrl, String s, 
							RatingControl ratCtrl, int rat,
							UsedControl useCtrl,
							ResearchControl resCtrl,
							MethodControl methCtrl,
							int start,
							int limit,
							boolean ratingQuery)  throws XDictSQLException;	// generic query
	int getCount( LengthControl lenCtrl, int len, 
			PatternControl patCtrl, String s, 
			RatingControl ratCtrl, int rat,
			UsedControl useCtrl,
			ResearchControl resCtrl,
			MethodControl methCtrl,
			boolean ratingQuery) throws XDictSQLException;	// generic query
    int getCount( LengthControl lenCtrl, int len,
                  PatternControl patCtrl, String s,
                  int minRat, int maxRat,
                  UsedControl useCtrl,
                  ResearchControl resCtrl,
                  MethodControl methCtrl) throws XDictSQLException;	// for rating breakdown report
    ArrayList<Word> getAllWords() throws XDictSQLException;				// return all words
	boolean connect();							// returns false if cannot connect
	void disconnect();
    ArrayList<String> showAllTables() throws XDictSQLException;          // get names of all tables (test, etc.) in DB
    int getTableSize(String tableName) throws XDictSQLException;         // get size (number of entries) in table
	void clear_YesIReallyMeanToDoThis() throws XDictSQLException;		// delete table and start fresh
    void createTablesIfNotExists(boolean isTemporary) throws XDictSQLException;  // create tables, if necessary
}
