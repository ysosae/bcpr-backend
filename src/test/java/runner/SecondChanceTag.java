package runner;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@SelectClasspathResource("classpath:features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "features.graphQL, features.commons, features.middleware")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features/graphQL")
@ExcludeTags("IGNORE")
@IncludeTags("secondChance")
public class SecondChanceTag {}
