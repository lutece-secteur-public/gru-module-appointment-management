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
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFilterDTO;
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
    public int search( List<AppointmentSearchItem> results, AppointmentFilterDTO filter, int nStartIndex, int nPageSize, AppointmentSortConfig sortConfig )
    {
        int nbResults = _searchEngine.getSearchResult( results, filter, nStartIndex, nPageSize, sortConfig );
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
