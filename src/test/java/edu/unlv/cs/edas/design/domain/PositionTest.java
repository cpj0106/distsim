package edu.unlv.cs.edas.design.domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.unlv.cs.edas.design.domain.Position;

public class PositionTest {

	private Position expected;
	
	private Position expectedSame;
	
	private Position expectedOther1;
	private Position expectedOther2;
	private Position expectedOther3;
	
	@Before
	public void setUp() {
		expected = new Position(1.0, 2.0);
		
		expectedSame = new Position(1.0, 2.0);
		
		expectedOther1 = new Position(1.0, 1.0);
		expectedOther2 = new Position(2.0, 2.0);
		expectedOther3 = new Position(3.0, 3.0);
	}
	
	@Test
	public void testGetX() {
		assertEquals(1, expected.getX().intValue());
	}
	
	@Test
	public void testGetY() {
		assertEquals(2, expected.getY().intValue());
	}
	
	@Test
	public void testToString() {
		assertEquals("Position[x=1,y=2]", expected.toString());
	}
	
	@Test
	public void testEquals() {
		assertFalse(expected.equals(null));
		assertTrue(expected.equals(expected));
		assertFalse(expected.equals(this));
		assertTrue(expected.equals(expectedSame));
		assertFalse(expected.equals(expectedOther1));
		assertFalse(expected.equals(expectedOther2));
		assertFalse(expected.equals(expectedOther3));
	}
	
	@Test
	public void testHashCode() {
		assertEquals(expected.hashCode(), expectedSame.hashCode());
		assertNotSame(expected.hashCode(), expectedOther1.hashCode());
		assertNotSame(expected.hashCode(), expectedOther2.hashCode());
		assertNotSame(expected.hashCode(), expectedOther3.hashCode());
	}
	
}
