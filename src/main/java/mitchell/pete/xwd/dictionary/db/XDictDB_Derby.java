//package mitchell.pete.xwd.dictionary.db;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.sql.Timestamp;
//import java.util.ArrayList;
//
//import mitchell.pete.xwd.dictionary.Word;
//import mitchell.pete.xwd.dictionary.Word.TimestampType;
//import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.LengthControl;
//import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.PatternControl;
//import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.UsedControl;
//import mitchell.pete.xwd.dictionary.reconciler.Reconciler1;
//
//public class XDictDB_Derby implements XDictDB_Interface
//{
//	private String dbURL = "";
//	private String TABLE_WORDS = "WORDS";
//	private DBControl dbControl = DBControl.TESTING_ONLY;	// default to most restrictive mode
//
//	private Reconciler1 reconciler = new Reconciler1();
//
//	// jdbc connection
//	private Connection conn = null;
//	private Statement stmt = null;
//	
//	public XDictDB_Derby( String path, DBControl flag )
//	{
//		setURL( path );
//		dbControl = flag;
//		
//		if ( dbControl == DBControl.CREATE_IF_NEEDED || dbControl == DBControl.TESTING_ONLY )
//			dbURL += "create=true";
//	}
//
//	private void setURL( String path )
//	{
//			dbURL = "jdbc:derby:" + path + ";";
//	}
//	
//	@Override
//	public boolean connect() 
//	{
//		try {
//			createConnection();
//			stmt = conn.createStatement();
//
//			if ( dbControl == DBControl.TESTING_ONLY )		// if testing; delete existing DB
//				clear_AreYouSureYouWantToDoThis();
//
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//			return false;
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//			return false;
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//			return false;
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return false;
//		}
//		
//		return true;
//	}
//
//	@Override
//	public void disconnect() 
//	{
//		shutdown();
//	}
//
//	/**
//	 * Insert a word with default values.  If already exists, then no-op.
//	 */
//	@Override
//	public int putWord( String s, boolean isManual ) 
//	{
//		Word w = getWord(s);
//		
//		if ( w != null )	// word already exists, no reason to add a default version
//			return w.getRating();
//		
//		w = new Word.Builder(s).build();
//		putWord( w, isManual );
//		return 0;
//	}
//
//	/**
//	 * Insert or replace a Word in the DB.  If replacing, use reconcile logic for values.
//	 */
//	@Override
//	public int putWord( Word w ) 
//	{
//
//		String key = w.getEntry();
//		Word oldWord = getWord(key);
//		Word w1 = null;
//
//		if ( oldWord != null )		// word already exists
//		{
//			w1 = reconciler.Reconcile( oldWord, w );	// reconcile
//			deleteWord( oldWord.getEntry() );			// delete old entry
//		}
//		else						// it's new, just add it
//			w1 = w;
//		
//		// Set the timestamps on the new word
//		w1.setTS_Auto(TimestampType.NOW);
//		if ( isManual )
//			w1.setTS_Manual(TimestampType.NOW);
//		else
//			w1.setTS_Manual(TimestampType.NEVER);
//		
//		// Add word to DB
//		String query = "insert into " + TABLE_WORDS 
//			+ " (ENTRY, LENGTH, DISPLAY, RATING, SPARKLE, FACILITY, CURRENCY, TASTE, USED_ANY, USED_NYT, NEEDS_RESEARCH, TS_MANUAL, TS_AUTO, COMMENTS) values('"
//			+ w1.getEntry() + "',"
//			+ w1.getEntry().length() + ",'"
//			+ w1.getDisplay() + "',"
//			+ w1.getRating() + ","
//			+ w1.getSparkle() + ","
//			+ w1.getFacility() + ","
//			+ w1.getCurrency() + ","
//			+ w1.getTaste() + ","
//			+ ( w1.isUsed_Any() ? 1 : 0 ) + ","
//			+ ( w1.isUsed_NYT() ? 1 : 0 ) + ","
//			+ ( w1.needsResearch() ? 1 : 0 ) + ",'"
//			+ w1.getTS_Manual().toString() + "','"
//			+ w1.getTS_Auto().toString() + "','"
//			+ w1.getComments() + "')";
//		
//		try {
//			stmt.executeUpdate(query);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return w1.getRating();
//	}
//	
//	private int insertWord(Word w) {
//		
//	}
//	
//	private int updateWord(Word w) {
//		
//	}
//
//	@Override
//	public ArrayList<Word> getAllWords() 
//	{
//		ArrayList<Word> list = new ArrayList<Word>();
//		String query = "select * from " + TABLE_WORDS;
//		
//		try {
//			ResultSet rs = stmt.executeQuery(query);
//			{
//				while( rs.next() )
//				{
//					Word w = getWordFromResultSet(rs);
//					
//					list.add(w);
//				}
//				rs.close();
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return list;
//	}
//
//	@Override
//	public Word getWord(String s) 
//	{
//		String key = Word.format(s);
//		String query = "select * from " + TABLE_WORDS + " where ENTRY = '" + key + "'";
//		
//		try {
//			stmt = conn.createStatement();
//			ResultSet rs = stmt.executeQuery(query);
//			{
//				if ( rs.next() )
//				{
//					Word w = getWordFromResultSet(rs);
//					
//					return w;
//				}
//				rs.close();
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return null;
//	}
//
//	@Override
//	public ArrayList<Word> getWords(LengthControl lc, int l, PatternControl pc, String s, UsedControl uc, ResearchControl rc) 
//	{
//		ArrayList<Word> list = new ArrayList<Word>();
//		boolean firstWhere = true;	// use to track when to put AND in query
//		StringBuilder sb = new StringBuilder("");
//		String key = Word.format(s);
//		
//		sb.append( "select * from " );
//		sb.append( TABLE_WORDS );
//		sb.append( " where " );
//		if ( pc == PatternControl.EQUALS )
//		{
//			if (firstWhere == false )
//				sb.append(" AND ");
//			sb.append("ENTRY = '");
//			sb.append(key);
//			sb.append("'" );
//			firstWhere = false;
//		}
//		if ( pc == PatternControl.STARTSWITH )
//		{
//			if (firstWhere == false )
//				sb.append(" AND ");
//			sb.append("ENTRY LIKE '");
//			sb.append(key);
//			sb.append("%'" );
//			firstWhere = false;
//		}
//		if ( pc == PatternControl.CONTAINS )
//		{
//			if (firstWhere == false )
//				sb.append(" AND ");
//			sb.append("ENTRY LIKE '%");
//			sb.append(key);
//			sb.append("%'" );
//			firstWhere = false;
//		}
//		if ( lc == LengthControl.EQUALS )
//		{
//			if (firstWhere == false )
//				sb.append(" AND ");
//			sb.append("LENGTH = ");
//			sb.append(l);
//			firstWhere = false;
//		}
//		if ( lc == LengthControl.ATLEAST )
//		{
//			if (firstWhere == false )
//				sb.append(" AND ");
//			sb.append("LENGTH >= ");
//			sb.append(l);
//			firstWhere = false;
//		}
//		if ( lc == LengthControl.ATMOST )
//		{
//			if (firstWhere == false )
//				sb.append(" AND ");
//			sb.append("LENGTH <= ");
//			sb.append(l);
//			firstWhere = false;
//		}
//		if ( uc == UsedControl.USED_NYT )
//		{
//			if (firstWhere == false )
//				sb.append(" AND ");
//			sb.append("USED_NYT > 0");
//			firstWhere = false;
//		}
//		else if ( uc == UsedControl.USED_ANY ) // NYT => ANY, so don't need this if NYT set
//		{
//			if (firstWhere == false )
//				sb.append(" AND ");
//			sb.append("USED_ANY > 0");
//			firstWhere = false;
//		}
//		if ( rc == ResearchControl.NEEDS_RESEARCH )
//		{
//			if (firstWhere == false )
//				sb.append(" AND ");
//			sb.append("NEEDS_RESEARCH > 0");
//			firstWhere = false;
//		}
//		else if ( rc == ResearchControl.NO_RESEARCH )
//		{
//			if (firstWhere == false )
//				sb.append(" AND ");
//			sb.append("NEEDS_RESEARCH == 0");
//			firstWhere = false;
//		}
//		
//		String query = sb.toString();
//		
//		try {
//			ResultSet rs = stmt.executeQuery(query);
//			{
//				while( rs.next() )
//				{
//					Word w = getWordFromResultSet(rs);
//					list.add(w);
//				}
//				rs.close();
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return list;
//	}
//
//	private Word getWordFromResultSet( ResultSet rs ) throws SQLException
//	{
//		String display = rs.getString("DISPLAY");
//		short rating = rs.getShort("RATING");
//		short sparkle = rs.getShort("SPARKLE");
//		short facility = rs.getShort("FACILITY");
//		short currency = rs.getShort("CURRENCY");
//		short taste = rs.getShort("TASTE");
//		boolean used_any = rs.getShort("USED_ANY") > 0;
//		boolean used_nyt = rs.getShort("USED_NYT") > 0;
//		boolean needs_research = rs.getShort("NEEDS_RESEARCH") > 0;
//		String comments = rs.getString("COMMENTS");
//		Timestamp ts_manual = rs.getTimestamp("TS_MANUAL");
//		Timestamp ts_auto = rs.getTimestamp("TS_AUTO");
//		
//		Word w = new Word.Builder(display).rating(rating).
//				sparkle(sparkle).facility(facility).
//				currency(currency).taste(taste).usedAny(used_any).
//				usedNYT(used_nyt).needsResearch(needs_research).
//				comments(comments).ts_auto(ts_auto).ts_manual(ts_manual).build();
//		
//		return w;
//	}
//	
//	@Override
//	public void deleteWord(String s) 
//	{
//		String key = Word.format(s);
//		
//		String query = "delete from " + TABLE_WORDS + " where ENTRY = '" + key + "'";
//		try {
//			stmt.executeUpdate(query);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	@Override
//	public int size() 
//	{
//		String query = "select count(*) from " + TABLE_WORDS;
//
//		try {
//			ResultSet rs = stmt.executeQuery(query);
//			if ( rs.next() )
//			{
//				int size = rs.getInt(1);
//				rs.close();
//				return size;
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return 0;
//	}
//	
//	private boolean createWordsTable()
//	{
//		String query = "create table WORDS " 
//			+ "(ENTRY VARCHAR(25) PRIMARY KEY NOT NULL,"
//			+ " LENGTH SMALLINT NOT NULL,"
//			+ " DISPLAY VARCHAR(30) NOT NULL,"
//			+ " RATING SMALLINT NOT NULL,"
//			+ " SPARKLE SMALLINT,"
//			+ " FACILITY SMALLINT,"
//			+ " CURRENCY SMALLINT,"
//			+ " TASTE SMALLINT,"
//			+ " USED_ANY SMALLINT NOT NULL,"
//			+ " USED_NYT SMALLINT NOT NULL,"
//			+ " NEEDS_RESEARCH SMALLINT NOT NULL,"
//			+ " TS_MANUAL TIMESTAMP NOT NULL,"
//			+ " TS_AUTO TIMESTAMP NOT NULL,"
//			+ " COMMENTS VARCHAR(100) )";
//
//		try {
//			stmt.executeUpdate(query);
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return false;
//		}
//		
//		return true;
//	}
//
//	private void dropWordsTable()
//	{
//		String query = "drop table WORDS";
//		try {
//			stmt.executeUpdate(query);
//		} catch (SQLException e) {
//		}
//	}
//
//	@Override
//	public void clear_AreYouSureYouWantToDoThis() 
//	{
//		dropWordsTable();
//		createWordsTable();
//	}
//
//	private void createConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
//	{
//		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
//		
//		// get a connection
//		conn = DriverManager.getConnection(dbURL);
//	}
//    
//    private void shutdown()
//    {
//        try {
//            if (stmt != null)
//                stmt.close();
//
//            if (conn != null)
//                conn.close();
//        } catch (SQLException sqlExcept) {
//        	System.out.println("Error on DB Shutdown: " + sqlExcept.toString());
//        }
//    }
//}
