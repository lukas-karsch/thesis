package karsch.lukas.uuid;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

// taken from https://manbunder.medium.com/streamline-uuid-v7-generation-in-spring-boot-entities-with-custom-annotations-hibernate-6-5-4ddc018895cf

public class UuidV7Generator extends SequenceStyleGenerator {
    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        return UuidUtils.randomV7();
    }
}
