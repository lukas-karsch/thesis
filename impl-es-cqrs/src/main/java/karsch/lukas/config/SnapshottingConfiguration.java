package karsch.lukas.config;

import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SnapshottingConfiguration {

    public static final int SNAPSHOT_TRIGGER_THRESHOLD = 25;

    @Bean(name = "snapshotTriggerDefinition")
    public SnapshotTriggerDefinition snapshotTriggerDefinition(Snapshotter snapshotter) {
        return new EventCountSnapshotTriggerDefinition(snapshotter, SNAPSHOT_TRIGGER_THRESHOLD);
    }
}
