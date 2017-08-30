/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ve.com.telefonica.actualizacioncampowap.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import ve.com.telefonica.actualizacioncampowap.webservices.ServicioWebExperto;



/**
 *
 * @author C96417
 */
public class ServiceUtil
{
	private static Logger logger = Logger.getLogger(ServiceUtil.class);
	public static final String DATE_FORMAT = "dd-MM-yyyy";
	public static final String FULL_FORMAT = DATE_FORMAT + " KK:mm a";
	public static final String ORA_FULL_FORMAT = DATE_FORMAT + " HH:MI PM";
	public static final SimpleDateFormat sdf = new SimpleDateFormat(FULL_FORMAT);
	public static final String EMPTY_STRING = "";


	public static String getParameter(HttpServletRequest request, String key)
	{
		String result = defaultIfEmpty(defaultIfEmpty(request.getParameter(key),
																	 (String) request.getAttribute(key)), EMPTY_STRING);
		if (isEmptyOrNullValue(result))
		{
			HttpSession session = request.getSession();
			if (session.getAttribute(key) != null && session.getAttribute(key) instanceof String)
			{
				result = defaultIfEmpty(result, (String) session.getAttribute(key));
			}
		}
		return emptyIfNull(result);
	}


	public static String emptyIfNull(String val)
	{
		return isEmptyOrNullValue(val) ? EMPTY_STRING : val;
	}


	public static String defaultIfEmpty(String value, String defaultValue)
	{
		return isEmptyOrNullValue(value) ? defaultValue : value.trim();
	}


	public static boolean isEmptyOrNullValue(String string)
	{
		return (isEmpty(string) || (string.trim().equalsIgnoreCase("null")));
	}


	public static boolean isEmpty(String string)
	{
		return string == null || string.trim().equals(EMPTY_STRING);
	}


	public static String formatDate(Date date)
	{
		try
		{
			return sdf.format(date);
		}
		catch (Exception e)
		{
			return ServiceUtil.EMPTY_STRING;
		}
	}


	public static String returnEmptyIfNull(String val)
	{
		return isEmptyOrNullValue(val) ? EMPTY_STRING : val;
	}

//    public static void setValuesDefault(HttpSession session) throws Exception {
//
//        InfoValues infoValues = InfoProxy.getInfoValuesService("accesoestaciones");
//
//        if (infoValues != null) {
//            session.setAttribute(Constants.LIST_TYPE_STATUS_SOLICITATION_ACCESS, infoValues.getEstadoSolicitudAccesoType());
//            session.setAttribute(Constants.LIST_TYPE_WORK, infoValues.getTipoTrabajoType());
//            session.setAttribute(Constants.LIST_TYPE_STATUS_SOLICITATION_CARD, infoValues.getEstadoSolicitudCarnetType());
//            session.setAttribute(Constants.LIST_TYPE_STATUS_CARD, infoValues.getEstadoCarnetType());
//            session.setAttribute(Constants.LIST_TYPE_STATUS_REGISTER, infoValues.getEstatusRegistroType());
//            session.setAttribute(Constants.LIST_TYPE_STATUS_REGISTER_OUT, infoValues.getEstatusSalidaType());
//            session.setAttribute(Constants.LIST_TYPE_ACCESS_REGISTER, infoValues.getTipoAccesoRegistroType());
//        }
//    }

	/**
	 * Metodo que termina una fecha de inicio para las consultas por defecto
	 *
	 * @param type
	 * @return Date
	 */
	public static Date getStartDateSearch(int type)
	{
		Calendar c1 = Calendar.getInstance();
		int days = 0;
		switch (type)
		{
			case 1: // registerAccessDefault
				days =
					ApplicationProperty.getIntegerProperty(Constants.RULES_FILTER_REGISTERACCESS_SEARCH_DEFAULT,
																		false);
				break;
			case 2: // solicitationAccessDefault
				days = ApplicationProperty.getIntegerProperty(
					Constants.RULES_FILTER_SOLICITATION_ACCESS_SEARCH_DEFAULT, false);
				break;
			case 3: // solicitationCardDefault
				days = ApplicationProperty.getIntegerProperty(
					Constants.RULES_FILTER_SOLICITATION_CARD_SEARCH_DEFAULT, false);
				break;
		}
		c1.add(Calendar.DATE, days * -1);
		return c1.getTime();
	}


