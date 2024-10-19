package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

public class CredentialsConfig extends AbstractAPI {
  private static final Logger log = Logger.getLogger(CredentialsConfig.class);
  private static final Properties prop = new Properties();
  private static final String GLOBAL_DATA_FILE_LOCATION = "test.properties";

  public static void initConfig() {
    try {
      InputStream input;
      input = new FileInputStream(GLOBAL_DATA_FILE_LOCATION);
      prop.load(input);
    } catch (IOException e) {
     log.error(e.getMessage());
    }
  }

  public CredentialsConfig() {
    CredentialsConfig.initConfig();
  }
}
