package edu.itba.exchange.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.*;

class ApplicationPropertiesProviderTest {
    private static final String FILE_PATH = "application.properties";
    private static final String BAK_PATH = "application.properties.bak";
    
    private static void createFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(content);
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.renameTo(new File(BAK_PATH));
        }
        createFile("PROP1=VAL1\nPROP2=VAL2");
    }

    @AfterEach
    void tearDown() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
        File bak = new File(BAK_PATH);
        if (bak.exists()) {
            bak.renameTo(new File(FILE_PATH));
        }
    }

    @Test
    void shouldLoadPropertiesSuccessfully() {
        ApplicationPropertiesProvider provider = new ApplicationPropertiesProvider(new Properties());
        assertEquals("VAL1", provider.get("PROP1"));
        assertEquals("VAL2", provider.get("PROP2"));
    }

    @Test
    void shouldThrowExceptionWhenPropertyNotFound() {
        ApplicationPropertiesProvider provider = new ApplicationPropertiesProvider(new Properties());
        assertThrows(NullPointerException.class, () -> provider.get("NON_EXISTENT_PROPERTY"));
    }

    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
        assertThrows(RuntimeException.class, () -> new ApplicationPropertiesProvider(new Properties()));
    }
}