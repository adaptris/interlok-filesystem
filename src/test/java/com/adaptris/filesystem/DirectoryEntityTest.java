package com.adaptris.filesystem;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DirectoryEntityTest {

    private File tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = File.createTempFile(DirectoryEntityTest.class.getSimpleName(), "", null);
        tempDir.delete();
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
    }

    @AfterEach
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
        assertEquals(directoryEntity.getDescription(), directoryEntityII.getDescription(), "Should have same \'getDescription\'");
        assertEquals(directoryEntity.getUpdatedAt(), directoryEntityII.getUpdatedAt(), "Should have identical \'updatedAt\' dates");
        assertNotSame(directoryEntity.getId(), directoryEntityII.getId(), "Should have disparate \'getId\' prior to update");
        assertNotSame(directoryEntity.getCreatedAt(), directoryEntityII.getCreatedAt(), "Should have disparate \'getCreatedAt\'");
        assertNotSame(directoryEntity, directoryEntityII, "Should be two unique instances of \'DirectoryEntity\'");
        //Setting new attributes
            directoryEntity.setId("14");
            directoryEntityII.setId("14");
            directoryEntity.setAbsolutePath("Foo");
            directoryEntity.setParentDirectory("Hello World");
            directoryEntity.setDescription("Bar");
            directoryEntity.setUpdatedAt(newDate);
            directoryEntity.setSize(5000L);
        //Confirming that directoryEntity is updated from defaults
        assertEquals("Hello World", directoryEntity.getParentDirectory(), "Should return \'Hello World\' as \'getParentDirectory\'");
        assertEquals(directoryEntity.getId(), directoryEntityII.getId(), "Should return matching \'getId\'");
        assertNotEquals(directoryEntity.getUpdatedAt(), directoryEntityII.getUpdatedAt(), "\'UpdatedAt\' should no longer match");
        assertNotEquals(directoryEntity.getDescription(), directoryEntityII.getDescription(), "Should have disparate \'getDescription\' after update");
        assertNotEquals(directoryEntity.getSize(), directoryEntityII.getSize(), "Should have disparate \'getSize\'");
        assertTrue(directoryEntity.toString().contains("\"createdAt\":\"2018-12-23T12:30:00+0000\""), "Should match \'newDate\' as the \'getCreatedAt\'");
        assertTrue(directoryEntity.toJSON().contains("\"createdAt\":\"2018-12-23T12:30:00+0000\""), "Should still be a string type");
        assertEquals(directoryEntity.toJSON().getClass(), String.class, "\'toString\' should return String type");
    }
}
