package com.adaptris.filesystem;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DirectoryEntityTest {

    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = File.createTempFile(DirectoryEntityTest.class.getSimpleName(), "", null);
        tempDir.delete();
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
    }

    @After
    public void tearDown() {
        for (final File f : tempDir.listFiles()) {
            f.delete();
        }
        tempDir.delete();
    }

    @Test
    public void testDefaults() {
        DirectoryEntity directoryEntity = new DirectoryEntity(tempDir);
        Date d = new java.util.Date();

        assertEquals(directoryEntity.getId(), directoryEntity.getDescription());
        assertTrue(directoryEntity.getAbsolutePath().contains(directoryEntity.getId()));
        assertEquals(d.toString(), directoryEntity.getCreatedAt().toString());
    }

    @Test
    public void testUniqueComparable() throws InterruptedException {
        DirectoryEntity directoryEntity = new DirectoryEntity(tempDir);
        DirectoryEntity directoryEntityII = new DirectoryEntity(tempDir);
        //Pausing to create new timestamp(better method welcome)
        GregorianCalendar gCal = new GregorianCalendar(2018, 11, 23, 12, 30);
        Date newDate = gCal.getTime();
        directoryEntity.setCreatedAt(newDate);
        //Confirming that directoryEntity is set to defaults
        assertEquals("Should have same \'getDescription\'", directoryEntity.getDescription(), directoryEntityII.getDescription());
        assertEquals("Should have identical \'updatedAt\' dates", directoryEntity.getUpdatedAt(), directoryEntityII.getUpdatedAt());
        assertNotSame("Should have disparate \'getId\' prior to update", directoryEntity.getId(), directoryEntityII.getId());
        assertNotSame("Should have disparate \'getCreatedAt\'", directoryEntity.getCreatedAt(), directoryEntityII.getCreatedAt());
        assertNotSame("Should be two unique instances of \'DirectoryEntity\'", directoryEntity, directoryEntityII);
        //Setting new attributes
            directoryEntity.setId("14");
            directoryEntityII.setId("14");
            directoryEntity.setAbsolutePath("Foo");
            directoryEntity.setParentDirectory("Hello World");
            directoryEntity.setDescription("Bar");
            directoryEntity.setUpdatedAt(newDate);
            directoryEntity.setSize(5000L);
        //Confirming that directoryEntity is updated from defaults
        assertEquals("Should return \'Hello World\' as \'getParentDirectory\'","Hello World", directoryEntity.getParentDirectory());
        assertEquals("Should return matching \'getId\'", directoryEntity.getId(), directoryEntityII.getId());
        assertNotEquals("\'UpdatedAt\' should no longer match", directoryEntity.getUpdatedAt(), directoryEntityII.getUpdatedAt());
        assertNotEquals("Should have disparate \'getDescription\' after update" , directoryEntity.getDescription(), directoryEntityII.getDescription());
        assertNotEquals("Should have disparate \'getSize\'", directoryEntity.getSize(), directoryEntityII.getSize());
        assertTrue("Should match \'newDate\' as the \'getCreatedAt\'", directoryEntity.toString().contains("\"createdAt\":\"2018-12-23T12:30:00+0000\""));
        assertTrue("Should still be a string type", directoryEntity.toJSON().contains("\"createdAt\":\"2018-12-23T12:30:00+0000\""));
        assertThat("\'toString\' should return String type", directoryEntity.toJSON(), instanceOf(String.class));
    }
}
