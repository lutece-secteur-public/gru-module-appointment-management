/*
 * Copyright (c) 2002-2022, City of Paris
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
package fr.paris.lutece.plugins.appointment.modules.management.service.indexer;

import java.io.IOException;
import java.nio.file.Paths;

import javax.annotation.PreDestroy;
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
     * Directory backing the current IndexWriter. Kept as a field because IndexWriter.close( ) does not close the Directory it was given : without this
     * reference the underlying handle would leak on every writer re-opening.
     */
    private Directory _directory;

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
                closeDirectory( );
                _directory = getDirectory( );

                Directory luceneDirectory = _directory;

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
     * Release the IndexWriter and its Directory when the Spring context is shut down.
     *
     * The IndexWriter is deliberately kept open for the whole life of the webapp, but it holds a native lock on the index directory ( write.lock ). That lock is
     * registered in the JVM wide lock table, not in the web application : if it is not released when the context is destroyed, a hot redeployment leaves the
     * stale lock behind and the new context fails with OverlappingFileLockException as soon as it tries to index.
     *
     * Invoked by Spring through AppInitListener.contextDestroyed -&gt; SpringContextService.shutdown( ).
     */
    @PreDestroy
    public void close( )
    {
        if ( _indexWriter != null )
        {
            try
            {
                if ( _indexWriter.isOpen( ) )
                {
                    _indexWriter.close( );
                }
            }
            catch( IOException e )
            {
                AppLogService.error( "Unable to close the Lucene Index Writer", e );
            }
            finally
            {
                _indexWriter = null;
            }
        }
        closeDirectory( );
    }

    /**
     * Close the current Directory, if any, and forget it.
     */
    private void closeDirectory( )
    {
        if ( _directory != null )
        {
            try
            {
                _directory.close( );
            }
            catch( IOException e )
            {
                AppLogService.error( "Unable to close the Lucene Directory", e );
            }
            finally
            {
                _directory = null;
            }
        }
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
