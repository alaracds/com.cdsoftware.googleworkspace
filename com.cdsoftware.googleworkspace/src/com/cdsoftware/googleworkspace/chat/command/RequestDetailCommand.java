package com.cdsoftware.googleworkspace.chat.command;


import org.compiere.model.MRequest;
import org.compiere.model.MRole;
import org.compiere.model.PO;

import com.cdsoftware.googleworkspace.chat.GoogleChatAuthorizationService;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommand;
import com.cdsoftware.googleworkspace.chat.GoogleChatExecutionContext;
import com.cdsoftware.googleworkspace.service.RequestService;

public class RequestDetailCommand {

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
            return "El rol configurado para tu usuario no tiene permisos "
                    + "para consultar solicitudes en iDempiere.";
        }

        String documentNo =
                command.getArguments().trim();

        if (documentNo.isEmpty()) {
            return "Debes indicar el número de solicitud."
                    + "\nEjemplo: /solicitud 10000002";
        }

        int cBPartnerId =
                chatSpace.get_ValueAsInt("C_BPartner_ID");

        if (cBPartnerId <= 0) {
            return "Este Space no tiene un socio de negocio asociado.";
        }

		GoogleChatExecutionContext executionContext =
		        new GoogleChatExecutionContext(
		                adUserId,
		                adRoleId);

        RequestService requestService =
                new RequestService();

        MRequest request =
                requestService.findRequest(
                        executionContext.getCtx(),
                        cBPartnerId,
                        documentNo);

        if (request == null) {
            return "No se encontró la solicitud "
                    + documentNo
                    + " o no tienes permisos para consultarla.";
        }

        StringBuilder response =
                new StringBuilder();

        response.append("Solicitud ")
                .append(request.getDocumentNo())
                .append("\n\n");

        response.append("Resumen: ")
                .append(request.getSummary())
                .append("\n");

        response.append("Creada: ")
                .append(request.getCreated())
                .append("\n");

        return response.toString();
    }
}
