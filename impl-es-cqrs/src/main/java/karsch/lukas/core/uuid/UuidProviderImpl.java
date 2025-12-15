package karsch.lukas.core.uuid;

import karsch.lukas.uuid.UuidUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidProviderImpl implements UuidProvider {

    @Override
    public UUID generateUuid() {
        return UuidUtils.randomV7();
    }
}
