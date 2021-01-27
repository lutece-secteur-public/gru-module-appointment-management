package fr.paris.lutece.plugins.appointment.modules.management.service.search;

import java.util.List;

import fr.paris.lutece.plugins.appointment.modules.management.business.search.AppointmentSearchItem;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFilterDTO;

public interface IAppointmentSearchEngine
{

    /**
     * Search lucene index for results.
     * @return total number of results.
     */
    int getSearchResult( List<AppointmentSearchItem> result, AppointmentFilterDTO filter, int nStartIndex, int nPageSize, AppointmentSortConfig sortConfig );
}
