package org.fungover.zipp.consumer;

import org.fungover.zipp.service.SseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportEventConsumerTest {

    @Mock
    private SseService sseService;

    @InjectMocks
    private ReportEventConsumer consumer;

    @Test
    void testConsumeCallsSseService() {
        String testMessage = "Hello from test";

        consumer.consume(testMessage);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sseService).send(captor.capture(), captor.capture());

        assertThat(captor.getAllValues().get(1)).isEqualTo(testMessage);
    }
}

