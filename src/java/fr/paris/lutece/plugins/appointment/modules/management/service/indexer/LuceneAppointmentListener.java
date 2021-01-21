package fr.paris.lutece.plugins.appointment.modules.management.service.indexer;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import fr.paris.lutece.plugins.appointment.service.listeners.IAppointmentListener;
import fr.paris.lutece.portal.business.indexeraction.IndexerAction;

public class LuceneAppointmentListener implements IAppointmentListener
{

    @Inject
    private IAppointmentSearchIndexer _indexer;
    
    @Override
    public void notifyAppointmentRemoval( int nIdAppointment )
    {
        _indexer.indexDocument( nIdAppointment, IndexerAction.TASK_DELETE );
    }

    @Override
    public String appointmentDateChanged( int nIdAppointment, List<Integer> listIdSlot, Locale locale )
    {
        _indexer.indexDocument( nIdAppointment, IndexerAction.TASK_MODIFY);
        return null;
    }

    @Override
    public void notifyAppointmentCreated( int nIdAppointment )
    {
        _indexer.indexDocument( nIdAppointment, IndexerAction.TASK_CREATE );
    }

    @Override
    public void notifyAppointmentUpdated( int nIdAppointment )
    {
        _indexer.indexDocument( nIdAppointment, IndexerAction.TASK_MODIFY);
    }
}
