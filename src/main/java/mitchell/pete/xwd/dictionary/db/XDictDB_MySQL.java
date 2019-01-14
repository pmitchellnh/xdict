package mitchell.pete.xwd.dictionary.db;

import mitchell.pete.xwd.dictionary.Word;
import mitchell.pete.xwd.dictionary.reconciler.Reconciler1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

public class XDictDB_MySQL implements XDictDB_Interface {
	private String dbURL = "";
	private String user = "xdict";
	private String password = "xdict";
	private String TABLE_WORDS = "WORDS";
	private String TABLE_COMMENTS = "COMMENTS";

	private Reconciler1 reconciler = new Reconciler1();

	// jdbc connection
	private Connection conn = null;
	private Statement stmt = null;
	
	public XDictDB_MySQL(String dbName) {
		setURL( dbName );
	}
	
	private void setURL( String dbName ) {
			dbURL = "jdbc:mysql://localhost:3306/" + dbName;
	}
	
	/**
	 * Insert a word with default values.  If already exists, then no-op.
	 */
	@Override
	public WORD_STATUS putWord( String s ) 
	{
		Word w = getWord(s);
		
		if ( w != null )	// word already exists, no reason to add a default version
			return WORD_STATUS.DUPLICATE;
		
		w = new Word.Builder(s).build();
		putWord( w );
		return WORD_STATUS.NEW;
	}

	/**
	 * Insert or replace a Word in the DB.  If replacing, use reconcile logic for values.
	 */
	@Override
	public WORD_STATUS putWord( Word w ) 
	{
		String key = w.getEntry();
		Word oldWord = getWord(key);
		WORD_STATUS status = WORD_STATUS.NEW;
		
		if (oldWord == null) {		// new word, nothing to reconcile
			insertWord(w);
			status = WORD_STATUS.NEW;
		} else if (reconciler.Reconcile( oldWord, w ))	{	// something changed
			updateWord(oldWord);
			status = WORD_STATUS.EXISTS;
		} else {
			status = WORD_STATUS.DUPLICATE;
		}
		
		if (oldWord == null || (!oldWord.hasComment() && w.hasComment())) {			// comment added
			insertComment(w);
		} else if (reconciler.ReconcileComment(oldWord, w)) {	// something changed
			if (!oldWord.hasComment()) {						// comment removed
				deleteComment(w);
			} else {											// comment updated
				updateComment(w);
			}
		}
		return status;
	}
	
	private int insertWord(Word w) {
		// Add word to DB
		String query = "insert into " + TABLE_WORDS 
			+ " (ENTRY, LENGTH, RATING, USED_ANY, USED_NYT, NEEDS_RESEARCH, MANUALLY_RATED, LAST_MODIFIED) values('"
			+ w.getEntry() + "',"
			+ w.length() + ","
			+ w.getRating() + ","
			+ w.isUsedAny() + ","
			+ w.isUsedNYT() + ","
			+ w.needsResearch() + ","
			+ w.isManuallyRated() + ",'"
			+ w.getLastModified().toString() + "')";
		
		try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
		
		return w.getRating();
	}
	
	private int updateWord(Word w) {
		// Add word to DB
		String query = "update " + TABLE_WORDS + " set "
			+ "LENGTH=" + w.length() + ","
			+ "RATING=" + w.getRating() + ","
			+ "USED_ANY=" + w.isUsedAny() + ","
			+ "USED_NYT=" + w.isUsedNYT() + ","
			+ "NEEDS_RESEARCH=" + w.needsResearch() + ","
			+ "MANUALLY_RATED=" + w.isManuallyRated() + ","
			+ "LAST_MODIFIED='" + w.getLastModified().toString() + "' "
			+ "where ENTRY='" + w.getEntry() + "'";
		
		try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
		
		return w.getRating();
	}
	
	private void insertComment(Word w) {
		String query = "insert into " + TABLE_COMMENTS 
			+ " (ENTRY, COMMENT) values('"
			+ w.getEntry() + "','"
			+ w.getComment() + "')";
		
		try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
	}
	
	private void updateComment(Word w) {
		String query = "update " + TABLE_COMMENTS + " set "
				+ "COMMENT='" + w.getComment() + "' "
				+ "where ENTRY='" + w.getEntry() + "'";
			
			try {
				stmt.executeUpdate(query);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				System.out.println("SQL: " + query);
			}
	}
	
	private void deleteComment(Word w) {
		String query = "delete from " + TABLE_COMMENTS + " where ENTRY = '" + w.getEntry() + "'";
		try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
	}

