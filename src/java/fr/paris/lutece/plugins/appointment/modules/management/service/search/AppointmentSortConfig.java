package fr.paris.lutece.plugins.appointment.modules.management.service.search;

public class AppointmentSortConfig
{
    private final String _strSortAttributeName;
    private final boolean _bAscSort;
    
    public AppointmentSortConfig( String strSortAttributeName, boolean bAscSort )
    {
        _strSortAttributeName = strSortAttributeName;
        _bAscSort = bAscSort;
    }

    /**
     * @return the strSortAttributeName
     */
    public String getSortAttributeName( )
    {
        return _strSortAttributeName;
    }

    /**
     * @return the bAscSort
     */
    public boolean isAscSort( )
    {
        return _bAscSort;
    }
}
