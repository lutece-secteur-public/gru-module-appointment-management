package fr.paris.lutece.plugins.appointment.modules.management.business.search;

import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFilterDTO;

public class MultiviewFilter extends AppointmentFilterDTO
{
    private static final long serialVersionUID = -6620743544234376592L;
    
    private int _nIdCategory;

    /**
     * @return the nIdCategory
     */
    public int getIdCategory( )
    {
        return _nIdCategory;
    }

    /**
     * @param nIdCategory the nIdCategory to set
     */
    public void setIdCategory( int nIdCategory )
    {
        _nIdCategory = nIdCategory;
    }
    
}
