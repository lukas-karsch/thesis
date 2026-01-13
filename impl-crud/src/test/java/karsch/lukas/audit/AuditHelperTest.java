package karsch.lukas.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditHelperTest {

    static class TesteeClass {
    }

    @Test
    void getNameFromEntityClass() {
        assertThat(AuditHelper.getNameFromEntityClass(TesteeClass.class)).isEqualTo("TesteeClass");
    }
}
