package mitchell.pete.xwd.dictionary.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import mitchell.pete.xwd.dictionary.Word;
import mitchell.pete.xwd.dictionary.reconciler.Reconciler1;

public class XDictDB_MySQL implements XDictDB_Interface {
	private String dbURL = "";
	private String user = "xdict";
	private String password = "xdict";
	private String TABLE_WORDS = "WORDS";

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
	public int putWord( String s ) 
	{
		Word w = getWord(s);
		
		if ( w != null )	// word already exists, no reason to add a default version
			return w.getRating();
		
		w = new Word.Builder(s).build();
		putWord( w );
		return w.getRating();
	}

	/**
	 * Insert or replace a Word in the DB.  If replacing, use reconcile logic for values.
	 */
	@Override
	public int putWord( Word w ) 
	{
		String key = w.getEntry();
		Word oldWord = getWord(key);
		int val = 0;
		
		if (oldWord == null) {		// new word, nothing to reconcile
			val = insertWord(w);
		} else if (reconciler.Reconcile( oldWord, w ))	{	// something changed
			val = updateWord(oldWord);
		}
		
		return val;
	}
	
	private int insertWord(Word w) {
		// Add word to DB
		String query = "insert into " + TABLE_WORDS 
			+ " (ENTRY, LENGTH, RATING, USED_ANY, USED_NYT, NEEDS_RESEARCH, MANUALLY_RATED, LAST_MODIFIED) values('"
			+ w.getEntry() + "',"
			+ w.getEntry().length() + ","
			+ w.getRating() + ","
			+ w.isUsedAny() + ","
			+ w.isUsedNYT() + ","
			+ w.needsResearch() + ","
			+ w.isManuallyRated() + ",'"
			+ w.getLastModified().toString() + "')";
		
		try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return w.getRating();
	}
	
	private int updateWord(Word w) {
		// Add word to DB
		String query = "update " + TABLE_WORDS + " set "
			+ "LENGTH=" + w.getEntry().length() + ","
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return w.getRating();
	}

	@Override
	public Word getWord(String s) 
	{
		String key = Word.format(s);
		String query = "select * from " + TABLE_WORDS + " where ENTRY = '" + key + "'";
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public void deleteWord(String s) 
	{
		String key = Word.format(s);
		
		String query = "delete from " + TABLE_WORDS + " where ENTRY = '" + key + "'";
		try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}

	@Override
	public ArrayList<Word> getWords(LengthControl lenCtrl, int len, PatternControl patCtrl, String s, RatingControl ratCtrl, int rat, UsedControl useCtrl, ResearchControl resCtrl) 
	{
		ArrayList<Word> list = new ArrayList<Word>();
		boolean firstWhere = true;	// use to track when to put AND in query
		StringBuilder sb = new StringBuilder("");
		String key = Word.format(s);
		
		sb.append( "select * from " );
		sb.append( TABLE_WORDS );
		sb.append( " where " );
		if ( patCtrl == PatternControl.EQUALS )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("ENTRY = '");
			sb.append(key);
			sb.append("'" );
			firstWhere = false;
		}
		if ( patCtrl == PatternControl.STARTSWITH )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("ENTRY LIKE '");
			sb.append(key);
			sb.append("%'" );
			firstWhere = false;
		}
		if ( patCtrl == PatternControl.CONTAINS )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("ENTRY LIKE '%");
			sb.append(key);
			sb.append("%'" );
			firstWhere = false;
		}
		if ( lenCtrl == LengthControl.EQUALS )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("LENGTH = ");
			sb.append(len);
			firstWhere = false;
		}
		if ( lenCtrl == LengthControl.ATLEAST )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("LENGTH >= ");
			sb.append(len);
			firstWhere = false;
		}
		if ( lenCtrl == LengthControl.ATMOST )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("LENGTH <= ");
			sb.append(len);
			firstWhere = false;
		}
		if ( ratCtrl == RatingControl.ATLEAST )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("RATING >= ");
			sb.append(rat);
			firstWhere = false;
		}
		if ( ratCtrl == RatingControl.ATMOST )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("RATING <= ");
			sb.append(rat);
			firstWhere = false;
		}
		if ( useCtrl == UsedControl.USED_NYT )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("USED_NYT > 0");
			firstWhere = false;
		}
		else if ( useCtrl == UsedControl.USED_ANY ) // NYT => ANY, so don't need this if NYT set
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("USED_ANY > 0");
			firstWhere = false;
		}
		if ( resCtrl == ResearchControl.NEEDS_RESEARCH )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("NEEDS_RESEARCH > 0");
			firstWhere = false;
		}
		else if ( resCtrl == ResearchControl.NO_RESEARCH )
		{
			if (firstWhere == false )
				sb.append(" AND ");
			sb.append("NEEDS_RESEARCH == 0");
			firstWhere = false;
		}
		
		String query = sb.toString();
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}

	@Override
	public ArrayList<Word> getAllWords() 
	{
		ArrayList<Word> list = new ArrayList<Word>();
		String query = "select * from " + TABLE_WORDS;
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			e.printStackTrace();
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

		Word w = new Word.Builder(entry).rating(rating).
				usedNYT(used_nyt).usedAny(used_any).needsResearch(needs_research).
				manuallyRated(manually_rated).lastModified(last_modified).build();
		
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
