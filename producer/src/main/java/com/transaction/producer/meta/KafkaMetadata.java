package com.transaction.producer.meta;

import com.transaction.producer.KafkaConfigurations;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Properties;

/**
 * Admin Client to fetch Kafka Cluster Metadata.
 */
@Component
public class KafkaMetadata {
    private static Logger log = LoggerFactory.getLogger(KafkaMetadata.class);

    @Autowired
    private KafkaConfigurations kConfig;

    @Scheduled(fixedRateString = "${kafka.metadata.request}")
    public void getMetadata() {
        log.info("Getting Kafka cluster Metadata");
        AdminClient client = null;
        try {
            client = KafkaAdminClient.create(getProperties());
            DescribeClusterResult cluster = client.describeCluster();

            Collection<Node> kNodes = cluster.nodes().get();
            for (Node n : kNodes) {
                log.info("Kafka Cluster Node IP : " + n.host() + ":" + n.port());
            }
        } catch (Exception e) {
            log.error("Kafka Cluster Metadata is not available. Retrying in {}ms...",
                    kConfig.getMetadataRequestInterval());
        } finally {
            log.info("Closing admin client connection.");
            if (client != null) {
                client.close(Duration.ofMillis(100));
            }
        }
    }

    /**
     * Assuming that atleast one bootstrap server IP is known.
     */
    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kConfig.getBootstrapServer());
        return properties;
    }
}
