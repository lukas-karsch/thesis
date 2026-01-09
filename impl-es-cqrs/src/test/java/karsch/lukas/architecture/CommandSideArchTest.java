package karsch.lukas.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "karsch.lukas.features")
public class CommandSideArchTest {

    @ArchTest
    public static ArchRule commandSideShouldNotDependOnReadSide = classes()
            .that().resideInAPackage("..command..")
            .should().onlyDependOnClassesThat().resideOutsideOfPackage("..queries..");
}
