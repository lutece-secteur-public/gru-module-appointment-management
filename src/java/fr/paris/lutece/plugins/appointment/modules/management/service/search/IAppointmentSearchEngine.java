package fr.paris.lutece.plugins.appointment.modules.management.service.search;

import java.util.List;

import fr.paris.lutece.plugins.appointment.modules.management.business.search.AppointmentSearchItem;

public interface IAppointmentSearchEngine
{

    /**
     * Search lucene index for results.
     * @return total number of results.
     */
    int getSearchResult( List<AppointmentSearchItem> result, int nStartIndex, int nPageSize, AppointmentSortConfig sortConfig );
}
