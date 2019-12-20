package com.hello2morrow.sonargraph.integration.sonarqube;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hello2morrow.sonargraph.integration.access.controller.ControllerFactory;
import com.hello2morrow.sonargraph.integration.access.controller.IMetaDataController;
import com.hello2morrow.sonargraph.integration.access.foundation.ResultWithOutcome;
import com.hello2morrow.sonargraph.integration.access.model.IExportMetaData;
import com.hello2morrow.sonargraph.integration.access.model.IIssueType;
import com.hello2morrow.sonargraph.integration.access.model.IMetricId;
import com.hello2morrow.sonargraph.integration.access.model.IMetricLevel;

public class ExportMetaDataXmlToPropertiesConverter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportMetaDataXmlToPropertiesConverter.class);

    private static final String RESOURCES_PATH = "./src/main/resources/com/hello2morrow/sonargraph/integration/sonarqube";
    private static final String BUILT_IN_META_DATA_RESOURCE_PATH = "/com/hello2morrow/sonargraph/integration/sonarqube/ExportMetaData.xml";

    public ExportMetaDataXmlToPropertiesConverter()
    {
        super();
    }

    private void convert() throws IOException
    {
        final IExportMetaData metaData = readBuiltInMetaData();
        convertRules(metaData);
        convertMetrics(metaData);
    }

    public int convertMetrics(final IExportMetaData metaData) throws IOException
    {
        final Map<String, IMetricId> standardMetrics = new HashMap<>();
        getMetricsForLevel(metaData, metaData.getMetricLevels().get(IMetricLevel.SYSTEM), standardMetrics);
        getMetricsForLevel(metaData, metaData.getMetricLevels().get(IMetricLevel.MODULE), standardMetrics);

        final SonargraphMetricsProvider metricsProvider = new SonargraphMetricsProvider();
        final Properties metricProperties = new Properties();
        final Collection<IMetricId> sonargraphMetrics = standardMetrics.values();
        sonargraphMetrics.forEach(m -> metricsProvider.addMetric(m, metricProperties));
        LOGGER.info("Created " + standardMetrics.size() + " standard metrics");
        final File targetDirectory = new File(RESOURCES_PATH);
        metricsProvider.save(metricProperties, targetDirectory, "Standard Sonargraph Metrics");

        return sonargraphMetrics.size();
    }

    public int convertRules(final IExportMetaData metaData) throws IOException
    {
        final List<IIssueType> issueTypes = new ArrayList<>(metaData.getIssueTypes().values());
        final SonargraphRulesProvider rulesProvider = new SonargraphRulesProvider();
        final Properties ruleProperties = new Properties();
        issueTypes.forEach(issueType -> rulesProvider.addRule(issueType, ruleProperties));
        LOGGER.info("Created " + issueTypes.size() + " standard rules");
        final File targetDirectory = new File(RESOURCES_PATH);
        rulesProvider.save(ruleProperties, targetDirectory, "Standard Sonargraph Rules / Issue Types");

        return ruleProperties.size();
    }

    public IExportMetaData readBuiltInMetaData()
    {
        final String errorMsg = "Failed to load built in meta data from '" + BUILT_IN_META_DATA_RESOURCE_PATH + "'";
        try (InputStream inputStream = ExportMetaDataXmlToPropertiesConverter.class.getResourceAsStream(BUILT_IN_META_DATA_RESOURCE_PATH))
        {
            if (inputStream != null)
            {
                final IMetaDataController controller = ControllerFactory.createMetaDataController();
                final ResultWithOutcome<IExportMetaData> result = controller.loadExportMetaData(inputStream, BUILT_IN_META_DATA_RESOURCE_PATH);
                if (result.isFailure())
                {
                    LOGGER.error(errorMsg + " - " + result.toString());
                }
                else
                {
                    return result.getOutcome();
                }
            }
            else
            {
                LOGGER.error(errorMsg);
            }
        }
        catch (final IOException ex)
        {
            LOGGER.error(errorMsg, ex);
        }

        return null;
    }

    private void getMetricsForLevel(final IExportMetaData builtInMetaData, final IMetricLevel level, final Map<String, IMetricId> metricMap)
    {
        for (final IMetricId next : builtInMetaData.getMetricIdsForLevel(level))
        {
            if (!metricMap.containsKey(next.getName()))
            {
                metricMap.put(next.getName(), next);
            }
        }
    }

    public static void main(final String[] args) throws IOException
    {
        final ExportMetaDataXmlToPropertiesConverter converter = new ExportMetaDataXmlToPropertiesConverter();
        converter.convert();
    }
}