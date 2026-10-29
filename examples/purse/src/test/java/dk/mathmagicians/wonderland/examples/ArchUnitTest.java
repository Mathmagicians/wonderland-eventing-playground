package dk.mathmagicians.wonderland.examples;

import com.tngtech.archunit.junit.AnalyzeClasses;
import dk.mathmagicians.wonderland.EventingPlatform;
import dk.mathmagicians.wonderland.architecture.HexagonalArchitecture;
import dk.mathmagicians.wonderland.architecture.Production;

/// The ArchUnit rules, the hexagonal rule, over the platform and this story as one system.
@AnalyzeClasses(packagesOf = EventingPlatform.class, importOptions = Production.class)
class ArchUnitTest extends HexagonalArchitecture {}
