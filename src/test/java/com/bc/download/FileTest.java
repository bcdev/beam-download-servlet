package com.bc.download;


import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class FileTest {

	@Test
	public void testFilenameSanitizing() throws Exception {
		assertEquals("filename.txt", new File("../../filename.txt").getName());
		assertEquals(new File("/tmp/filename.txt").getAbsolutePath(), 
                     new File(new File("/tmp"), new File("../etc/filename.txt").getName()).getAbsolutePath());
	}
	
	@Test
	public void testSubstring() throws Exception {
		assertEquals("Honk", "Honk".substring(0,Math.min(100, "Honk".length())));
	}
}
