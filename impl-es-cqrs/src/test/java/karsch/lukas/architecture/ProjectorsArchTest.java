package karsch.lukas.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.axonframework.config.ProcessingGroup;
import org.springframework.stereotype.Component;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "karsch.lukas")
public class ProjectorsArchTest {

    @ArchTest
    public static ArchRule projectorsShouldBeAnnotatedWithComponent = classes()
            .that().haveSimpleNameEndingWith("Projector")
            .should().beAnnotatedWith(Component.class);

    @ArchTest
    public static ArchRule projectorsShouldHaveAProcessingGroup = classes()
            .that().haveSimpleNameEndingWith("Projector")
            .should().beAnnotatedWith(ProcessingGroup.class);

    @ArchTest
    public static ArchRule projectorsShouldBePackagePrivate = classes()
            .that().haveSimpleNameEndingWith("Projector")
            .should().bePackagePrivate();
}
