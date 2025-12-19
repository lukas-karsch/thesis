package karsch.lukas.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "karsch.lukas.features")
public class ValidatorsArchTest {

    @ArchTest
    public static final ArchRule validatorsShouldBeAnnotated = classes()
            .that().haveSimpleNameEndingWith("Validator").and().areNotInterfaces()
            .should().beAnnotatedWith(Service.class);
}
