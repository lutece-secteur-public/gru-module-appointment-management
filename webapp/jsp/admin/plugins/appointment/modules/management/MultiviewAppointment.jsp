<jsp:useBean id="multiviewAppointmentAppointments" scope="session" class="fr.paris.lutece.plugins.appointment.modules.management.web.MultiviewAppointmentJspBean" />
<% String strContent = multiviewAppointmentAppointments.processController ( request , response ); %>

<%@ page errorPage="../../../../ErrorPage.jsp" %>
<jsp:include page="../../../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../../../AdminFooter.jsp" %>
