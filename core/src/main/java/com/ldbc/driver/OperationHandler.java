package com.ldbc.driver;

import java.util.concurrent.Callable;

import com.ldbc.driver.util.Duration;
import com.ldbc.driver.util.Time;

public abstract class OperationHandler<A extends Operation<?>> implements Callable<OperationResult>
{
    private A operation;
    private DbConnectionState dbConnectionState;

    public final void setOperation( Operation<?> operation )
    {
        this.operation = (A) operation;
    }

    public final void setDbConnectionState( DbConnectionState dbConnectionState )
    {
        this.dbConnectionState = dbConnectionState;
    }

    public final DbConnectionState getDbConnectionState()
    {
        return dbConnectionState;
    }

    @Override
    public OperationResult call() throws Exception
    {
        Time actualStartTime = Time.now();
        OperationResult operationResult = executeOperation( operation );
        Time actualEndTime = Time.now();

        operationResult.setOperationType( operation.getClass().getName() );
        operationResult.setScheduledStartTime( operation.getScheduledStartTime() );
        operationResult.setActualStartTime( actualStartTime );
        operationResult.setRunTime( Duration.durationBetween( actualStartTime, actualEndTime ) );

        return operationResult;
    }

    protected abstract OperationResult executeOperation( A operation ) throws DbException;

    @Override
    public String toString()
    {
        return String.format( "OperationHandler [type=%s, operation=%s]", getClass().getName(), operation );

    }
}
