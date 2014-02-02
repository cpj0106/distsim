package edu.unlv.cs.edas.graph;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.unlv.cs.edas.graph.Key;

public class KeyTest {

	private Key expected;
	private Key expectedCopy;
	private Key expectedOther;
	
	@Before
	public void setUp() throws Exception {
		expected = new Key("one");
		expectedCopy = new Key("one");
		expectedOther = new Key("two");
	}
	
	@Test
	public void testConstruct() throws Exception {
		try {
			new Key(null);
			fail();
		} catch (IllegalArgumentException e) {
			// this is good
		}
	}
	
	@Test
	public void testGetters() throws Exception {
		assertEquals("one", expected.getName());
	}
	
	@Test
	public void testEquals() throws Exception {
		assertFalse(expected.equals(null));
		assertTrue(expected.equals(expected));
		assertFalse(expected.equals(this));
		assertTrue(expected.equals(expectedCopy));
		assertFalse(expected.equals(expectedOther));
	}
	
	@Test
	public void testHashCode() throws Exception {
		assertEquals(expected.hashCode(), expectedCopy.hashCode());
		assertNotSame(expected.hashCode(), expectedOther.hashCode());
	}
	
	@Test
	public void testToString() throws Exception {
		assertEquals("Key[name=one]", expected.toString());
	}
	
}