package dk.mathmagicians.wonderland;

import com.tngtech.archunit.junit.AnalyzeClasses;
import dk.mathmagicians.wonderland.architecture.HexagonalArchitecture;
import dk.mathmagicians.wonderland.architecture.Production;

/// The hexagonal rule over the distribution: the platform and every story on the classpath.
@AnalyzeClasses(packagesOf = EventingPlatform.class, importOptions = Production.class)
class ArchitectureTest extends HexagonalArchitecture {}
