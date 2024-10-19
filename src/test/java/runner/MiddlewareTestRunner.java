package runner;

import static io.cucumber.junit.platform.engine.Constants.*;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasspathResource("classpath:features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "features.commons, features.middleware")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features.middleware")
@ExcludeTags("IGNORE")
public class MiddlewareTestRunner {}
