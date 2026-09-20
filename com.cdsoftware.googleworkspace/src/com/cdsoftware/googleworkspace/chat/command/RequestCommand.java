package com.cdsoftware.googleworkspace.chat.command;

import java.util.List;

import org.compiere.model.MRequest;
import org.compiere.model.MRole;
import org.compiere.model.PO;

import com.cdsoftware.googleworkspace.chat.GoogleChatAuthorizationService;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommand;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommandResult;
import com.cdsoftware.googleworkspace.chat.GoogleChatExecutionContext;
import com.cdsoftware.googleworkspace.service.RequestService;
import com.cdsoftware.googleworkspace.service.RequestService.StatusFilter;

public class RequestCommand {

	public GoogleChatCommandResult execute(
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
		    return GoogleChatCommandResult.text("El rol configurado para tu usuario no tiene "
		            + "permisos para consultar solicitudes en iDempiere.");
		}

		GoogleChatExecutionContext executionContext =
		        new GoogleChatExecutionContext(
		                adUserId,
		                adRoleId);

	    int cBPartnerId =
	            chatSpace.get_ValueAsInt("C_BPartner_ID");

	    if (cBPartnerId <= 0) {
	        return GoogleChatCommandResult.text("Este Space no tiene un socio de negocio asociado.");
	    }

	    String filtro = command.getArguments().trim().toLowerCase();

	    if (filtro.isEmpty()) {
	        filtro = "abiertas";
	    }

	    StatusFilter statusFilter;

        switch (filtro) {

        case "abiertas":
            statusFilter = StatusFilter.OPEN;
            break;

        case "cerradas":
            statusFilter = StatusFilter.CLOSED;
            break;

        case "todas":
            statusFilter = StatusFilter.ALL;
            break;

        default:
            return GoogleChatCommandResult.text("Filtro no reconocido: " + filtro
                    + "\nUsa: /solicitudes abiertas, "
                    + "/solicitudes cerradas o "
                    + "/solicitudes todas");
        }

        RequestService requestService =
                new RequestService();

        List<MRequest> requests =
                requestService.findRequests(
                        executionContext.getCtx(),
                        cBPartnerId,
                        statusFilter,
                        5);

	    if (requests.isEmpty()) {
	        return GoogleChatCommandResult.text("No se encontraron solicitudes "
	                + filtro
	                + " para este socio de negocio.");
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

	    return GoogleChatCommandResult.text(response.toString());
	}
}
