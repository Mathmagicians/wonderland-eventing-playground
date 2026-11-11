package dk.mathmagicians.wonderland.architecture.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

/// The context of an architecture-discovery feature: the driver alone, no application, no
/// container. The feature reads what the build wrote.
@CucumberContextConfiguration
@ContextConfiguration(classes = ArchitectureModel.class)
public class ArchitectureContext {}
