/*
 * Copyright (c) 2002-2021, City of Paris
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
package fr.paris.lutece.plugins.appointment.modules.management.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.appointment.business.form.Form;
import fr.paris.lutece.plugins.appointment.business.form.FormHome;
import fr.paris.lutece.plugins.appointment.modules.management.business.search.AppointmentSearchItem;
import fr.paris.lutece.plugins.appointment.modules.management.service.AppointmentSearchService;
import fr.paris.lutece.plugins.appointment.modules.management.service.IAppointmentSearchService;
import fr.paris.lutece.plugins.appointment.modules.management.service.search.AppointmentSortConfig;
import fr.paris.lutece.plugins.appointment.service.AppointmentService;
import fr.paris.lutece.plugins.appointment.service.export.AppointmentExportService;
import fr.paris.lutece.plugins.appointment.service.export.ExcelAppointmentGenerator;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentDTO;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFilterDTO;
import fr.paris.lutece.plugins.filegenerator.service.TemporaryFileGeneratorService;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
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
    private static final String PARAMETER_SELECTED_DEFAULT_FIELD = "selectedDefaultFieldList";

    // Views
    private static final String MULTIVIEW_APPOINTMENTS = "multiview_appointments";

    // Actions
    private static final String ACTION_EXPORT_APPOINTMENTS = "doExportAppointments";

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
    private static final String MARK_DEFAULT_FIELD_LIST = "defaultFieldList";

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
        else
            if ( request.getParameter( PARAMETER_RESET ) != null || _filter == null )
            {
                _filter = new AppointmentFilterDTO( );
            }

        List<AppointmentSearchItem> appointmentList = new ArrayList<>( );
        int nbResults = _appointmentSearchService.search( appointmentList, _filter, getIndexStart( ), _nItemsPerPage, _sortConfig );
        LocalizedDelegatePaginator<AppointmentSearchItem> paginator = new LocalizedDelegatePaginator<>( appointmentList, _nItemsPerPage, JSP_MANAGE_APPOINTMENT,
                PARAMETER_PAGE_INDEX, _strCurrentPageIndex, nbResults, getLocale( ) );

        Map<String, Object> model = getModel( );
        model.put( MARK_NB_ITEMS_PER_PAGE, String.valueOf( _nItemsPerPage ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_APPOINTMENT_LIST, paginator.getPageItems( ) );
        model.put( MARK_LIST_STATUS, getListStatus( ) );
        model.put( MARK_FILTER, _filter );
        model.put( MARK_LANGUAGE, getLocale( ) );
        model.put( MARK_LIST_FORMS, getListForms( ) );
        model.put( MARK_DEFAULT_FIELD_LIST, AppointmentExportService.getDefaultColumnList( getLocale( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_MULTIVIEW_APPOINTMENTS, TEMPLATE_MULTIVIEW_APPOINTMENT, model );
    }

    /**
     * Do download a file from an appointment response
     * 
     * @param request
     *            The request
     * @param response
     *            The response
     * @return nothing.
     * @throws AccessDeniedException
     *             If the user is not authorized to access this feature
     */
    @Action( ACTION_EXPORT_APPOINTMENTS )
    public String doExportAppointments( HttpServletRequest request ) throws AccessDeniedException
    {
        Locale locale = getLocale( );
        List<AppointmentDTO> listAppointmentsDTO = new ArrayList<>( );
        if ( _filter != null )
        {
            listAppointmentsDTO = AppointmentService.findListAppointmentsDTOByFilter( _filter );
        }

        List<String> defaultColumnList = new ArrayList<>( );
        if ( ArrayUtils.isNotEmpty( request.getParameterValues( PARAMETER_SELECTED_DEFAULT_FIELD ) ) )
        {
            defaultColumnList = Arrays.asList( request.getParameterValues( PARAMETER_SELECTED_DEFAULT_FIELD ) );
        }

        ExcelAppointmentGenerator generator = new ExcelAppointmentGenerator( defaultColumnList, locale, listAppointmentsDTO, new ArrayList<>( ) );

        TemporaryFileGeneratorService.getInstance( ).generateFile( generator, getUser( ) );
        addInfo( "appointment.export.async.message", locale );

        return getMultiviewAppointments( request );
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
