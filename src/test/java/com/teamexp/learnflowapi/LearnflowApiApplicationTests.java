package com.teamexp.learnflowapi;

import com.google.cloud.storage.Storage;
import com.teamexp.learnflowapi.content.external.GcpFileUploadService;
import com.teamexp.learnflowapi.content.external.GcpSignedUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LearnflowApiApplicationTests {

    @MockBean
    private Storage storage;

    @MockBean
    private GcpSignedUrlService gcpSignedUrlService;

    @MockBean
    private GcpFileUploadService gcpFileUploadService;

    @Test
    void contextLoads() {
    }

}
