package com.cdsoftware.googleworkspace.chat.command;


import org.compiere.model.MRequest;
import org.compiere.model.MRole;
import org.compiere.model.PO;

import com.cdsoftware.googleworkspace.chat.GoogleChatAuthorizationService;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommand;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommandResult;
import com.cdsoftware.googleworkspace.chat.GoogleChatExecutionContext;
import com.cdsoftware.googleworkspace.chat.card.RequestCardBuilder;
import com.cdsoftware.googleworkspace.service.RequestService;

public class RequestDetailCommand {

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
            return GoogleChatCommandResult.text("El rol configurado para tu usuario no tiene permisos "
                    + "para consultar solicitudes en iDempiere.");
        }

        String documentNo =
                command.getArguments().trim();

        if (documentNo.isEmpty()) {
            return GoogleChatCommandResult.text("Debes indicar el número de solicitud."
                    + "\nEjemplo: /solicitud 10000002");
        }

        int cBPartnerId =
                chatSpace.get_ValueAsInt("C_BPartner_ID");

        if (cBPartnerId <= 0) {
            return GoogleChatCommandResult.text("Este Space no tiene un socio de negocio asociado.");
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
            return GoogleChatCommandResult.text("No se encontró la solicitud "
                    + documentNo
                    + " o no tienes permisos para consultarla.");
        }

        RequestCardBuilder cardBuilder =
                new RequestCardBuilder();

        return GoogleChatCommandResult.card(
                cardBuilder.build(request));
    }
}
