package mitchell.pete.xwd.dictionary.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class XDictDB_MySQLTest extends TestCase {
	private XDictDB_MySQL dict;
	private static final String dbName = "xdict";
	
	@Before
	public void setUp() {
		dict = new XDictDB_MySQL( dbName );
		dict.connect();
		dict.clear_AreYouSureYouWantToDoThis();
	}
	
	@After
	public void tearDown() {
		dict.disconnect();
	}
	
	@Test
	public void testCreate() {
		XDictDB_Tests.doTestCreate(dict);
	}
	
	@Test
	public void testPut() {
		XDictDB_Tests.doTestPut(dict);
	}
	
	@Test
	public void testDelete() {
		XDictDB_Tests.doTestDelete(dict);
	}
	
	@Test
	public void testGetLists() {
		XDictDB_Tests.doTestGetLists(dict);
	}
}
