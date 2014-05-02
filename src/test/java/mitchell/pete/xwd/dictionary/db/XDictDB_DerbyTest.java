package mitchell.pete.xwd.dictionary.db;

import mitchell.pete.xwd.dictionary.db.XDictDB_Interface.DBControl;
import junit.framework.TestCase;


public class XDictDB_DerbyTest extends TestCase 
{
	private XDictDB_Derby dict;
	private static final String path = "C:/Users/Pete/DB/testDict";
	
	public void setUp() {
		dict = new XDictDB_Derby( path, DBControl.TESTING_ONLY );	// TESTING_ONLY => Deletes existing DB
		dict.connect();
	}
	
	public void tearDown() {
		dict.disconnect();
	}
	
	public void testCreate() {
		XDictDB_Tests.doTestCreate(dict);
	}
	
	public void testPut() {
		XDictDB_Tests.doTestPut(dict);
	}
	
	public void testDelete() {
		XDictDB_Tests.doTestDelete(dict);
	}
	
	public void testGetLists() {
		XDictDB_Tests.doTestGetLists(dict);
	}
}
