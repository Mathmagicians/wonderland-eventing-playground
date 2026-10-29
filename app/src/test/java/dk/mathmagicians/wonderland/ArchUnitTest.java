package dk.mathmagicians.wonderland;

import com.tngtech.archunit.junit.AnalyzeClasses;
import dk.mathmagicians.wonderland.architecture.HexagonalArchitecture;
import dk.mathmagicians.wonderland.architecture.Production;

/// The ArchUnit rules over the distribution, the hexagonal rule: the platform and every story on
/// the classpath.
@AnalyzeClasses(packagesOf = EventingPlatform.class, importOptions = Production.class)
class ArchUnitTest extends HexagonalArchitecture {}
