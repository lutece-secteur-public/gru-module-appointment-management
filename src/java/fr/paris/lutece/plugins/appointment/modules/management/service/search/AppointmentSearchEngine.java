package fr.paris.lutece.plugins.appointment.modules.management.service.search;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import fr.paris.lutece.plugins.appointment.modules.management.business.search.AppointmentSearchItem;
import fr.paris.lutece.plugins.appointment.modules.management.service.indexer.LuceneAppointmentIndexFactory;
import fr.paris.lutece.portal.service.search.LuceneSearchEngine;
import fr.paris.lutece.portal.service.util.AppLogService;

public class AppointmentSearchEngine implements IAppointmentSearchEngine
{

    @Inject
    private LuceneAppointmentIndexFactory _indexFactory;

    @Override
    public int getSearchResult( List<AppointmentSearchItem> result, int nStartIndex, int nPageSize, AppointmentSortConfig sortConfig )
    {
        int nbResults = 0;
        Query query = new MatchAllDocsQuery( );
        
        try ( Directory directory = _indexFactory.getDirectory( ) ; IndexReader ir = DirectoryReader.open( directory ) ; )
        {
            Sort sort = buildLuceneSort( sortConfig );
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
                Document document = searcher.doc( hits[i].doc );
                result.add( new AppointmentSearchItem( document ) );
            }
        }
        catch( IOException e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        return nbResults;
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
