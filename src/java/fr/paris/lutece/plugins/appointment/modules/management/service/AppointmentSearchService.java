package fr.paris.lutece.plugins.appointment.modules.management.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;

import fr.paris.lutece.plugins.appointment.business.form.Form;
import fr.paris.lutece.plugins.appointment.business.form.FormHome;
import fr.paris.lutece.plugins.appointment.modules.management.business.search.AppointmentSearchItem;
import fr.paris.lutece.plugins.appointment.modules.management.service.search.AppointmentSearchEngine;
import fr.paris.lutece.plugins.appointment.modules.management.service.search.AppointmentSortConfig;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.business.state.StateFilter;
import fr.paris.lutece.plugins.workflowcore.service.state.StateService;

public class AppointmentSearchService implements IAppointmentSearchService
{
    public static final String BEAN_NAME = "appointment-management.appointmentSearchService";
    
    @Inject
    private AppointmentSearchEngine _searchEngine;
    
    @Autowired( required = false )
    private StateService _stateService;
    
    @Override
    public int search( List<AppointmentSearchItem> results, int nStartIndex, int nPageSize, AppointmentSortConfig sortConfig )
    {
        int nbResults = _searchEngine.getSearchResult( results, nStartIndex, nPageSize, sortConfig );
        Map<Integer, Form> mapForms = FormHome.findAllForms( ).stream( ).collect( Collectors.toMap( Form::getIdForm, Function.identity( ) ) );
        Map<Integer, State> mapState = new HashMap<>( );
        if ( _stateService != null )
        {
           List<State> stateList = _stateService.getListStateByFilter( new StateFilter( ) );
           mapState = stateList.stream( ).collect( Collectors.toMap( State::getId, Function.identity( ) ) );
        }
        
        for ( AppointmentSearchItem item : results )
        {
            Form form = mapForms.get( item.getIdForm( ) );
            State state = mapState.get( item.getIdState( ) );
            
            if ( form != null )
            {
                item.setFormTitle( form.getTitle( ) );
            }
            if ( state != null )
            {
                item.setStateTitle( state.getName( ) );
            }
        }
        return nbResults;
    }
}
