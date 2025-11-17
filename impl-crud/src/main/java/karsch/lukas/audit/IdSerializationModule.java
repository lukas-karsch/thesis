package karsch.lukas.audit;

import com.fasterxml.jackson.databind.module.SimpleModule;

class IdSerializationModule extends SimpleModule {
    public IdSerializationModule() {
        setSerializerModifier(new IdPropertySerializerModifier());
    }
}
