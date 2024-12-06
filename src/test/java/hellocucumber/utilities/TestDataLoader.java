package hellocucumber.utilities;

/*
Set the filename for INPUT Data as SystemProperty when executing the test via Maven
to get TestData via Execution use method getTestData, to write testdata in File to reuse later again use method setTestData
*/

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hellocucumber.steps.Hook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TestDataLoader {
    private static final Logger LOG = LogManager.getLogger(TestDataLoader.class);
    private static final ThreadLocal<Map<String, String>> specifiedEnvData = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, String>> threadLocalTestDataRuntime = new ThreadLocal<>();
    private static Random random = new Random();

    public TestDataLoader() {
        specifiedEnvData.set(readJsonFile("INPUT_" + Hook.testedEnv.toUpperCase() + ".json"));
        assert specifiedEnvData.get() != null;
        threadLocalTestDataRuntime.set(mergeTestData(specifiedEnvData.get()));
    }

    public static Map<String, String> readJsonFile(String dataFileToLoad) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File jsonFile = new File("src/test/resources/testdata/" + dataFileToLoad);
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            Map<String, String> mergedMap = new HashMap<>();

            Iterator<String> fieldNames = rootNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldNode = rootNode.get(fieldName);
                if (fieldNode.isObject()) {
                    Iterator<String> innerFieldNames = fieldNode.fieldNames();
                    while (innerFieldNames.hasNext()) {
                        String innerFieldName = innerFieldNames.next();
                        JsonNode innerValue = fieldNode.get(innerFieldName);
                        mergedMap.put(
                                innerFieldName, innerValue.isTextual() ? innerValue.asText() : innerValue.toString());
                    }
                } else {
                    mergedMap.put(fieldName, fieldNode.isTextual() ? fieldNode.asText() : fieldNode.toString());
                }
            }
            return mergedMap;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    public static Map<String, String> mergeTestData(Map<String, String> envSpecific) {
        Set<String> duplicates = findDuplicateKeys(envSpecific);
        LOG.info("found duplicate keys: {}", duplicates);
        Map<String, String> environmentVariables = System.getenv();
        environmentVariables.forEach((key, value) -> LOG.info("Key {} has value {}", key, value));
        envSpecific.putAll(envSpecific);
        return envSpecific;
    }

    public static Set<String> findDuplicateKeys(Map<String, ?>... maps) {
        Set<String> keysSet = new HashSet<>();
        Set<String> duplicateKeys = new HashSet<>();

        for (Map<String, ?> map : maps) {
            for (String key : map.keySet()) {
                if (!keysSet.add(key)) {
                    duplicateKeys.add(key);
                }
            }
        }
        return duplicateKeys;
    }

    public static String getTestData(String key) {
        assert threadLocalTestDataRuntime.get() != null;
        String value = "";

        if (key.startsWith("@TD:") || key.startsWith("@td:")) {
            int end = key.length();
            value = threadLocalTestDataRuntime.get().get(key.substring(4, end));
            checkTestData(key, value);
        } else {
            value = key;
        }
        return value;
    }

    public static void checkTestData(String key, String value) {
        if (value == null) {
            throw new NullPointerException("your key " + key + " is not pointing to a Value!");
        } else {
            String lowercaseKey = key.toLowerCase();
            if (lowercaseKey.contains("password") || lowercaseKey.contains("pw")) {
                LOG.info("Loading \"{}\" as \"*********\" from Test Data Set", key);
            } else {
                LOG.info("Loading \"{}\" as \"{}\" from Test Data Set", value, key);
            }
        }
    }

    public static void setTestData(String key, String value) {
        assert threadLocalTestDataRuntime.get() != null;
        threadLocalTestDataRuntime.get().put(key, value);
        if (Hook.threadLocalDataSetInExecution.get() == null) Hook.threadLocalDataSetInExecution.set(new HashMap<>());
        Hook.threadLocalDataSetInExecution.get().put(key, value);
        LOG.info("Saving \"{}\" as \"{}\" in Test Data Set", value, key);
    }
}
