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
 */
package fr.paris.lutece.plugins.appointment.modules.management.business.search;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;

import fr.paris.lutece.plugins.appointment.service.Utilities;
import fr.paris.lutece.portal.service.search.SearchItem;
import fr.paris.lutece.portal.service.util.AppLogService;

/**
 * This class is use for processing searches in Appointment
 */
public class AppointmentSearchItem extends SearchItem
{

    public static final String FIELD_DATE_SUFFIX = "_date";
    public static final String FIELD_INT_SUFFIX = "_int";

    public static final String FIELD_ID_APPOINTMENT = "id_appointment";
    public static final String FIELD_ID_FORM = "id_form";
    public static final String FIELD_FIRST_NAME = "first_name";
    public static final String FIELD_FIRST_NAME_SEARCH = "first_name_search";
    public static final String FIELD_LAST_NAME = "last_name";
    public static final String FIELD_LAST_NAME_SEARCH = "last_name_search";
    public static final String FIELD_MAIL = "mail";
    public static final String FIELD_MAIL_SEARCH = "mail_search";
    public static final String FIELD_PHONE_NUMBER = "phone_number";
    public static final String FIELD_PHONE_NUMBER_SEARCH = "phone_number_search";
    public static final String FIELD_START_DATE = "start_date";
    public static final String FIELD_END_DATE = "end_date";
    public static final String FIELD_ADMIN = "admin";
    public static final String FIELD_CANCELLED = "cancelled";
    public static final String FIELD_ID_WORKFLOW_STATE = "id_workflow_state";
    public static final String FIELD_NB_SEATS = "nb_seats_int";
    public static final String FIELD_DATE_APPOINTMENT_TAKEN = "appointment_taken_date";
    public static final String FIELD_ID_CATEGORY = "id_category";

    private static final int INTEGER_MINUS_ONE = -1;

    private int _idAppointment;
    private int _idForm;
    private String _firstName;
    private String _lastName;
    private String _mail;
    private String _phoneNumber;
    private LocalDateTime _startDate;
    private LocalDateTime _endDate;
    private String _admin;
    private boolean _cancelled;
    private int _idState;
    private int _nbSeats;
    private LocalDateTime _dateAppointmentTaken;

    private String _dateOfTheAppointment;
    private LocalTime _startingTime;
    private LocalTime _endingTime;

    private int _idCategory;

    private String _stateTitle = "";
    private String _formTitle = "";
    private String _categoryTitle = "";

    public AppointmentSearchItem( Document document )
    {
        super( document );

        _idAppointment = manageIntegerNullValue( document.get( AppointmentSearchItem.FIELD_ID_APPOINTMENT ) );
        _idForm = manageIntegerNullValue( document.get( AppointmentSearchItem.FIELD_ID_FORM ) );
        _firstName = document.get( AppointmentSearchItem.FIELD_FIRST_NAME );
        _lastName = document.get( AppointmentSearchItem.FIELD_LAST_NAME );
        _mail = document.get( AppointmentSearchItem.FIELD_MAIL );
        _startDate = parseDate( document.get( FIELD_START_DATE ) );
        _phoneNumber = document.get( AppointmentSearchItem.FIELD_PHONE_NUMBER ) != null ? document.get( AppointmentSearchItem.FIELD_PHONE_NUMBER ) : "";
        _endDate = parseDate( document.get( FIELD_END_DATE ) );
        _admin = document.get( AppointmentSearchItem.FIELD_ADMIN );
        _cancelled = Boolean.valueOf( document.get( AppointmentSearchItem.FIELD_CANCELLED ) );
        _idState = manageIntegerNullValue( document.get( AppointmentSearchItem.FIELD_ID_WORKFLOW_STATE ) );
        _nbSeats = manageIntegerNullValue( document.get( AppointmentSearchItem.FIELD_NB_SEATS ) );
        _dateAppointmentTaken = parseDate( document.get( FIELD_DATE_APPOINTMENT_TAKEN ) );

        _dateOfTheAppointment = _startDate.toLocalDate( ).format( Utilities.getFormatter( ) );
        _startingTime = _startDate.toLocalTime( );
        _endingTime = _endDate.toLocalTime( );
        _idCategory = manageIntegerNullValue( document.get( AppointmentSearchItem.FIELD_ID_CATEGORY ) );
    }

