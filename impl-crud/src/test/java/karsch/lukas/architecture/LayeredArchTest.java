package karsch.lukas.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "karsch.lukas")
public class LayeredArchTest {

    @ArchTest
    public static final ArchRule controllersShouldBeAnnotatedWithRestController = classes()
            .that().haveSimpleNameEndingWith("Controller").and().areNotInterfaces()
            .should().beAnnotatedWith(RestController.class);

    @ArchTest
    public static final ArchRule noClassShouldDependOnControllers = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().onlyBeAccessed().byClassesThat().resideOutsideOfPackages("karsch.lukas");

}
