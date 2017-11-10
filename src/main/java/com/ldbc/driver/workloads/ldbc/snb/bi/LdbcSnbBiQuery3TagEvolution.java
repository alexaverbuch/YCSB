package com.ldbc.driver.workloads.ldbc.snb.bi;

import com.ldbc.driver.Operation;
import com.ldbc.driver.SerializingMarshallingException;

import java.util.ArrayList;
import java.util.List;

public class LdbcSnbBiQuery3TagEvolution extends Operation<List<LdbcSnbBiQuery3TagEvolutionResult>>
{
    public static final int TYPE = 3;
    public static final int DEFAULT_LIMIT = 100;
    private final int year;
    private final int month;
    private final int limit;

    public LdbcSnbBiQuery3TagEvolution( int year, int month, int limit )
    {
        this.year = year;
        this.month = month;
        this.limit = limit;
    }

    public int year()
    {
        return year;
    }

    public int month()
    {
        return month;
    }

    public int limit()
    {
        return limit;
    }

    @Override
    public String toString()
    {
        return "LdbcSnbBiQuery3TagEvolution{" +
               "year=" + year +
               ", month=" + month +
               '}';
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        { return true; }
        if ( o == null || getClass() != o.getClass() )
        { return false; }

        LdbcSnbBiQuery3TagEvolution that = (LdbcSnbBiQuery3TagEvolution) o;

        if ( year != that.year )
        { return false; }
        if ( month != that.month )
        { return false; }
        return limit == that.limit;
    }

    @Override
    public int hashCode()
    {
        int result = year;
        result = 31 * result + month;
        result = 31 * result + limit;
        return result;
    }

    @Override
    public List<LdbcSnbBiQuery3TagEvolutionResult> marshalResult( String serializedResults )
            throws SerializingMarshallingException
    {
        List<List<Object>> resultsAsList = SerializationUtil.marshalListOfLists( serializedResults );
        List<LdbcSnbBiQuery3TagEvolutionResult> result = new ArrayList<>();
        for ( int i = 0; i < resultsAsList.size(); i++ )
        {
            List<Object> row = resultsAsList.get( i );
            String tag = (String) row.get( 0 );
            int countA = ((Number) row.get( 1 )).intValue();
            int countB = ((Number) row.get( 2 )).intValue();
            int difference = ((Number) row.get( 3 )).intValue();
            result.add(
                    new LdbcSnbBiQuery3TagEvolutionResult(
                            tag,
                            countA,
                            countB,
                            difference
                    )
            );
        }

        return result;
    }

    @Override
    public String serializeResult( Object resultsObject ) throws SerializingMarshallingException
    {
        List<LdbcSnbBiQuery3TagEvolutionResult> result = (List<LdbcSnbBiQuery3TagEvolutionResult>) resultsObject;
        List<List<Object>> resultsFields = new ArrayList<>();
        for ( int i = 0; i < result.size(); i++ )
        {
            LdbcSnbBiQuery3TagEvolutionResult row = result.get( i );
            List<Object> resultFields = new ArrayList<>();
            resultFields.add( row.tag() );
            resultFields.add( row.countA() );
            resultFields.add( row.countB() );
            resultFields.add( row.difference() );
            resultsFields.add( resultFields );
        }
        return SerializationUtil.toJson( resultsFields );
    }

    @Override
    public int type()
    {
        return TYPE;
    }
}
