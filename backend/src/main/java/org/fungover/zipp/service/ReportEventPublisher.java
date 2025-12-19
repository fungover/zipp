package org.fungover.zipp.service;

import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.kafka.ReportAvro;
import org.fungover.zipp.mapper.ReportDtoToAvroMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReportEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(ReportEventPublisher.class);

    private final KafkaTemplate<String, ReportAvro> template;
    private final ReportDtoToAvroMapper mapper;
    private final String topic;

    public ReportEventPublisher(KafkaTemplate<String, ReportAvro> template, ReportDtoToAvroMapper mapper,
            @Value("${app.kafka.topic.report}") String topic) {
        this.template = template;
        this.mapper = mapper;
        this.topic = topic;
    }

    public void publishReportCreated(ReportResponse report) {
        ReportAvro avro = mapper.toAvro(report);

        template.send(topic, report.submittedByUserId(), avro).whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error("Failed to publish report {}", report, ex);
            } else {
                LOG.debug("Published report {}", report);
            }
        });
    }
}
