package fr.paris.lutece.plugins.appointment.modules.management.service.indexer;

import java.io.IOException;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class LuceneAppointmentIndexFactory
{
    // Constants
    private static final String PATH_INDEX = "appointment-management.internalIndexer.lucene.indexPath";
    private static final String PATH_INDEX_IN_WEBAPP = "appointment-management.internalIndexer.lucene.indexInWebapp";
    
    @Inject
    @Named( value = "appointment-management.luceneAnalizer" )
    private Analyzer _analyzer;
    
    private IndexWriter _indexWriter;
    
    /**
     * Create the IndexWriter with its configuration
     * 
     * @param bCreateIndex
     *            The boolean which tell if the index must be created
     * @return the created IndexWriter
     * @throws IOException
     *             - if there is a low level IO error
     */
    public IndexWriter getIndexWriter( Boolean bCreateIndex )
    {
        if ( _indexWriter == null || !_indexWriter.isOpen( ) )
        {
            try
            {
                Directory luceneDirectory = getDirectory( );

                if ( !DirectoryReader.indexExists( luceneDirectory ) )
                {
                    bCreateIndex = Boolean.TRUE;
                }

                IndexWriterConfig conf = new IndexWriterConfig( _analyzer );

                if ( Boolean.TRUE.equals( bCreateIndex ) )
                {
                    conf.setOpenMode( OpenMode.CREATE );
                }
                else
                {
                    conf.setOpenMode( OpenMode.APPEND );
                }
                _indexWriter = new IndexWriter( luceneDirectory, conf );
            }
            catch( IOException e )
            {
                AppLogService.error( "Unable to create a new Lucene Index Writer", e );
                return null;
            }
        }
        return _indexWriter;
    }
    
    /**
     * Return the Directory to use for the search
     * 
     * @return the Directory to use for the search
     * @throws IOException
     *             - if the path string cannot be converted to a Path
     */
    public Directory getDirectory( ) throws IOException
    {
        String strIndex;

        boolean indexInWebapp = AppPropertiesService.getPropertyBoolean( PATH_INDEX_IN_WEBAPP, true );
        if ( indexInWebapp )
        {
            strIndex = AppPathService.getPath( PATH_INDEX );
        }
        else
        {
            strIndex = AppPropertiesService.getProperty( PATH_INDEX );
        }

        return FSDirectory.open( Paths.get( strIndex ) );
    }
}
