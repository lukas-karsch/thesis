package karsch.lukas.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "karsch.lukas")
class RepositoryArchTest {

    @ArchTest
    public static final ArchRule repositoriesShouldBeCorrectlyAnnotated = classes()
            .that().haveSimpleNameEndingWith("Repository")
            .should().beAnnotatedWith(Repository.class);
}