	@Override
	public Word getWord(String s) 
	{
		String key = Word.format(s, Word.NO_WILDS);
		String query = "select * from " + TABLE_WORDS + 
				" LEFT JOIN COMMENTS ON WORDS.ENTRY=COMMENTS.ENTRY" +
				" where WORDS.ENTRY = '" + key + "'";
		
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			{
				if ( rs.next() )
				{
					Word w = getWordFromResultSet(rs);
					
					return w;
				}
				rs.close();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
		
		return null;
	}

	@Override
	public void deleteWord(String s) 
	{
		String key = Word.format(s, Word.NO_WILDS);
		
		String query = "delete from " + TABLE_WORDS + " where ENTRY = '" + key + "'";
		String query1 = "delete from " + TABLE_COMMENTS + " where ENTRY = '" + key + "'";
		try {
			stmt.executeUpdate(query);
			stmt.executeUpdate(query1);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
		
	}

	@Override
	public int size() 
	{
		String query = "select count(*) from " + TABLE_WORDS;

		try {
			ResultSet rs = stmt.executeQuery(query);
			if ( rs.next() )
			{
				int size = rs.getInt(1);
				rs.close();
				return size;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
		
		return 0;
	}

	@Override
	public ArrayList<Word> getWords(LengthControl lenCtrl, int len, PatternControl patCtrl, String s, RatingControl ratCtrl, int rat, UsedControl useCtrl, ResearchControl resCtrl, MethodControl methCtrl, int start, int limit, boolean ratingQuery) 
	{
		ArrayList<Word> list = new ArrayList<Word>();
		boolean firstWhere = true;	// use to track when to put AND in query
		StringBuilder sb = new StringBuilder("");
		String key = Word.format(s, Word.WILD_OK);
		
		sb.append( "select * from " );
		sb.append( TABLE_WORDS );
		sb.append(" LEFT JOIN COMMENTS ON ");
		sb.append( TABLE_WORDS );
		sb.append(".ENTRY=");
		sb.append(TABLE_COMMENTS);
		sb.append(".ENTRY");

		
		sb.append( " where " );
		
		if ( patCtrl == PatternControl.EQUALS ) {
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append(TABLE_WORDS);
			sb.append(".ENTRY LIKE '");
			sb.append(key);
			sb.append("'" );
			firstWhere = false;
		} else if ( patCtrl == PatternControl.STARTSWITH ) {
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append(TABLE_WORDS);
			sb.append(".ENTRY LIKE '");
			sb.append(key);
			sb.append("%'" );
			firstWhere = false;
		} else if ( patCtrl == PatternControl.CONTAINS ) {
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append(TABLE_WORDS);
			sb.append(".ENTRY LIKE '%");
			sb.append(key);
			sb.append("%'" );
			firstWhere = false;
		}

        // If looking for an exact pattern, ignore other criteria
        if (patCtrl != PatternControl.EQUALS || key.contains("_")) {
            if (lenCtrl == LengthControl.ALL) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("LENGTH >= 1");
                firstWhere = false;
            } else if (lenCtrl == LengthControl.EQUALS) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("LENGTH = ");
                sb.append(len);
                firstWhere = false;
            } else if (lenCtrl == LengthControl.ATLEAST) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("LENGTH >= ");
                sb.append(len);
                firstWhere = false;
            } else if (lenCtrl == LengthControl.ATMOST) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("LENGTH <= ");
                sb.append(len);
                firstWhere = false;
            }

            if (ratCtrl == RatingControl.ATLEAST) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("RATING >= ");
                sb.append(rat);
                firstWhere = false;
            } else if (ratCtrl == RatingControl.ATMOST) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("RATING <= ");
                sb.append(rat);
                firstWhere = false;
            }

            if (useCtrl == UsedControl.USED_NYT) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("USED_NYT > 0");
                firstWhere = false;
            } else if (useCtrl == UsedControl.USED_ANY) { // NYT => ANY, so don't need this if NYT set
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("USED_ANY > 0");
                firstWhere = false;
            } else if (useCtrl == UsedControl.NOT_USED) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("USED_ANY = 0");
                firstWhere = false;
            }

            if (resCtrl == ResearchControl.NEEDS_RESEARCH) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("NEEDS_RESEARCH > 0");
                firstWhere = false;
            } else if (resCtrl == ResearchControl.NO_RESEARCH) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("NEEDS_RESEARCH = 0");
                firstWhere = false;
            }

            if (methCtrl == MethodControl.AUTOMATIC) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("MANUALLY_RATED = 0");
                firstWhere = false;
            } else if (methCtrl == MethodControl.MANUAL) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("MANUALLY_RATED > 0");
                firstWhere = false;
            }
        }
		sb.append(" ORDER BY ");
		sb.append(TABLE_WORDS);
		sb.append(".ENTRY");
		sb.append(" LIMIT " + start + "," + limit);
		
		String query = sb.toString();
