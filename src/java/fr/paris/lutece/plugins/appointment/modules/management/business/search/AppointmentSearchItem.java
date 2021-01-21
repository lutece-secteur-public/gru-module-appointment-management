package fr.paris.lutece.plugins.appointment.modules.management.business.search;

import org.apache.lucene.document.Document;

import fr.paris.lutece.portal.service.search.SearchItem;

/**
 * This class is use for processing searches in Appointment
 */
public class AppointmentSearchItem extends SearchItem
{

    public AppointmentSearchItem( Document document )
    {
        super( document );
    }

    public static final String FIELD_ID_APPOINTMENT = "id_appointment";
    public static final String FIELD_ID_FORM = "id_form";
    public static final String FIELD_FIRST_NAME = "first_name";
    public static final String FIELD_LAST_NAME = "last_name";
    public static final String FIELD_MAIL = "mail";
    public static final String FIELD_START_DATE = "start_date";
    public static final String FIELD_END_DATE = "end_date";
    public static final String FIELD_ADMIN = "admin";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_ID_WORKFLOW_STATE = "id_workflow_state";
    public static final String FIELD_NB_SEATS = "nb_seats";
    public static final String FIELD_DATE_APPOINTMENT_TAKEN = "date_appointment_taken";
}
