/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.appointment.modules.management.service.search;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import fr.paris.lutece.plugins.appointment.modules.management.business.search.AppointmentSearchItem;
import fr.paris.lutece.plugins.appointment.modules.management.service.indexer.LuceneAppointmentIndexFactory;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFilterDTO;
import fr.paris.lutece.portal.service.search.LuceneSearchEngine;
import fr.paris.lutece.portal.service.util.AppLogService;

public class AppointmentSearchEngine implements IAppointmentSearchEngine
{

    @Inject
    private LuceneAppointmentIndexFactory _indexFactory;

    @Override
    public int getSearchResult( List<AppointmentSearchItem> result, AppointmentFilterDTO filter, int nStartIndex, int nPageSize,
            AppointmentSortConfig sortConfig )
    {
        int nbResults = 0;
        Query query = createQuery( filter );
        Sort sort = buildLuceneSort( sortConfig );

        try ( Directory directory = _indexFactory.getDirectory( ) ; IndexReader ir = DirectoryReader.open( directory ) ; )
        {
            IndexSearcher searcher = new IndexSearcher( ir );
            TopDocs topDocs = null;
            // Get results documents
            if ( sort != null )
            {
                topDocs = searcher.search( query, LuceneSearchEngine.MAX_RESPONSES, sort );
            }
            else
            {
                topDocs = searcher.search( query, LuceneSearchEngine.MAX_RESPONSES );
            }

            ScoreDoc [ ] hits = topDocs.scoreDocs;
            nbResults = hits.length;
            int nMaxIndex = hits.length;
            if ( nPageSize > 0 )
            {
                nMaxIndex = Math.min( nStartIndex + nPageSize, hits.length );
            }

            for ( int i = nStartIndex; i < nMaxIndex; i++ )
            {
                Document document = searcher.doc( hits [i].doc );
                result.add( new AppointmentSearchItem( document ) );
            }
        }
        catch( IOException e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        return nbResults;
    }

    private Query createQuery( AppointmentFilterDTO filter )
    {
        BooleanQuery.Builder builder = new BooleanQuery.Builder( );
        if ( filter.getIdForm( ) > 0 )
        {
            Query query = IntPoint.newExactQuery( AppointmentSearchItem.FIELD_ID_FORM, filter.getIdForm( ) );
            builder.add( query, BooleanClause.Occur.MUST );
        }
        if ( StringUtils.isNotEmpty( filter.getFirstName( ) ) )
        {
            Query query = new TermQuery( new Term( AppointmentSearchItem.FIELD_FIRST_NAME, filter.getFirstName( ) ) );
            builder.add( query, BooleanClause.Occur.MUST );
        }
        if ( StringUtils.isNotEmpty( filter.getLastName( ) ) )
        {
            Query query = new TermQuery( new Term( AppointmentSearchItem.FIELD_LAST_NAME, filter.getLastName( ) ) );
            builder.add( query, BooleanClause.Occur.MUST );
        }
        if ( StringUtils.isNotEmpty( filter.getEmail( ) ) )
        {
            Query query = new TermQuery( new Term( AppointmentSearchItem.FIELD_MAIL, filter.getEmail( ) ) );
            builder.add( query, BooleanClause.Occur.MUST );
        }
        builder.add( createDateRangeQuery( filter ), BooleanClause.Occur.MUST );
        if ( filter.getStatus( ) != -1 )
        {
            Query query = new TermQuery( new Term( AppointmentSearchItem.FIELD_CANCELLED, String.valueOf( filter.getStatus( ) == 1 ) ) );
            builder.add( query, BooleanClause.Occur.MUST );
        }

        return builder.build( );
    }

    private Query createDateRangeQuery( AppointmentFilterDTO filter )
    {
        Query query = null;
        Timestamp startingTimestamp = null;
        if ( filter.getStartingDateOfSearch( ) != null )
        {
            LocalDate startingDate = filter.getStartingDateOfSearch( ).toLocalDate( );
            if ( StringUtils.isNotEmpty( filter.getStartingTimeOfSearch( ) ) )
            {
                startingTimestamp = Timestamp.valueOf( startingDate.atTime( LocalTime.parse( filter.getStartingTimeOfSearch( ) ) ) );
            }
            else
            {
                startingTimestamp = Timestamp.valueOf( startingDate.atStartOfDay( ) );
            }
        }
        Timestamp endingTimestamp = null;
        if ( filter.getEndingDateOfSearch( ) != null )
        {
            LocalDate startingDate = filter.getEndingDateOfSearch( ).toLocalDate( );
            if ( StringUtils.isNotEmpty( filter.getEndingTimeOfSearch( ) ) )
            {
                endingTimestamp = Timestamp.valueOf( startingDate.atTime( LocalTime.parse( filter.getEndingTimeOfSearch( ) ) ) );
            }
            else
            {
                endingTimestamp = Timestamp.valueOf( startingDate.atTime( LocalTime.MAX ) );
            }
        }
        if ( startingTimestamp != null && endingTimestamp != null )
        {
            query = LongPoint.newRangeQuery( AppointmentSearchItem.FIELD_START_DATE, startingTimestamp.getTime( ), endingTimestamp.getTime( ) );
        }
        else
            if ( startingTimestamp != null )
            {
                query = LongPoint.newRangeQuery( AppointmentSearchItem.FIELD_START_DATE, startingTimestamp.getTime( ), Long.MAX_VALUE );
            }
            else
                if ( endingTimestamp != null )
                {
                    query = LongPoint.newRangeQuery( AppointmentSearchItem.FIELD_START_DATE, Long.MIN_VALUE, endingTimestamp.getTime( ) );
                }
                else
                {
                    query = LongPoint.newRangeQuery( AppointmentSearchItem.FIELD_START_DATE, Long.MIN_VALUE, Long.MAX_VALUE );
                }
        return query;
    }

    /**
     * Build the Lucene Sort obj
     * 
     * @param sortConfig
     *            The sort config
     * @return the Lucene Sort obj
     */
    private Sort buildLuceneSort( AppointmentSortConfig sortConfig )
    {
        if ( sortConfig != null )
        {
            String strAttributeName = sortConfig.getSortAttributeName( );
            if ( strAttributeName != null )
            {
                if ( strAttributeName.endsWith( AppointmentSearchItem.FIELD_DATE_SUFFIX ) )
                {
                    return new Sort( new SortedNumericSortField( sortConfig.getSortAttributeName( ), SortField.Type.LONG, sortConfig.isAscSort( ) ) );
                }
                if ( strAttributeName.endsWith( AppointmentSearchItem.FIELD_INT_SUFFIX ) )
                {
                    return new Sort( new SortedNumericSortField( sortConfig.getSortAttributeName( ), SortField.Type.LONG, sortConfig.isAscSort( ) ) );

                }
                return new Sort( new SortField( sortConfig.getSortAttributeName( ), SortField.Type.STRING, sortConfig.isAscSort( ) ) );
            }
        }

        return null;
    }
}
