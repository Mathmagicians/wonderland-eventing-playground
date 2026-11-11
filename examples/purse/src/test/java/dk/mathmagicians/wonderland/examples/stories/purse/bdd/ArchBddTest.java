package dk.mathmagicians.wonderland.examples.stories.purse.bdd;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/// Runs the story's architecture-discovery features under `src/test/resources/features` through the
/// JUnit Platform Suite engine, `make arch-bdd`: the glue is the platform's architecture steps,
/// which read what the build wrote, no image, no broker. The reports are its own, under
/// `build/reports/arch`.
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "dk.mathmagicians.wonderland.architecture.bdd")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@architecture-discovery")
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value =
                "pretty, html:build/reports/arch/index.html,"
                        + " json:build/reports/arch/cucumber.json,"
                        + " pretty:build/reports/arch/pretty.txt")
class ArchBddTest {}
