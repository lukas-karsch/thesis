package karsch.lukas.featureflags;

import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FeatureFlagService {

    private final Set<Feature> enabledFeatures = ConcurrentHashMap.newKeySet();

    public boolean isEnabled(Feature feature) {
        return enabledFeatures.contains(feature);
    }

    public void enable(Feature feature) {
        enabledFeatures.add(feature);
    }

    public void disable(Feature feature) {
        enabledFeatures.remove(feature);
    }

    public Set<Feature> getEnabledFeatures() {
        return EnumSet.copyOf(enabledFeatures);
    }
}
