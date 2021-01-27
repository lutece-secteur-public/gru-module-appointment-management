package fr.paris.lutece.plugins.appointment.modules.management.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.appointment.business.form.Form;
import fr.paris.lutece.plugins.appointment.business.form.FormHome;
import fr.paris.lutece.plugins.appointment.modules.management.business.search.AppointmentSearchItem;
import fr.paris.lutece.plugins.appointment.modules.management.service.AppointmentSearchService;
import fr.paris.lutece.plugins.appointment.modules.management.service.IAppointmentSearchService;
import fr.paris.lutece.plugins.appointment.modules.management.service.search.AppointmentSortConfig;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFilterDTO;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.util.LocalizedDelegatePaginator;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.AbstractPaginator;

@Controller( controllerJsp = "MultiviewAppointment.jsp", controllerPath = "jsp/admin/plugins/appointment/modules/management", right = "MULTIVIEW_APPOINTMENT" )
public class MultiviewAppointmentJspBean extends MVCAdminJspBean
{
    private static final long serialVersionUID = 2621411978305115179L;

    private static final String PROPERTY_DEFAULT_LIST_ITEM_PER_PAGE = "appointment-management.itemsPerPage";
    private static final String JSP_MANAGE_APPOINTMENT = "jsp/admin/plugins/appointment/modules/management/MultiviewAppointment.jsp";
    
    // Parameters
    private static final String PARAMETER_PAGE_INDEX = "page_index";
    private static final String PARAMETER_ORDER_BY = "orderBy";
    private static final String PARAMETER_ORDER_ASC = "orderAsc";
    private static final String PARAMETER_SEARCH = "Search";
    private static final String PARAMETER_RESET = "reset";
    
    // Views
    private static final String MULTIVIEW_APPOINTMENTS = "multiview_appointments";
    
    // Templates
    private static final String TEMPLATE_MULTIVIEW_APPOINTMENT = "admin/plugins/appointment/modules/management/multiview_appointments.html";
    
    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MULTIVIEW_APPOINTMENTS = "module.appointment.management.multiview.appointment.pageTitle";
    private static final String UNRESERVED = "appointment.message.labelStatusUnreserved";
    private static final String RESERVED = "appointment.message.labelStatusReserved";
    
    // Marks
    private static final String MARK_APPOINTMENT_LIST = "appointment_list";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_LIST_STATUS = "listStatus";
    private static final String MARK_LIST_FORMS = "listForms";
    private static final String MARK_FILTER = "filter";
    private static final String MARK_LANGUAGE = "language";
    
    // Variables
    private IAppointmentSearchService _appointmentSearchService = SpringContextService.getBean( AppointmentSearchService.BEAN_NAME );
    private String _strCurrentPageIndex;
    private int _nItemsPerPage;
    private AppointmentSortConfig _sortConfig;
    private AppointmentFilterDTO _filter;
    
    /**
     * Return the view with the responses of all the appointments
     * 
     * @param request
     *            The request on which to retrieve informations
     * @return the view associated to the responses value of all forms
     */
    @View( value = MULTIVIEW_APPOINTMENTS, defaultView = true )
    public String getMultiviewAppointments( HttpServletRequest request )
    { 
        initiatePaginatorProperties( request );
        
        // If it is a new search
        if ( request.getParameter( PARAMETER_SEARCH ) != null )
        {
            // Populate the filter
            populate( _filter, request );
        }
        else if ( request.getParameter( PARAMETER_RESET ) != null || _filter == null )
        {
            _filter = new AppointmentFilterDTO( );
        }
        
        List<AppointmentSearchItem> appointmentList = new ArrayList<>( );
        int nbResults = _appointmentSearchService.search( appointmentList, _filter, getIndexStart( ), _nItemsPerPage, _sortConfig );
        LocalizedDelegatePaginator<AppointmentSearchItem> paginator = new LocalizedDelegatePaginator<>( appointmentList, _nItemsPerPage, JSP_MANAGE_APPOINTMENT, PARAMETER_PAGE_INDEX, _strCurrentPageIndex, nbResults, getLocale( ) );

        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_NB_ITEMS_PER_PAGE, String.valueOf( _nItemsPerPage ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_APPOINTMENT_LIST, paginator.getPageItems( ) );
        model.put( MARK_LIST_STATUS, getListStatus( ) );
        model.put( MARK_FILTER, _filter );
        model.put( MARK_LANGUAGE, getLocale( ) );
        model.put( MARK_LIST_FORMS, getListForms( ) );
        
        return getPage( PROPERTY_PAGE_TITLE_MULTIVIEW_APPOINTMENTS, TEMPLATE_MULTIVIEW_APPOINTMENT, model );
    }
    
    private void initiatePaginatorProperties( HttpServletRequest request )
    {
        _sortConfig = null;
        _strCurrentPageIndex = AbstractPaginator.getPageIndex( request, AbstractPaginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        int nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_LIST_ITEM_PER_PAGE, 50 );
        _nItemsPerPage = AbstractPaginator.getItemsPerPage( request, AbstractPaginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, nDefaultItemsPerPage );

        String sortName = request.getParameter( PARAMETER_ORDER_BY );
        String sortOrder = request.getParameter( PARAMETER_ORDER_ASC );
        
        if ( StringUtils.isNotEmpty( sortName ) && StringUtils.isNotEmpty( sortOrder ) )
        {
            _sortConfig = new AppointmentSortConfig( sortName, Boolean.valueOf( sortOrder ) );
        }
    }
    
    /**
     * Return the current page index as int
     * 
     * @return the current page index
     */
    private int getCurrentPageIndex( )
    {
        if ( _strCurrentPageIndex != null )
        {
            return Integer.parseInt( _strCurrentPageIndex );
        }
        return 1;
    }

    /**
     * Get the index start
     * 
     * @return the started index
     */
    private int getIndexStart( )
    {
        return ( getCurrentPageIndex( ) - 1 ) * _nItemsPerPage;
    }
    
    private ReferenceList getListForms( )
    {
        ReferenceList refListForms = new ReferenceList( );
        refListForms.addItem( -1, StringUtils.EMPTY );
        
        List<Form> formList = FormHome.findAllForms( );
        for ( Form form : formList )
        {
            refListForms.addItem( form.getIdForm( ), form.getTitle( ) );
        }
        return refListForms;
    }
    /**
     * List of all the available status of an appointment
     * 
     * @return the list of the status
     */
    private ReferenceList getListStatus( )
    {
        ReferenceList refListStatus = new ReferenceList( );
        refListStatus.addItem( -1, StringUtils.EMPTY );
        refListStatus.addItem( 0, I18nService.getLocalizedString( RESERVED, getLocale( ) ) );
        refListStatus.addItem( 1, I18nService.getLocalizedString( UNRESERVED, getLocale( ) ) );
        return refListStatus;
    }
}
