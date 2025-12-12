package org.fungover.zipp.service;

import org.fungover.zipp.kafka.ReportAvro;
import org.fungover.zipp.mapper.ReportAvroMapper;
import org.fungover.zipp.dto.ReportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReportEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(ReportEventPublisher.class);
    private final KafkaTemplate<String, ReportAvro> template;
    private final ReportAvroMapper mapper;

    public ReportEventPublisher(
        KafkaTemplate<String, ReportAvro> template,
        ReportAvroMapper mapper
    ) {
        this.template = template;
        this.mapper = mapper;
    }

    public void publishReportCreated(ReportResponse report) {
        var avro = mapper.toAvro(report);

        template.send(
            "report-avro",
            String.valueOf(report.submittedByUserId()),
            avro
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error("Failed to publish report {}", report, ex);
            } else {
                LOG.debug("Published report {}", report);
            }
        });
    }
}
