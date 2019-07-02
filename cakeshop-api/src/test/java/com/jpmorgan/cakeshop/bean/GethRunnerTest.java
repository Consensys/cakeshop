package com.jpmorgan.cakeshop.bean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class GethRunnerTest {

    public static final String ENODE_ADDRESS = "enode://abcd@1.2.3.4:1111?raftport=2222";
    public static final String ENODE_ADDRESS_NO_RAFT = "enode://abcd@1.2.3.4:1111";
    public static final String TEMP_DIR = System.getProperty("user.dir") + "/target";
    public static final String FILE_NAME = "static-nodes.json";
    @Mock
    GethConfig gethConfig;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    RestTemplate restTemplate;

    GethRunner gethRunner;

    @BeforeMethod
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(gethConfig.getGethDataDirPath()).thenReturn(TEMP_DIR);

        ArrayList<Object> existingList = new ArrayList<>();
        existingList.add(ENODE_ADDRESS_NO_RAFT);
        when(objectMapper.readValue(any(File.class), any(TypeReference.class))).thenReturn(
            existingList);

        gethRunner = new GethRunner(gethConfig, objectMapper, restTemplate);
    }

    @AfterMethod
    public void tearDown() {
        FileUtils.deleteQuietly(new File(TEMP_DIR, FILE_NAME));
    }

    @Test
    public void testAddToEnodesConfig_fileDoesntExist() throws IOException {
        List<String> expectedNodeList = new ArrayList<>();
        expectedNodeList.add(ENODE_ADDRESS);
        gethRunner.addToEnodesConfig(ENODE_ADDRESS, FILE_NAME);
        // should not load from file if it doesn't exist
        verify(objectMapper, never()).readValue(any(File.class), any(TypeReference.class));
        verify(objectMapper).writeValue(new File(TEMP_DIR, FILE_NAME), expectedNodeList);
    }

    @Test
    public void testAddToEnodesConfig_fileExists() throws IOException {
        FileUtils.touch(new File(TEMP_DIR, FILE_NAME));

        List<String> expectedNodeList = new ArrayList<>();
        expectedNodeList.add(ENODE_ADDRESS_NO_RAFT);
        expectedNodeList.add(ENODE_ADDRESS);
        gethRunner.addToEnodesConfig(ENODE_ADDRESS, FILE_NAME);
        verify(objectMapper).writeValue(new File(TEMP_DIR, FILE_NAME), expectedNodeList);
    }

    @Test
    public void testRemoveFromEnodesConfig() throws IOException {
        FileUtils.touch(new File(TEMP_DIR, FILE_NAME));

        List<String> expectedNodeList = new ArrayList<>();
        gethRunner.removeFromEnodesConfig(ENODE_ADDRESS_NO_RAFT, FILE_NAME);
        verify(objectMapper).writeValue(new File(TEMP_DIR, FILE_NAME), expectedNodeList);
    }

    @Test
    public void testRemoveFromEnodesConfig_notInList() throws IOException {
        FileUtils.touch(new File(TEMP_DIR, FILE_NAME));

        List<String> expectedNodeList = new ArrayList<>();
        gethRunner.removeFromEnodesConfig(ENODE_ADDRESS, FILE_NAME);
        verify(objectMapper, never()).writeValue(new File(TEMP_DIR, FILE_NAME), expectedNodeList);
    }

    @Test
    public void testCreateEnodeURL() throws IOException {
        String enodeURL = gethRunner.formatEnodeUrl("abcd", "1.2.3.4", "1111", "2222");
        assertEquals(enodeURL, ENODE_ADDRESS);
    }

    @Test
    public void testCreateEnodeURL_noRaft() throws IOException {
        String enodeURL = gethRunner.formatEnodeUrl("abcd", "1.2.3.4", "1111", null);
        assertEquals(enodeURL, ENODE_ADDRESS_NO_RAFT);
    }
}
