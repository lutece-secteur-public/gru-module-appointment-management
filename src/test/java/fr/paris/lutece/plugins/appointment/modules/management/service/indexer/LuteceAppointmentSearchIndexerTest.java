/*
 * Copyright (c) 2002-2026, City of Paris
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
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import fr.paris.lutece.plugins.appointment.modules.management.business.search.AppointmentSearchItem;
import fr.paris.lutece.portal.business.indexeraction.IndexerAction;
import fr.paris.lutece.portal.business.indexeraction.IndexerActionHome;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * Tests of the appointment indexer when the index or the appointments change under it
 */
public class LuteceAppointmentSearchIndexerTest extends LuteceTestCase
{
    private static final long INDEXING_TIMEOUT = 10000L;

    @TempDir
    Path _indexPath;
    private TestIndexFactory _factory;
    private LuteceAppointmentSearchIndexer _indexer;

    /**
     * Index factory writing in a temporary directory
     */
    private static class TestIndexFactory extends LuceneAppointmentIndexFactory
    {
        private final Path _path;

        TestIndexFactory( Path path )
        {
            _path = path;
        }

        @Override
        public Directory getDirectory( ) throws IOException
        {
            return FSDirectory.open( _path );
        }
    }

    @BeforeEach
    public void setUpIndexer( ) throws Exception
    {
        _factory = new TestIndexFactory( _indexPath );
        _indexer = new LuteceAppointmentSearchIndexer( );
        Field field = LuteceAppointmentSearchIndexer.class.getDeclaredField( "_luceneAppointmentIndexFactory" );
        field.setAccessible( true );
        field.set( _indexer, _factory );
        waitEndOfIndexing( );
    }

    @AfterEach
    public void tearDownIndexer( ) throws Exception
    {
        waitEndOfIndexing( );
        IndexWriter indexWriter = _factory.getIndexWriter( false );
        if ( indexWriter != null )
        {
            indexWriter.rollback( );
        }
    }

    /**
     * The index write lock is replaced while the writer is open (an index directory copied or restored at startup): the
     * failed indexing must not leave the indexer marked as running, and the next indexing must reopen the writer.
     */
    @Test
    public void testIndexingAfterWriteLockReplaced( ) throws Exception
    {
        IndexWriter brokenWriter = _factory.getIndexWriter( true );
        brokenWriter.commit( );
        Path lock = _indexPath.resolve( IndexWriter.WRITE_LOCK_NAME );
        Files.delete( lock );
        Thread.sleep( 50 );
        Files.createFile( lock );

        try
        {
            _indexer.indexDocuments( );
        }
        catch( RuntimeException e )
        {
            // the first indexing may fail, the indexer must recover from it
        }
        waitEndOfIndexing( );
        assertFalse( isIndexRunning( ), "The indexer is still marked as running after a failed indexing" );

        _indexer.indexDocuments( );
        waitEndOfIndexing( );

        IndexWriter indexWriter = _factory.getIndexWriter( false );
        assertNotSame( brokenWriter, indexWriter, "The index writer whose lock was replaced is still used" );
        assertTrue( indexWriter.isOpen( ) );
        try ( Directory directory = FSDirectory.open( _indexPath ) )
        {
            assertTrue( DirectoryReader.indexExists( directory ) );
        }
    }

    /**
     * An indexer action refers to an appointment deleted since: there is no document to index for it
     */
    @Test
    public void testGetDocumentsOfDeletedAppointment( ) throws Exception
    {
        List<Document> listDocument = _indexer.getDocuments( String.valueOf( Integer.MAX_VALUE ) );

        assertNotNull( listDocument );
        assertTrue( listDocument.isEmpty( ) );
    }

    /**
     * The multiview indexer leaves the pending actions of the other indexers of the site to the core indexing
     */
    @Test
    public void testActionOfAnotherIndexerIsLeft( ) throws Exception
    {
        IndexerAction other = new IndexerAction( );
        other.setIdDocument( "424242" );
        other.setIdTask( IndexerAction.TASK_MODIFY );
        other.setIndexerName( "OtherIndexer" );
        IndexerActionHome.create( other );
        try
        {
            _indexer.indexDocument( Integer.MAX_VALUE, IndexerAction.TASK_DELETE );
            waitEndOfIndexing( );

            assertNotNull( IndexerActionHome.findByPrimaryKey( other.getIdAction( ) ), "The action of another indexer was removed" );
        }
        finally
        {
            IndexerActionHome.remove( other.getIdAction( ) );
        }
    }

    /**
     * The core incremental indexing processes and removes every row of core_indexer_action: when it runs between the
     * queueing of an appointment and the multiview indexing, the multiview index must still be updated
     */
    @Test
    public void testCoreIndexingDoesNotTakeTheMultiviewAction( ) throws Exception
    {
        int nIdAppointment = 987654;
        Document doc = new Document( );
        doc.add( new IntPoint( AppointmentSearchItem.FIELD_ID_APPOINTMENT, nIdAppointment ) );
        IndexWriter writer = _factory.getIndexWriter( false );
        writer.addDocument( doc );
        writer.commit( );
        assertEquals( 1, countDocuments( nIdAppointment ) );

        // the multiview indexing waits for the lock while the core indexing consumes the shared table
        synchronized ( getLock( ) )
        {
            _indexer.indexDocument( nIdAppointment, IndexerAction.TASK_DELETE );
            for ( IndexerAction action : IndexerActionHome.getList( ) )
            {
                IndexerActionHome.remove( action.getIdAction( ) );
            }
        }
        waitEndOfIndexing( );

        assertEquals( 0, countDocuments( nIdAppointment ), "The removal of the appointment did not reach the multiview index" );
    }

    private int countDocuments( int nIdAppointment ) throws IOException
    {
        try ( Directory directory = FSDirectory.open( _indexPath ); DirectoryReader reader = DirectoryReader.open( directory ) )
        {
            return new IndexSearcher( reader ).count( IntPoint.newExactQuery( AppointmentSearchItem.FIELD_ID_APPOINTMENT, nIdAppointment ) );
        }
    }

    private static Object getLock( ) throws ReflectiveOperationException
    {
        Field field = LuteceAppointmentSearchIndexer.class.getDeclaredField( "LOCK" );
        field.setAccessible( true );
        return field.get( null );
    }

    private static AtomicBoolean getIndexRunningFlag( ) throws ReflectiveOperationException
    {
        Field field = LuteceAppointmentSearchIndexer.class.getDeclaredField( "_bIndexIsRunning" );
        field.setAccessible( true );
        return (AtomicBoolean) field.get( null );
    }

    private static boolean isIndexRunning( ) throws ReflectiveOperationException
    {
        return getIndexRunningFlag( ).get( );
    }

    /**
     * Wait for the end of the background indexing, a stuck flag being left as it is for the assertions
     */
    private static void waitEndOfIndexing( ) throws Exception
    {
        long lEnd = System.currentTimeMillis( ) + INDEXING_TIMEOUT;
        while ( isIndexRunning( ) && System.currentTimeMillis( ) < lEnd )
        {
            Thread.sleep( 50 );
        }
    }
}