//		System.out.println("Query: " + query);
		
		try {
			ResultSet rs = stmt.executeQuery(query);
			{
				while( rs.next() )
				{
					Word w = getWordFromResultSet(rs);
					list.add(w);
				}
				rs.close();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
		
		return list;
	}

	@Override
	public int getCount(LengthControl lenCtrl, int len, PatternControl patCtrl, String s, RatingControl ratCtrl, int rat, UsedControl useCtrl, ResearchControl resCtrl, MethodControl methCtrl, boolean ratingQuery) 
	{
		boolean firstWhere = true;	// use to track when to put AND in query
		StringBuilder sb = new StringBuilder("");
		String key = Word.format(s, Word.WILD_OK);
		
		sb.append( "select count(*) from " );
		sb.append( TABLE_WORDS );
		sb.append( " where " );
		if ( patCtrl == PatternControl.EQUALS ) {
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("ENTRY LIKE '");
			sb.append(key);
			sb.append("'" );
			firstWhere = false;
		} else if ( patCtrl == PatternControl.STARTSWITH ) {
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("ENTRY LIKE '");
			sb.append(key);
			sb.append("%'" );
			firstWhere = false;
		} else if ( patCtrl == PatternControl.CONTAINS ) {
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("ENTRY LIKE '%");
			sb.append(key);
			sb.append("%'" );
			firstWhere = false;
		}

        // If looking for an exact pattern, ignore other criteria
        if (patCtrl != PatternControl.EQUALS || key.contains("_")) {
            if (lenCtrl == LengthControl.ALL) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("LENGTH >= 1");
                firstWhere = false;
            } else if (lenCtrl == LengthControl.EQUALS) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("LENGTH = ");
                sb.append(len);
                firstWhere = false;
            } else if (lenCtrl == LengthControl.ATLEAST) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("LENGTH >= ");
                sb.append(len);
                firstWhere = false;
            } else if (lenCtrl == LengthControl.ATMOST) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("LENGTH <= ");
                sb.append(len);
                firstWhere = false;
            }


            if (ratCtrl == RatingControl.ATLEAST) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("RATING >= ");
                sb.append(rat);
                firstWhere = false;
            } else if (ratCtrl == RatingControl.ATMOST) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("RATING <= ");
                sb.append(rat);
                firstWhere = false;
            }

            if (useCtrl == UsedControl.USED_NYT) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("USED_NYT > 0");
                firstWhere = false;
            } else if (useCtrl == UsedControl.USED_ANY) { // NYT => ANY, so don't need this if NYT set
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("USED_ANY > 0");
                firstWhere = false;
            } else if (useCtrl == UsedControl.NOT_USED) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("USED_ANY = 0");
                firstWhere = false;
            }

            if (resCtrl == ResearchControl.NEEDS_RESEARCH) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("NEEDS_RESEARCH > 0");
                firstWhere = false;
            } else if (resCtrl == ResearchControl.NO_RESEARCH) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("NEEDS_RESEARCH = 0");
                firstWhere = false;
            }

            if (methCtrl == MethodControl.AUTOMATIC) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("MANUALLY_RATED = 0");
            } else if (methCtrl == MethodControl.MANUAL) {
                if (firstWhere == false)
                    sb.append(" AND ");
                sb.append("MANUALLY_RATED > 0");
            }
        }
		
		String query = sb.toString();
		
		try {
			ResultSet rs = stmt.executeQuery(query);
			{
				if ( rs.next() )
				{
					int size = rs.getInt(1);
					rs.close();
					return size;
				}
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
		
		return 0;
	}

	@Override
	public ArrayList<Word> getAllWords() 
	{
		ArrayList<Word> list = new ArrayList<Word>();
		String query = "select * from " + TABLE_WORDS + " LEFT JOIN COMMENTS ON WORDS.ENTRY=COMMENTS.ENTRY";
		
		try {
			ResultSet rs = stmt.executeQuery(query);
			{
				while( rs.next() )
				{
					Word w = getWordFromResultSet(rs);
					
					list.add(w);
				}
				rs.close();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
		
		return list;
	}

	@Override
	public boolean connect() {
		try {
			createConnection();
			stmt = conn.createStatement();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public void disconnect() 
	{
		shutdown();
	}

	@Override
	public void clear_AreYouSureYouWantToDoThis() {
		String query = "delete from WORDS";
		try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("SQL: " + query);
		}
	}
	
	private Word getWordFromResultSet( ResultSet rs ) throws SQLException
	{
		String entry = rs.getString("ENTRY");
		byte rating = rs.getByte("RATING");
		boolean used_any = rs.getBoolean("USED_ANY");
		boolean used_nyt = rs.getBoolean("USED_NYT");
		boolean needs_research = rs.getBoolean("NEEDS_RESEARCH");
		boolean manually_rated = rs.getBoolean("MANUALLY_RATED");
		Timestamp last_modified = rs.getTimestamp("LAST_MODIFIED");
		String comment = rs.getString("COMMENT");

		Word w = new Word.Builder(entry).rating(rating).
				usedNYT(used_nyt).usedAny(used_any).needsResearch(needs_research).
				manuallyRated(manually_rated).lastModified(last_modified).comment(comment).build();
		
		return w;
	}
	
	

	private void createConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		
		// get a connection
		conn = DriverManager.getConnection(dbURL, user, password);
	}
    
    private void shutdown()
    {
        try {
            if (stmt != null)
                stmt.close();

            if (conn != null)
                conn.close();
        } catch (SQLException sqlExcept) {
        	System.out.println("Error on DB Shutdown: " + sqlExcept.toString());
        }
    }
}
