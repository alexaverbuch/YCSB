package com.ldbc.driver;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.ldbc.driver.control.ConsoleAndFileDriverConfiguration;
import com.ldbc.driver.control.DriverConfigurationException;
import com.ldbc.driver.generator.GeneratorFactory;
import com.ldbc.driver.generator.RandomDataGeneratorFactory;
import com.ldbc.driver.temporal.TemporalUtil;
import com.ldbc.driver.util.Tuple;
import com.ldbc.driver.validation.WorkloadFactory;
import com.ldbc.driver.workloads.dummy.TimedNamedOperation1;
import com.ldbc.driver.workloads.dummy.TimedNamedOperation1Factory;
import com.ldbc.driver.workloads.dummy.TimedNamedOperation2Factory;
import com.ldbc.driver.workloads.dummy.TimedNamedOperation3Factory;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class WorkloadStreamsTest {
    private static final TemporalUtil TEMPORAL_UTIL = new TemporalUtil();

    @Test
    public void shouldReturnSameWorkloadStreamsAsCreatedWith() {
        WorkloadStreams workloadStreamsBefore = getWorkloadStreams();

        Operation<?> firstAsyncDependencyOperation = workloadStreamsBefore.asynchronousStream().dependencyOperations().next();
        Operation<?> secondAsyncDependencyOperation = workloadStreamsBefore.asynchronousStream().dependencyOperations().next();
        assertThat(firstAsyncDependencyOperation.scheduledStartTimeAsMilli(), is(0l));
        assertThat(firstAsyncDependencyOperation.dependencyTimeAsMilli(), is(0l));
        assertThat(secondAsyncDependencyOperation.scheduledStartTimeAsMilli(), is(10l));
        assertThat(secondAsyncDependencyOperation.dependencyTimeAsMilli(), is(10l));

        Operation<?> firstAsyncNonDependencyOperation = workloadStreamsBefore.asynchronousStream().nonDependencyOperations().next();
        Operation<?> secondAsyncNonDependencyOperation = workloadStreamsBefore.asynchronousStream().nonDependencyOperations().next();
        assertThat(firstAsyncNonDependencyOperation.scheduledStartTimeAsMilli(), is(2l));
        assertThat(firstAsyncNonDependencyOperation.dependencyTimeAsMilli(), is(2l));
        assertThat(secondAsyncNonDependencyOperation.scheduledStartTimeAsMilli(), is(102l));
        assertThat(secondAsyncNonDependencyOperation.dependencyTimeAsMilli(), is(102l));

        Operation<?> firstBlocking1DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).dependencyOperations().next();
        Operation<?> secondBlocking1DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).dependencyOperations().next();
        assertThat(firstBlocking1DependencyOperation.scheduledStartTimeAsMilli(), is(4l));
        assertThat(firstBlocking1DependencyOperation.dependencyTimeAsMilli(), is(4l));
        assertThat(secondBlocking1DependencyOperation.scheduledStartTimeAsMilli(), is(1004l));
        assertThat(secondBlocking1DependencyOperation.dependencyTimeAsMilli(), is(1004l));

        Operation<?> firstBlocking1NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).nonDependencyOperations().next();
        Operation<?> secondBlocking1NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).nonDependencyOperations().next();
        assertThat(firstBlocking1NonDependencyOperation.scheduledStartTimeAsMilli(), is(6l));
        assertThat(firstBlocking1NonDependencyOperation.dependencyTimeAsMilli(), is(6l));
        assertThat(secondBlocking1NonDependencyOperation.scheduledStartTimeAsMilli(), is(10006l));
        assertThat(secondBlocking1NonDependencyOperation.dependencyTimeAsMilli(), is(10006l));

        Operation<?> firstBlocking2DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).dependencyOperations().next();
        Operation<?> secondBlocking2DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).dependencyOperations().next();
        assertThat(firstBlocking2DependencyOperation.scheduledStartTimeAsMilli(), is(8l));
        assertThat(firstBlocking2DependencyOperation.dependencyTimeAsMilli(), is(8l));
        assertThat(secondBlocking2DependencyOperation.scheduledStartTimeAsMilli(), is(10008l));
        assertThat(secondBlocking2DependencyOperation.dependencyTimeAsMilli(), is(10008l));

        Operation<?> firstBlocking2NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).nonDependencyOperations().next();
        Operation<?> secondBlocking2NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).nonDependencyOperations().next();
        assertThat(firstBlocking2NonDependencyOperation.scheduledStartTimeAsMilli(), is(10l));
        assertThat(firstBlocking2NonDependencyOperation.dependencyTimeAsMilli(), is(10l));
        assertThat(secondBlocking2NonDependencyOperation.scheduledStartTimeAsMilli(), is(100010l));
        assertThat(secondBlocking2NonDependencyOperation.dependencyTimeAsMilli(), is(100010l));
    }

    @Test
    public void shouldPerformTimeOffsetCorrectly() throws WorkloadException {
        long offset = TEMPORAL_UTIL.convert(100, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
        WorkloadStreams workloadStreamsBefore = WorkloadStreams.timeOffsetAndCompressWorkloadStreams(
                getWorkloadStreams(),
                0l + offset,
                1.0,
                new GeneratorFactory(new RandomDataGeneratorFactory(42l)));

        Operation<?> firstAsyncDependencyOperation = workloadStreamsBefore.asynchronousStream().dependencyOperations().next();
        Operation<?> secondAsyncDependencyOperation = workloadStreamsBefore.asynchronousStream().dependencyOperations().next();
        assertThat(firstAsyncDependencyOperation.scheduledStartTimeAsMilli(), is(0l + offset));
        assertThat(firstAsyncDependencyOperation.dependencyTimeAsMilli(), is(0l + offset));
        assertThat(secondAsyncDependencyOperation.scheduledStartTimeAsMilli(), is(10l + offset));
        assertThat(secondAsyncDependencyOperation.dependencyTimeAsMilli(), is(10l + offset));

        Operation<?> firstAsyncNonDependencyOperation = workloadStreamsBefore.asynchronousStream().nonDependencyOperations().next();
        Operation<?> secondAsyncNonDependencyOperation = workloadStreamsBefore.asynchronousStream().nonDependencyOperations().next();
        assertThat(firstAsyncNonDependencyOperation.scheduledStartTimeAsMilli(), is(2l + offset));
        assertThat(firstAsyncNonDependencyOperation.dependencyTimeAsMilli(), is(2l + offset));
        assertThat(secondAsyncNonDependencyOperation.scheduledStartTimeAsMilli(), is(102l + offset));
        assertThat(secondAsyncNonDependencyOperation.dependencyTimeAsMilli(), is(102l + offset));

        Operation<?> firstBlocking1DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).dependencyOperations().next();
        Operation<?> secondBlocking1DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).dependencyOperations().next();
        assertThat(firstBlocking1DependencyOperation.scheduledStartTimeAsMilli(), is(4l + offset));
        assertThat(firstBlocking1DependencyOperation.dependencyTimeAsMilli(), is(4l + offset));
        assertThat(secondBlocking1DependencyOperation.scheduledStartTimeAsMilli(), is(1004l + offset));
        assertThat(secondBlocking1DependencyOperation.dependencyTimeAsMilli(), is(1004l + offset));

        Operation<?> firstBlocking1NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).nonDependencyOperations().next();
        Operation<?> secondBlocking1NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).nonDependencyOperations().next();
        assertThat(firstBlocking1NonDependencyOperation.scheduledStartTimeAsMilli(), is(6l + offset));
        assertThat(firstBlocking1NonDependencyOperation.dependencyTimeAsMilli(), is(6l + offset));
        assertThat(secondBlocking1NonDependencyOperation.scheduledStartTimeAsMilli(), is(10006l + offset));
        assertThat(secondBlocking1NonDependencyOperation.dependencyTimeAsMilli(), is(10006l + offset));

        Operation<?> firstBlocking2DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).dependencyOperations().next();
        Operation<?> secondBlocking2DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).dependencyOperations().next();
        assertThat(firstBlocking2DependencyOperation.scheduledStartTimeAsMilli(), is(8l + offset));
        assertThat(firstBlocking2DependencyOperation.dependencyTimeAsMilli(), is(8l + offset));
        assertThat(secondBlocking2DependencyOperation.scheduledStartTimeAsMilli(), is(10008l + offset));
        assertThat(secondBlocking2DependencyOperation.dependencyTimeAsMilli(), is(10008l + offset));

        Operation<?> firstBlocking2NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).nonDependencyOperations().next();
        Operation<?> secondBlocking2NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).nonDependencyOperations().next();
        assertThat(firstBlocking2NonDependencyOperation.scheduledStartTimeAsMilli(), is(10l + offset));
        assertThat(firstBlocking2NonDependencyOperation.dependencyTimeAsMilli(), is(10l + offset));
        assertThat(secondBlocking2NonDependencyOperation.scheduledStartTimeAsMilli(), is(100010l + offset));
        assertThat(secondBlocking2NonDependencyOperation.dependencyTimeAsMilli(), is(100010l + offset));
    }

    @Test
    public void shouldPerformTimeOffsetAndCompressionCorrectly() throws WorkloadException {
        long offset = TEMPORAL_UTIL.convert(100, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
        WorkloadStreams workloadStreamsBefore = WorkloadStreams.timeOffsetAndCompressWorkloadStreams(
                getWorkloadStreams(),
                0l + offset,
                0.5,
                new GeneratorFactory(new RandomDataGeneratorFactory(42l)));

        Operation<?> firstAsyncDependencyOperation = workloadStreamsBefore.asynchronousStream().dependencyOperations().next();
        Operation<?> secondAsyncDependencyOperation = workloadStreamsBefore.asynchronousStream().dependencyOperations().next();
        assertThat(firstAsyncDependencyOperation.scheduledStartTimeAsMilli(), is(0l + offset));
        assertThat(secondAsyncDependencyOperation.scheduledStartTimeAsMilli(), is(5l + offset));
        assertThat(secondAsyncDependencyOperation.scheduledStartTimeAsMilli() - firstAsyncDependencyOperation.scheduledStartTimeAsMilli(), is(5l));
        assertThat(secondAsyncDependencyOperation.dependencyTimeAsMilli() - firstAsyncDependencyOperation.dependencyTimeAsMilli(), is(5l));

        Operation<?> firstAsyncNonDependencyOperation = workloadStreamsBefore.asynchronousStream().nonDependencyOperations().next();
        Operation<?> secondAsyncNonDependencyOperation = workloadStreamsBefore.asynchronousStream().nonDependencyOperations().next();
        assertThat(firstAsyncNonDependencyOperation.scheduledStartTimeAsMilli(), is(1l + offset));
        assertThat(firstAsyncNonDependencyOperation.dependencyTimeAsMilli(), is(1l + offset));
        assertThat(secondAsyncNonDependencyOperation.scheduledStartTimeAsMilli(), is(51l + offset));
        assertThat(secondAsyncNonDependencyOperation.dependencyTimeAsMilli(), is(51l + offset));

        Operation<?> firstBlocking1DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).dependencyOperations().next();
        Operation<?> secondBlocking1DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).dependencyOperations().next();
        assertThat(firstBlocking1DependencyOperation.scheduledStartTimeAsMilli(), is(2l + offset));
        assertThat(firstBlocking1DependencyOperation.dependencyTimeAsMilli(), is(2l + offset));
        assertThat(secondBlocking1DependencyOperation.scheduledStartTimeAsMilli(), is(502l + offset));
        assertThat(secondBlocking1DependencyOperation.dependencyTimeAsMilli(), is(502l + offset));

        Operation<?> firstBlocking1NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).nonDependencyOperations().next();
        Operation<?> secondBlocking1NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(0).nonDependencyOperations().next();
        assertThat(firstBlocking1NonDependencyOperation.scheduledStartTimeAsMilli(), is(3l + offset));
        assertThat(firstBlocking1NonDependencyOperation.dependencyTimeAsMilli(), is(3l + offset));
        assertThat(secondBlocking1NonDependencyOperation.scheduledStartTimeAsMilli(), is(5003l + offset));
        assertThat(secondBlocking1NonDependencyOperation.dependencyTimeAsMilli(), is(5003l + offset));

        Operation<?> firstBlocking2DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).dependencyOperations().next();
        Operation<?> secondBlocking2DependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).dependencyOperations().next();
        assertThat(firstBlocking2DependencyOperation.scheduledStartTimeAsMilli(), is(4l + offset));
        assertThat(firstBlocking2DependencyOperation.dependencyTimeAsMilli(), is(4l + offset));
        assertThat(secondBlocking2DependencyOperation.scheduledStartTimeAsMilli(), is(5004l + offset));
        assertThat(secondBlocking2DependencyOperation.dependencyTimeAsMilli(), is(5004l + offset));

        Operation<?> firstBlocking2NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).nonDependencyOperations().next();
        Operation<?> secondBlocking2NonDependencyOperation = workloadStreamsBefore.blockingStreamDefinitions().get(1).nonDependencyOperations().next();
        assertThat(firstBlocking2NonDependencyOperation.scheduledStartTimeAsMilli(), is(5l + offset));
        assertThat(firstBlocking2NonDependencyOperation.dependencyTimeAsMilli(), is(5l + offset));
        assertThat(secondBlocking2NonDependencyOperation.scheduledStartTimeAsMilli(), is(50005l + offset));
        assertThat(secondBlocking2NonDependencyOperation.dependencyTimeAsMilli(), is(50005l + offset));
    }

    @Test
    public void shouldLimitWorkloadCorrectly() throws WorkloadException, DriverConfigurationException, IOException {
        GeneratorFactory gf = new GeneratorFactory(new RandomDataGeneratorFactory(42l));
        WorkloadFactory workloadFactory = new WorkloadFactory() {
            @Override
            public Workload createWorkload() throws WorkloadException {
                return new TestWorkload();
            }
        };
        ConsoleAndFileDriverConfiguration configuration = ConsoleAndFileDriverConfiguration.fromDefaults(null, null, 100);
        Tuple.Tuple2<WorkloadStreams, Workload> limitedWorkloadStreamsAndWorkload = WorkloadStreams.createNewWorkloadWithLimitedWorkloadStreams(workloadFactory, configuration, gf);
        WorkloadStreams workloadStreams = limitedWorkloadStreamsAndWorkload._1();
        Workload workload = limitedWorkloadStreamsAndWorkload._2();
        assertThat(Iterators.size(workloadStreams.mergeSortedByStartTime(gf)), is(100));
        workload.close();
    }

    @Test
    public void shouldLimitStreamsCorrectly() throws WorkloadException {
        GeneratorFactory gf = new GeneratorFactory(new RandomDataGeneratorFactory(42l));

        List<Operation<?>> stream0 = Lists.<Operation<?>>newArrayList(
                new TimedNamedOperation1(0l, 0l, "0-1"),
                new TimedNamedOperation1(1l, 0l, "0-2"),
                new TimedNamedOperation1(2l, 0l, "0-3"),
                new TimedNamedOperation1(6l, 0l, "0-4"),
                new TimedNamedOperation1(7l, 0l, "0-5")
        );

        List<Operation<?>> stream1 = Lists.<Operation<?>>newArrayList(
                new TimedNamedOperation1(0l, 0l, "1-1"),
                new TimedNamedOperation1(3l, 0l, "1-2"),
                new TimedNamedOperation1(4l, 0l, "1-3"),
                new TimedNamedOperation1(9l, 0l, "1-4")
        );

        List<Operation<?>> stream2 = Lists.<Operation<?>>newArrayList(
                new TimedNamedOperation1(1l, 0l, "2-1"),
                new TimedNamedOperation1(3l, 0l, "2-2"),
                new TimedNamedOperation1(4l, 0l, "2-3"),
                new TimedNamedOperation1(8l, 0l, "2-4"),
                new TimedNamedOperation1(8l, 0l, "2-5"),
                new TimedNamedOperation1(9l, 0l, "2-6")
        );

        List<Operation<?>> stream3 = Lists.newArrayList(
        );

        List<Operation<?>> stream4 = Lists.newArrayList(gf.limit(
                        new TimedNamedOperation1Factory(
                                gf.incrementing(10l, 1l),
                                gf.constant(0l),
                                gf.constant("4-x")
                        ),
                        1000000
                )
        );

        List<Iterator<Operation<?>>> streams = Lists.newArrayList(
                stream0.iterator(),
                stream1.iterator(),
                stream2.iterator(),
                stream3.iterator(),
                stream4.iterator()
        );

        long k = 10;
        long[] kForIterator = WorkloadStreams.fromAmongAllRetrieveTopK(streams, k);

        List<Operation<?>> topK = Lists.newArrayList(
                gf.mergeSortOperationsByStartTime(
                        gf.limit(
                                stream0.iterator(),
                                kForIterator[0]
                        ),
                        gf.limit(
                                stream1.iterator(),
                                kForIterator[1]
                        ),
                        gf.limit(
                                stream2.iterator(),
                                kForIterator[2]
                        ),
                        gf.limit(
                                stream3.iterator(),
                                kForIterator[3]
                        ),
                        gf.limit(
                                stream4.iterator(),
                                kForIterator[4]
                        )
                )
        );

        assertThat((long) topK.size(), is(k));
        assertThat(((TimedNamedOperation1) topK.get(0)).name(), anyOf(equalTo("0-1"), equalTo("1-1")));
        assertThat(((TimedNamedOperation1) topK.get(1)).name(), anyOf(equalTo("0-1"), equalTo("1-1")));
        assertThat(((TimedNamedOperation1) topK.get(0)).name(), not(equalTo(((TimedNamedOperation1) topK.get(1)).name())));
        assertThat(((TimedNamedOperation1) topK.get(2)).name(), anyOf(equalTo("0-2"), equalTo("2-1")));
        assertThat(((TimedNamedOperation1) topK.get(3)).name(), anyOf(equalTo("0-2"), equalTo("2-1")));
        assertThat(((TimedNamedOperation1) topK.get(2)).name(), not(equalTo(((TimedNamedOperation1) topK.get(3)).name())));
        assertThat(((TimedNamedOperation1) topK.get(4)).name(), anyOf(equalTo("0-3")));
        assertThat(((TimedNamedOperation1) topK.get(5)).name(), anyOf(equalTo("1-2"), equalTo("2-2")));
        assertThat(((TimedNamedOperation1) topK.get(6)).name(), anyOf(equalTo("1-2"), equalTo("2-2")));
        assertThat(((TimedNamedOperation1) topK.get(5)).name(), not(equalTo(((TimedNamedOperation1) topK.get(6)).name())));
        assertThat(((TimedNamedOperation1) topK.get(7)).name(), anyOf(equalTo("1-3"), equalTo("2-3")));
        assertThat(((TimedNamedOperation1) topK.get(8)).name(), anyOf(equalTo("1-3"), equalTo("2-3")));
        assertThat(((TimedNamedOperation1) topK.get(7)).name(), not(equalTo(((TimedNamedOperation1) topK.get(8)).name())));
        assertThat(((TimedNamedOperation1) topK.get(9)).name(), anyOf(equalTo("0-4")));
    }

    @Test
    public void shouldLimitStreamsCorrectlyWhenLimitIsHigherThanActualStreamsLength() throws WorkloadException {
        GeneratorFactory gf = new GeneratorFactory(new RandomDataGeneratorFactory(42l));

        List<Operation<?>> stream0 = Lists.<Operation<?>>newArrayList(
                new TimedNamedOperation1(0l, 0l, "0-1"),
                new TimedNamedOperation1(1l, 0l, "0-2"),
                new TimedNamedOperation1(2l, 0l, "0-3"),
                new TimedNamedOperation1(6l, 0l, "0-4")
        );

        List<Operation<?>> stream1 = Lists.<Operation<?>>newArrayList(
                new TimedNamedOperation1(0l, 0l, "1-1"),
                new TimedNamedOperation1(3l, 0l, "1-2"),
                new TimedNamedOperation1(4l, 0l, "1-3")
        );

        List<Operation<?>> stream2 = Lists.<Operation<?>>newArrayList(
                new TimedNamedOperation1(1l, 0l, "2-1"),
                new TimedNamedOperation1(3l, 0l, "2-2"),
                new TimedNamedOperation1(4l, 0l, "2-3")
        );

        List<Operation<?>> stream3 = Lists.newArrayList(
        );

        List<Operation<?>> stream4 = Lists.newArrayList(gf.limit(
                        new TimedNamedOperation1Factory(
                                gf.incrementing(10l, 1l),
                                gf.constant(0l),
                                gf.constant("4-x")
                        ),
                        1000000
                )
        );

        List<Iterator<Operation<?>>> streams = Lists.newArrayList(
                stream0.iterator(),
                stream1.iterator(),
                stream2.iterator(),
                stream3.iterator(),
                stream4.iterator()
        );

        long k = 10000;
        long[] kForIterator = WorkloadStreams.fromAmongAllRetrieveTopK(streams, k);

        List<Operation<?>> topK = Lists.newArrayList(
                gf.mergeSortOperationsByStartTime(
                        gf.limit(
                                stream0.iterator(),
                                kForIterator[0]
                        ),
                        gf.limit(
                                stream1.iterator(),
                                kForIterator[1]
                        ),
                        gf.limit(
                                stream2.iterator(),
                                kForIterator[2]
                        ),
                        gf.limit(
                                stream3.iterator(),
                                kForIterator[3]
                        ),
                        gf.limit(
                                stream4.iterator(),
                                kForIterator[4]
                        )
                )
        );

        assertThat((long) topK.size(), is(k));
        assertThat(((TimedNamedOperation1) topK.get(0)).name(), anyOf(equalTo("0-1"), equalTo("1-1")));
        assertThat(((TimedNamedOperation1) topK.get(1)).name(), anyOf(equalTo("0-1"), equalTo("1-1")));
        assertThat(((TimedNamedOperation1) topK.get(0)).name(), not(equalTo(((TimedNamedOperation1) topK.get(1)).name())));
        assertThat(((TimedNamedOperation1) topK.get(2)).name(), anyOf(equalTo("0-2"), equalTo("2-1")));
        assertThat(((TimedNamedOperation1) topK.get(3)).name(), anyOf(equalTo("0-2"), equalTo("2-1")));
        assertThat(((TimedNamedOperation1) topK.get(2)).name(), not(equalTo(((TimedNamedOperation1) topK.get(3)).name())));
        assertThat(((TimedNamedOperation1) topK.get(4)).name(), anyOf(equalTo("0-3")));
        assertThat(((TimedNamedOperation1) topK.get(5)).name(), anyOf(equalTo("1-2"), equalTo("2-2")));
        assertThat(((TimedNamedOperation1) topK.get(6)).name(), anyOf(equalTo("1-2"), equalTo("2-2")));
        assertThat(((TimedNamedOperation1) topK.get(5)).name(), not(equalTo(((TimedNamedOperation1) topK.get(6)).name())));
        assertThat(((TimedNamedOperation1) topK.get(7)).name(), anyOf(equalTo("1-3"), equalTo("2-3")));
        assertThat(((TimedNamedOperation1) topK.get(8)).name(), anyOf(equalTo("1-3"), equalTo("2-3")));
        assertThat(((TimedNamedOperation1) topK.get(7)).name(), not(equalTo(((TimedNamedOperation1) topK.get(8)).name())));
        assertThat(((TimedNamedOperation1) topK.get(9)).name(), anyOf(equalTo("0-4")));
    }

    private class TestWorkload extends Workload {

        @Override
        public void onInit(Map<String, String> params) throws WorkloadException {
        }

        @Override
        protected void onClose() throws IOException {
        }

        @Override
        protected WorkloadStreams getStreams(GeneratorFactory generators) throws WorkloadException {
            return getWorkloadStreams();
        }

        @Override
        public String serializeOperation(Operation<?> operation) throws SerializingMarshallingException {
            return null;
        }

        @Override
        public Operation<?> marshalOperation(String serializedOperation) throws SerializingMarshallingException {
            return null;
        }
    }

    private WorkloadStreams getWorkloadStreams() {
        GeneratorFactory gf = new GeneratorFactory(new RandomDataGeneratorFactory(42l));
        Iterator<Operation<?>> asyncDependencyStream = new TimedNamedOperation1Factory(
                gf.incrementing(0l, 10l),
                gf.incrementing(0l, 10l),
                gf.constant("ad")
        );
        Iterator<Operation<?>> asyncNonDependencyStream = new TimedNamedOperation1Factory(
                gf.incrementing(2l, 100l),
                gf.incrementing(2l, 100l),
                gf.constant("an")
        );
        Iterator<Operation<?>> blockingDependencyStream1 = new TimedNamedOperation2Factory(
                gf.incrementing(4l, 1000l),
                gf.incrementing(4l, 1000l),
                gf.constant("bd1")
        );
        Iterator<Operation<?>> blockingNonDependencyStream1 = new TimedNamedOperation2Factory(
                gf.incrementing(6l, 10000l),
                gf.incrementing(6l, 10000l),
                gf.constant("bn1")
        );
        Iterator<Operation<?>> blockingDependencyStream2 = new TimedNamedOperation3Factory(
                gf.incrementing(8l, 10000l),
                gf.incrementing(8l, 10000l),
                gf.constant("bd2")
        );
        Iterator<Operation<?>> blockingNonDependencyStream2 = new TimedNamedOperation3Factory(
                gf.incrementing(10l, 100000l),
                gf.incrementing(10l, 100000l),
                gf.constant("bn2")
        );
        WorkloadStreams workloadStreams = new WorkloadStreams();
        workloadStreams.setAsynchronousStream(
                new HashSet<Class<? extends Operation<?>>>(),
                asyncDependencyStream,
                asyncNonDependencyStream
        );
        workloadStreams.addBlockingStream(
                new HashSet<Class<? extends Operation<?>>>(),
                blockingDependencyStream1,
                blockingNonDependencyStream1
        );
        workloadStreams.addBlockingStream(
                new HashSet<Class<? extends Operation<?>>>(),
                blockingDependencyStream2,
                blockingNonDependencyStream2
        );
        return workloadStreams;
    }
}