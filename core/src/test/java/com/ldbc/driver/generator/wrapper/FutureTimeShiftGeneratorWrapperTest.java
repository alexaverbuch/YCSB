package com.ldbc.driver.generator.wrapper;

import org.junit.Test;

import com.ldbc.driver.Operation;
import com.ldbc.driver.generator.Generator;
import com.ldbc.driver.generator.GeneratorException;
import com.ldbc.driver.generator.IdentityGenerator;
import com.ldbc.driver.util.Function;
import com.ldbc.driver.util.time.Time;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;

public class FutureTimeShiftGeneratorWrapperTest
{
    @Test
    public void shouldShiftTimeForward()
    {
        // Given
        Generator<Long> timeInNanoGenerator = new IdentityGenerator<Long>( 10l, 20l, 30l, 40l, 50l );

        Function<Long, Time> timeFromNanoFun = new Function<Long, Time>()
        {
            @Override
            public Time apply( Long from )
            {
                return Time.fromNano( from );
            }
        };
        Generator<Time> timeGenerator = new MapGeneratorWrapper<Long, Time>( timeInNanoGenerator, timeFromNanoFun );

        Generator<Operation<?>> operationGenerator = new StartTimeOperationGeneratorWrapper( timeGenerator,
                new OperationGenerator() );

        // When
        Generator<Operation<?>> shiftedOperationGenerator = new FutureTimeShiftGeneratorWrapper( operationGenerator,
                Time.fromNano( 60l ) );

        // Then
        assertThat( shiftedOperationGenerator.next().getScheduledStartTime(), is( Time.fromNano( 60l ) ) );
        assertThat( shiftedOperationGenerator.next().getScheduledStartTime(), is( Time.fromNano( 70l ) ) );
        assertThat( shiftedOperationGenerator.next().getScheduledStartTime(), is( Time.fromNano( 80l ) ) );
        assertThat( shiftedOperationGenerator.next().getScheduledStartTime(), is( Time.fromNano( 90l ) ) );
        assertThat( shiftedOperationGenerator.next().getScheduledStartTime(), is( Time.fromNano( 100l ) ) );
        assertThat( shiftedOperationGenerator.hasNext(), is( false ) );
    }

    static class OperationGenerator extends Generator<Operation<?>>
    {
        protected OperationGenerator()
        {
            super( null );
        }

        @Override
        protected Operation<?> doNext() throws GeneratorException
        {
            return new Operation<Object>()
            {
            };
        }
    }
}