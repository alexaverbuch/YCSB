package com.ldbc.driver.workloads.ldbc.snb.interactive;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ldbc.driver.*;
import com.ldbc.driver.control.ConcurrentControlService;
import com.ldbc.driver.control.ConsoleAndFileDriverConfiguration;
import com.ldbc.driver.control.DriverConfigurationException;
import com.ldbc.driver.control.LocalControlService;
import com.ldbc.driver.runtime.ConcurrentErrorReporter;
import com.ldbc.driver.runtime.coordination.CompletionTimeException;
import com.ldbc.driver.runtime.metrics.ConcurrentMetricsService;
import com.ldbc.driver.runtime.metrics.MetricsCollectionException;
import com.ldbc.driver.runtime.metrics.WorkloadResultsSnapshot;
import com.ldbc.driver.temporal.SystemTimeSource;
import com.ldbc.driver.temporal.TemporalUtil;
import com.ldbc.driver.temporal.TimeSource;
import com.ldbc.driver.util.MapUtils;
import com.ldbc.driver.workloads.ldbc.snb.interactive.db.DummyLdbcSnbInteractiveDb;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LdbcSnbInteractiveWorkloadPerformanceTest {
    private static final TemporalUtil TEMPORAL_UTIL = new TemporalUtil();
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    TimeSource timeSource = new SystemTimeSource();

    @Ignore
    @Test
    public void performanceTest()
            throws InterruptedException, DbException, WorkloadException, IOException, MetricsCollectionException, CompletionTimeException, DriverConfigurationException {
        File paramsDir = new File("/Users/alexaverbuch/IdeaProjects/scale_factor_streams/new_read_params/sf10_partitions_01/");
        File parentStreamsDir = new File("/Users/alexaverbuch/IdeaProjects/scale_factor_streams/current/");
        List<File> streamsDirs = Lists.newArrayList(
//                new File(parentStreamsDir, "sf10_partitions_01/")
                new File(parentStreamsDir, "sf10_partitions_04/")
//                new File(parentStreamsDir, "sf10_partitions_16/")
        );

        for (File streamDir : streamsDirs) {
            Iterable<String> personFilePaths = Iterables.transform(
                    Lists.newArrayList(
                            streamDir.listFiles(
                                    new FilenameFilter() {
                                        @Override
                                        public boolean accept(File dir, String name) {
                                            return name.endsWith("_person.csv");
                                        }
                                    }
                            )
                    ),
                    new Function<File, String>() {
                        @Override
                        public String apply(File file) {
                            return file.getAbsolutePath();
                        }
                    }
            );
            Iterable<String> forumFilePaths = Iterables.transform(
                    Lists.newArrayList(
                            streamDir.listFiles(
                                    new FilenameFilter() {
                                        @Override
                                        public boolean accept(File dir, String name) {
                                            return name.endsWith("_forum.csv");
                                        }
                                    }
                            )
                    ),
                    new Function<File, String>() {
                        @Override
                        public String apply(File file) {
                            return file.getAbsolutePath();
                        }
                    }
            );

            List<Integer> threadCounts = Lists.newArrayList(1, 2, 4);
            long operationCount = 10000000;
            for (int threadCount : threadCounts) {
                doPerformanceTest(
                        threadCount,
                        operationCount,
                        forumFilePaths,
                        personFilePaths,
                        paramsDir.getAbsolutePath(),
                        streamDir.getAbsolutePath(),
                        "TC" + threadCount + "-" + streamDir.getName(),
                        new File(streamDir, "updateStream.properties").getAbsolutePath()
                );
            }
        }
    }

    public void doPerformanceTest(int threadCount,
                                  long operationCount,
                                  Iterable<String> forumFilePaths,
                                  Iterable<String> personFilePaths,
                                  String parametersDir,
                                  String resultsDir,
                                  String name,
                                  String updateStreamPropertiesPath)
            throws InterruptedException, DbException, WorkloadException, IOException, MetricsCollectionException, CompletionTimeException, DriverConfigurationException {
        ConcurrentErrorReporter errorReporter = new ConcurrentErrorReporter();
        ConcurrentControlService controlService = null;
        Db db = null;
        Workload workload = null;
        ConcurrentMetricsService metricsService = null;
        try {
            Map<String, String> paramsMap = LdbcSnbInteractiveConfiguration.defaultConfig();
            paramsMap.put(LdbcSnbInteractiveConfiguration.PARAMETERS_DIRECTORY, parametersDir);
            paramsMap.put(LdbcSnbInteractiveConfiguration.FORUM_UPDATE_FILES, LdbcSnbInteractiveConfiguration.serializeFilePathsListFromConfiguration(forumFilePaths));
            paramsMap.put(LdbcSnbInteractiveConfiguration.PERSON_UPDATE_FILES, LdbcSnbInteractiveConfiguration.serializeFilePathsListFromConfiguration(personFilePaths));
            paramsMap.put(DummyLdbcSnbInteractiveDb.SLEEP_DURATION_MILLI_ARG, "0");
            // Driver-specific parameters
            String dbClassName = DummyLdbcSnbInteractiveDb.class.getName();
            String workloadClassName = LdbcSnbInteractiveWorkload.class.getName();
            int statusDisplayInterval = 2;
            TimeUnit timeUnit = TimeUnit.MICROSECONDS;
            String resultDirPath = resultsDir;
            double timeCompressionRatio = 1.0;
            long windowedExecutionWindowDuration = 1000l;
            Set<String> peerIds = new HashSet<>();
            long toleratedExecutionDelay = TEMPORAL_UTIL.convert(60, TimeUnit.MINUTES, TimeUnit.MILLISECONDS);
            ConsoleAndFileDriverConfiguration.ConsoleAndFileValidationParamOptions validationParams = null;
            String dbValidationFilePath = null;
            boolean validateWorkload = false;
            boolean calculateWorkloadStatistics = false;
            long spinnerSleepDuration = 0;
            boolean printHelp = false;
            boolean ignoreScheduledStartTimes = true;
            boolean shouldCreateResultsLog = true;

            ConsoleAndFileDriverConfiguration configuration = new ConsoleAndFileDriverConfiguration(
                    paramsMap,
                    name,
                    dbClassName,
                    workloadClassName,
                    operationCount,
                    threadCount,
                    statusDisplayInterval,
                    timeUnit,
                    resultDirPath,
                    timeCompressionRatio,
                    windowedExecutionWindowDuration,
                    peerIds,
                    toleratedExecutionDelay,
                    validationParams,
                    dbValidationFilePath,
                    validateWorkload,
                    calculateWorkloadStatistics,
                    spinnerSleepDuration,
                    printHelp,
                    ignoreScheduledStartTimes,
                    shouldCreateResultsLog
            );

            configuration = (ConsoleAndFileDriverConfiguration) configuration.applyMap(MapUtils.loadPropertiesToMap(new File(updateStreamPropertiesPath)));

            // When
            Client client = new Client(new LocalControlService(timeSource.nowAsMilli() + 3000, configuration), timeSource);
            client.start();

            // Then
            File resultsFile = new File(resultsDir, name + "-results.json");
            WorkloadResultsSnapshot resultsSnapshot = WorkloadResultsSnapshot.fromJson(resultsFile);

            double operationsPerSecond = Math.round(((double) operationCount / resultsSnapshot.totalRunDurationAsNano()) * TEMPORAL_UTIL.convert(1, TimeUnit.SECONDS, TimeUnit.NANOSECONDS));
            double microSecondPerOperation = (double) TEMPORAL_UTIL.convert(resultsSnapshot.totalRunDurationAsNano(), TimeUnit.NANOSECONDS, TimeUnit.MICROSECONDS) / operationCount;
            DecimalFormat numberFormatter = new DecimalFormat("###,###,###,###");
            System.out.println(
                    String.format("[%s]Completed %s operations in %s = %s op/sec = 1 op/%s us",
                            name,
                            numberFormatter.format(operationCount),
                            resultsSnapshot.totalRunDurationAsNano(),
                            numberFormatter.format(operationsPerSecond),
                            microSecondPerOperation
                    )
            );
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (errorReporter.errorEncountered())
                System.out.println(errorReporter.toString());
            if (null != controlService) controlService.shutdown();
            if (null != db) db.close();
            if (null != workload) workload.close();
            if (null != metricsService) metricsService.shutdown();
        }
    }
}