    /**
     * @return the idAppointment
     */
    public int getIdAppointment( )
    {
        return _idAppointment;
    }

    /**
     * @return the idForm
     */
    public int getIdForm( )
    {
        return _idForm;
    }

    /**
     * @return the firstName
     */
    public String getFirstName( )
    {
        return _firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName( )
    {
        return _lastName;
    }

    /**
     * @return the mail
     */
    public String getMail( )
    {
        return _mail;
    }

    /**
     * @return the phone number
     */
    public String getPhoneNumber( )
    {
        return _phoneNumber;
    }
    
    /**
     * @return the startDate
     */
    public LocalDateTime getStartDate( )
    {
        return _startDate;
    }

    /**
     * @return the endDate
     */
    public LocalDateTime getEndDate( )
    {
        return _endDate;
    }

    /**
     * @return the admin
     */
    public String getAdmin( )
    {
        return _admin;
    }

    /**
     * @return the idState
     */
    public int getIdState( )
    {
        return _idState;
    }

    /**
     * @return the nbSeats
     */
    public int getNbSeats( )
    {
        return _nbSeats;
    }

    /**
     * @return the dateAppointmentTaken
     */
    public LocalDateTime getDateAppointmentTaken( )
    {
        return _dateAppointmentTaken;
    }

    private LocalDateTime parseDate( String strDocumentValue )
    {
        LocalDateTime date = null;

        if ( StringUtils.isNotEmpty( strDocumentValue ) )
        {
            date = LocalDateTime.ofInstant( Instant.ofEpochMilli( Long.valueOf( strDocumentValue ) ), TimeZone.getDefault( ).toZoneId( ) );
        }
        return date;
    }

    private Integer manageIntegerNullValue( String strDocumentValue )
    {
        Integer nReturn = INTEGER_MINUS_ONE;
        if ( strDocumentValue != null )
        {
            try
            {
                nReturn = Integer.parseInt( strDocumentValue );
            }
            catch( NumberFormatException e )
            {
                AppLogService.error( "Unable to convert " + strDocumentValue + " to integer." );
            }
        }
        return nReturn;
    }

    /**
     * @return the stateTitle
     */
    public String getStateTitle( )
    {
        return _stateTitle;
    }

    /**
     * @param stateTitle
     *            the stateTitle to set
     */
    public void setStateTitle( String stateTitle )
    {
        _stateTitle = stateTitle;
    }

    /**
     * @return the formTitle
     */
    public String getFormTitle( )
    {
        return _formTitle;
    }

    /**
     * @param formTitle
     *            the formTitle to set
     */
    public void setFormTitle( String formTitle )
    {
        _formTitle = formTitle;
    }

    /**
     * @return the dateOfTheAppointment
     */
    public String getDateOfTheAppointment( )
    {
        return _dateOfTheAppointment;
    }

    /**
     * @return the startingTime
     */
    public LocalTime getStartingTime( )
    {
        return _startingTime;
    }

    /**
     * @return the endingTime
     */
    public LocalTime getEndingTime( )
    {
        return _endingTime;
    }

    /**
     * 
     * @return _cancelled
     */
    public boolean isCancelled( )
    {
        return _cancelled;
    }

    /**
     * @return the categoryTitle
     */
    public String getCategoryTitle( )
    {
        return _categoryTitle;
    }

    /**
     * @param categoryTitle
     *            the categoryTitle to set
     */
    public void setCategoryTitle( String categoryTitle )
    {
        _categoryTitle = categoryTitle;
    }

    /**
     * @return the idCategory
     */
    public int getIdCategory( )
    {
        return _idCategory;
    }
}
