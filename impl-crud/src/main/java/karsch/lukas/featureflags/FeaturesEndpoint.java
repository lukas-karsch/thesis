package karsch.lukas.featureflags;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Actuator endpoint to control feature flags.
 */
@Component
@Endpoint(id = "features")
@RequiredArgsConstructor
public class FeaturesEndpoint {

    private final FeatureFlagService featureFlagService;

    @ReadOperation
    public Set<Feature> features() {
        return featureFlagService.getEnabledFeatures();
    }

    @WriteOperation
    public void configureFeature(String name, boolean enabled) {
        Feature feature = Feature.valueOf(name.toUpperCase());
        if (enabled) {
            featureFlagService.enable(feature);
        } else {
            featureFlagService.disable(feature);
        }
    }
}
