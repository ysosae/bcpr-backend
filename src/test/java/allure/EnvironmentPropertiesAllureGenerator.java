package allure;

import static enums.FilesPath.ALLURE_REPORT_FILE_LOCATION;
import config.RestAssuredPropertiesConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Logger;


public class EnvironmentPropertiesAllureGenerator {
    private static final Properties prop = new Properties();
    private static final Logger log = Logger.getLogger(EnvironmentPropertiesAllureGenerator.class);
    public static String environment;
    public static String osPlatform;
    public static String osRelease;
    public static String osVersion;
    public static String javaVersion;
    public static String nameRelease;
    public static String nameBranch;
    public static String namePipeline;

    public static void initConfig() {
        try {
            InputStream input;
            input = new FileInputStream(ALLURE_REPORT_FILE_LOCATION.getText());
            prop.load(input);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        environment = RestAssuredPropertiesConfig.getEnvironment();
        prop.setProperty("Environment", environment);


        osPlatform = System.getProperty("os.name").toLowerCase();
        prop.setProperty("os_platform", osPlatform);

        osRelease = System.getProperty("os.version");
        prop.setProperty("os_release", osRelease);

        osVersion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        prop.setProperty("os_version", osVersion);

        String pythonVersion = getPythonVersion();
        prop.setProperty("python_version", pythonVersion);

        javaVersion = System.getProperty("java.version");
        prop.setProperty("java_version", javaVersion);

        nameRelease = System.getProperty("java.version");
        prop.setProperty("name_release", nameRelease);

        nameBranch = System.getProperty("java.version");
        prop.setProperty("name_branch", nameBranch);

        namePipeline = System.getProperty("java.version");
        prop.setProperty("name_pipeline", namePipeline);

    }

    private static String getPythonVersion() {
        try {
            Process process = Runtime.getRuntime().exec("python --version");
            java.io.InputStream is = process.getInputStream();
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            String version = s.hasNext() ? s.next() : "";
            if (version.isEmpty()) {
                is = process.getErrorStream();
                s = new java.util.Scanner(is).useDelimiter("\\A");
                version = s.hasNext() ? s.next() : "";
            }
            return version.trim();
        } catch (IOException e) {
            log.error(e.getMessage());
            return "Python not found";
        }
    }

}