	/**
	 * Metodo que permite verificar si un rango de fecha esta dentro del rango permitido de tiempo para las
	 * consultas
	 *
	 * @param start
	 * @param end
	 * @return boolean
	 */
	public static boolean validateDateRange(Date start, Date end, int type)
	{
		boolean result = true;

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(end);
		int days = 0;
		switch (type)
		{
			case 1: // registerAccessDefault
				days = ApplicationProperty.getIntegerProperty(Constants.RULES_FILTER_REGISTERACCESS_SEARCH_RANGE,
																			 false);
				break;
			case 2: // SolicitationAccessDefault
				days = ApplicationProperty.getIntegerProperty(
					Constants.RULES_FILTER_SOLICITATION_ACCESS_SEARCH_RANGE, false);
				break;
			case 3: // SolicitationCardDefault
				days = ApplicationProperty.getIntegerProperty(
					Constants.RULES_FILTER_SOLICITATION_CARD_SEARCH_RANGE, false);
				break;
		}
		logger.debug("Dias: " + days);
		c.add(Calendar.DATE, (days * -1) - 1);
		logger.debug("fecha de referencia: " + c.getTime());
		if (start.before(c.getTime()))
		{
			result = false;
		}
		return result;
	}


	public static String getDaysRules(int type)
	{
		String days = null;
		switch (type)
		{
			case 1: // registerAccessDefault
				days = ApplicationProperty.getProperty(Constants.RULES_FILTER_REGISTERACCESS_SEARCH_RANGE, false);
				break;
			case 2: // SolicitationAccessDefault
				days = ApplicationProperty.getProperty(Constants.RULES_FILTER_SOLICITATION_ACCESS_SEARCH_RANGE,
																	false);
				break;
			case 3: // SolicitationCardDefault
				days = ApplicationProperty.getProperty(Constants.RULES_FILTER_SOLICITATION_CARD_SEARCH_RANGE,
																	false);
				break;
		}
		return days;
	}


	public static String OrganizarComentariosTicket(String comentario)
	{
		try
		{
 			if (comentario.isEmpty())
			{
				return comentario;
			}
			List<String> fechaTimeStamp = extraerTimeStampComentario(comentario);
			String fechaReal;
			int i = 1;
			for (String fecha : fechaTimeStamp)
			{
				fechaReal = ManejadorFecha.convertirTimeStampToFecha(fecha);
				comentario = comentario.replace(fecha, "\n\n Status falla(" + i + ")  (" + fechaReal + ") ");
				i++;
			}
			comentario = comentario.replaceAll("\\uf8e3", " ");
			comentario = comentario.replaceAll("\\uf8e2", " ");

			List<String> codEmpleados = extraerCodigoEmpleadoComentario(comentario);
			for (String codigo : codEmpleados)
			{
				comentario = comentario.replace(codigo+" ", codigo + ". \n");
			}
//                  comentario = comentario.replaceAll("<br>", "\n"); RQ-52652 Daniel Sánchez
                    comentario = "++++++++++++++++++++++++++++++++++++++++++++++++++++++\n" + comentario; //RQ-52652 Daniel Sánchez
                    comentario = comentario.replaceAll("<br><br>", "\n\n++++++++++++++++++++++++++++++++++++++++++++++++++++++\n"); //RQ-52652 Daniel Sánchez
                    comentario = comentario.replaceAll("<br>", "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++\n"); //RQ-52652 Daniel Sánchez
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		return comentario;
	}


	public static List<String> extraerTimeStampComentario(String entrada)
	{

		List<String> cic = new ArrayList<String>();
		Pattern p = Pattern.compile("[\\uf8e3]?\\d{10}[\\uf8e3]");
		Matcher m = p.matcher(entrada);
		while (m.find())
		{
//         System.out.println(m.group());
			String timeStamp = m.group();

			int q = timeStamp.length();
			int y = q - 1;
			String stp = timeStamp.substring(0, y);
			cic.add(stp.trim());
		}
		return cic;
	}


	public static List<String> extraerCodigoEmpleadoComentario(String entrada)
	{

		List<String> cEmpl = new ArrayList<String>();
		Pattern p = Pattern.compile(" ?([A-Z])\\d{5}? ");
		Matcher m = p.matcher(entrada);
		while (m.find())
		{
			System.out.println(m.group());
			String timeStamp = m.group();

			int q = timeStamp.length();
			int y = q - 1;
			String stp = timeStamp.substring(0, y);
			cEmpl.add(stp.trim());
		}
		return cEmpl;
	}
}
