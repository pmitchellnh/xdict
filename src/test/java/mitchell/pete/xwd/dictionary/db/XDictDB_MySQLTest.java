package mitchell.pete.xwd.dictionary.db;

import mitchell.pete.xwd.dictionary.XDictConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class XDictDB_MySQLTest extends TestCase {
	private XDictDB_MySQL dict;
	private static final String dbName = "xdict";
    private static final String jtestSuffix = "_JUNIT";
	
	@Before
	public void setUp() {
        XDictConfig.setDbModeSuffix(jtestSuffix);
		dict = new XDictDB_MySQL( dbName );
		dict.connect();
        try {
            dict.createTablesIfNotExists(true); // create temp tables
            dict.clear_YesIReallyMeanToDoThis();
        } catch (XDictSQLException e) {
            fail(e.toString());
        }
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

    @Test
    public void testCommentLengths() { XDictDB_Tests.doTestCommentLengths(dict);}
}
