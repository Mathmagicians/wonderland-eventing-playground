package dk.mathmagicians.wonderland.bdd;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/// Runs the features of the repository itself under `src/test/resources/features`, how we work,
/// through the JUnit Platform Suite engine: `make bdd`. The glue is this package's; what it reads,
/// the fixtures' step patterns, the feature files, the stories' reports, is in the repository.
/// Plugins and report paths are in the platform's `junit-platform.properties`.
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "dk.mathmagicians.wonderland.bdd")
class RunCucumberTest {}
