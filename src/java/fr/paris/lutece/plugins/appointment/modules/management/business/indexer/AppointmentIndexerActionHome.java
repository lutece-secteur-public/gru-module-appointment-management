/*
 * Copyright (c) 2002-2022, City of Paris
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
 */package fr.paris.lutece.plugins.appointment.modules.management.business.indexer;

import java.util.List;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import jakarta.enterprise.inject.spi.CDI;

/**
 * Queue of the multiview indexer. It is the module's own: the core search indexing processes and removes every row of
 * core_indexer_action, so a change queued there could be consumed before the multiview indexer reads it.
 */
public final class AppointmentIndexerActionHome
{
    private static final String PLUGIN_NAME = "appointment-management";
    private static IAppointmentIndexerActionDAO _dao = CDI.current( ).select( IAppointmentIndexerActionDAO.class ).get( );
    private static Plugin _plugin = PluginService.getPlugin( PLUGIN_NAME );

    private AppointmentIndexerActionHome( )
    {
    }

    /**
     * Queue a change of an appointment
     *
     * @param nIdAppointment
     *            the id of the appointment
     * @param nIdTask
     *            the task (IndexerAction.TASK_CREATE, TASK_MODIFY or TASK_DELETE)
     * @return the queued action
     */
    public static AppointmentIndexerAction create( int nIdAppointment, int nIdTask )
    {
        AppointmentIndexerAction action = new AppointmentIndexerAction( );
        action.setIdAppointment( nIdAppointment );
        action.setIdTask( nIdTask );
        _dao.insert( action, _plugin );
        return action;
    }

    /**
     * Remove a processed action
     *
     * @param nIdAction
     *            the id of the action
     */
    public static void remove( int nIdAction )
    {
        _dao.delete( nIdAction, _plugin );
    }

    /**
     * Pending actions of a task
     *
     * @param nIdTask
     *            the task
     * @return the actions, oldest first
     */
    public static List<AppointmentIndexerAction> getListByTask( int nIdTask )
    {
        return _dao.selectByTask( nIdTask, _plugin );
    }
}
