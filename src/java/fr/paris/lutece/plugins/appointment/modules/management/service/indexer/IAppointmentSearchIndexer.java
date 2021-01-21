package fr.paris.lutece.plugins.appointment.modules.management.service.indexer;

import fr.paris.lutece.portal.service.search.SearchIndexer;

/**
 * IAppointmentSearchIndexer
 */
public interface IAppointmentSearchIndexer extends SearchIndexer
{

    /**
     * Directly index one document
     * 
     * @param nIdAppointment
     * @param task
     */
    void indexDocument( int nIdAppointment, int idTask );
    
}
