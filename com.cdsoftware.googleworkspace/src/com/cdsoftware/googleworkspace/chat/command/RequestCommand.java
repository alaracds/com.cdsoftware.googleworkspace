package com.cdsoftware.googleworkspace.chat.command;

import java.util.List;
import java.util.Properties;

import org.compiere.model.MRequest;
import org.compiere.model.MRole;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.Env;

import com.cdsoftware.googleworkspace.chat.GoogleChatAuthorizationService;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommand;

public class RequestCommand {

	public String execute(
	        GoogleChatCommand command,
	        PO chatSpace,
	        int adUserId,
	        int adRoleId) {

		GoogleChatAuthorizationService authorizationService =
		        new GoogleChatAuthorizationService();

		MRole role =
		        authorizationService.getReadRole(
		                adUserId,
		                adRoleId,
		                MRequest.Table_ID);

		if (role == null) {
		    return "El rol configurado para tu usuario no tiene permisos para consultar solicitudes en iDempiere.";
		}

		Properties userCtx = new Properties();

		userCtx.putAll(Env.getCtx());

		Env.setContext(
		        userCtx,
		        Env.AD_USER_ID,
		        adUserId);

		Env.setContext(
		        userCtx,
		        Env.AD_ROLE_ID,
		        adRoleId);

	    int cBPartnerId =
	            chatSpace.get_ValueAsInt("C_BPartner_ID");

	    if (cBPartnerId <= 0) {
	        return "Este Space no tiene un socio de negocio asociado.";
	    }
	    
	    String filtro = command.getArguments().trim().toLowerCase();

	    if (filtro.isEmpty()) {
	        filtro = "abiertas";
	    }
	    
	    String whereClause = "C_BPartner_ID=?";

	    switch (filtro) {

	        case "abiertas":
	            whereClause +=
	                    " AND R_Status_ID IN ("
	                    + "SELECT R_Status_ID "
	                    + "FROM R_Status "
	                    + "WHERE IsOpen='Y'"
	                    + ")";
	            break;

	        case "cerradas":
	            whereClause +=
	                    " AND R_Status_ID IN ("
	                    + "SELECT R_Status_ID "
	                    + "FROM R_Status "
	                    + "WHERE IsClosed='Y'"
	                    + ")";
	            break;

	        case "todas":
	            break;

	        default:
	            return "Filtro no reconocido: " + filtro
	                    + "\nUsa: /solicitudes abiertas, "
	                    + "/solicitudes cerradas o "
	                    + "/solicitudes todas";
	    }
	    

	    List<MRequest> requests = new Query(
	            userCtx,
	            MRequest.Table_Name,
	            whereClause,
	            null)
	            .setClient_ID()
	            .setOnlyActiveRecords(true)
	            .setApplyAccessFilter(true)
	            .setParameters(cBPartnerId)
	            .setOrderBy("Created DESC")
	            .setPageSize(5)
	            .list();
	    
	    if (requests.isEmpty()) {
	        return "No se encontraron solicitudes "
	                + filtro
	                + " para este socio de negocio.";
	    }

	    StringBuilder response = new StringBuilder();

	    response.append("Solicitudes ")
	            .append(filtro)
	            .append(":\n\n");

	    for (MRequest request : requests) {

	        response.append("• ")
	                .append(request.getDocumentNo())
	                .append(" - ")
	                .append(request.getSummary())
	                .append("\n");
	    }

	    return response.toString();
	}
}
