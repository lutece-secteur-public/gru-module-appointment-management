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

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * DAO of the queue of the multiview indexer, table appointment_management_indexer_action
 */
@ApplicationScoped
public class AppointmentIndexerActionDAO implements IAppointmentIndexerActionDAO
{
    private static final String SQL_QUERY_INSERT = "INSERT INTO appointment_management_indexer_action ( id_appointment, id_task ) VALUES ( ?, ? )";
    private static final String SQL_QUERY_DELETE = "DELETE FROM appointment_management_indexer_action WHERE id_action = ?";
    private static final String SQL_QUERY_SELECT_BY_TASK = "SELECT id_action, id_appointment, id_task FROM appointment_management_indexer_action WHERE id_task = ? ORDER BY id_action";

    @Override
    public void insert( AppointmentIndexerAction action, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, action.getIdAppointment( ) );
            daoUtil.setInt( nIndex, action.getIdTask( ) );
            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                action.setIdAction( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    @Override
    public void delete( int nIdAction, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nIdAction );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public List<AppointmentIndexerAction> selectByTask( int nIdTask, Plugin plugin )
    {
        List<AppointmentIndexerAction> list = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_TASK, plugin ) )
        {
            daoUtil.setInt( 1, nIdTask );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                int nIndex = 1;
                AppointmentIndexerAction action = new AppointmentIndexerAction( );
                action.setIdAction( daoUtil.getInt( nIndex++ ) );
                action.setIdAppointment( daoUtil.getInt( nIndex++ ) );
                action.setIdTask( daoUtil.getInt( nIndex ) );
                list.add( action );
            }
        }
        return list;
    }
}
