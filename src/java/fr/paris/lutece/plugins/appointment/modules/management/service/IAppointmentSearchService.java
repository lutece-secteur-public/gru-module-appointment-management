package fr.paris.lutece.plugins.appointment.modules.management.service;

import java.util.List;

import fr.paris.lutece.plugins.appointment.modules.management.business.search.AppointmentSearchItem;
import fr.paris.lutece.plugins.appointment.modules.management.service.search.AppointmentSortConfig;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFilterDTO;

public interface IAppointmentSearchService
{

    int search( List<AppointmentSearchItem> result, AppointmentFilterDTO filter, int nStartIndex, int nPageSize, AppointmentSortConfig sortConfig );
}
