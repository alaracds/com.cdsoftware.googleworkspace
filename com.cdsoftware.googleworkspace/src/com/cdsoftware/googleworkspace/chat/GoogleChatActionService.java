package com.cdsoftware.googleworkspace.chat;

import java.util.List;

import org.compiere.model.MRequest;
import org.compiere.model.MRequestUpdate;
import org.compiere.model.PO;

import com.cdsoftware.googleworkspace.chat.card.RequestCardBuilder;
import com.cdsoftware.googleworkspace.chat.card.RequestUpdatesCardBuilder;
import com.cdsoftware.googleworkspace.service.GoogleChatAuthorizationService;
import com.cdsoftware.googleworkspace.service.RequestService;

public class GoogleChatActionService {

    private final RequestService requestService =
            new RequestService();

    private final GoogleChatAuthorizationService authorizationService =
            new GoogleChatAuthorizationService();

    public GoogleChatCommandResult execute(
            String action,
            String requestIdValue,
            PO space,
            PO chatUser) {

        switch (action) {

        case "requestDetail":
            return requestDetail(
                    requestIdValue,
                    space,
                    chatUser);

        case "requestUpdates":
            return requestUpdates(
                    requestIdValue,
                    space,
                    chatUser);

        default:
            return GoogleChatCommandResult.text(
                    "Acción no reconocida.");
    }


    }

    private GoogleChatCommandResult requestUpdates(
            String requestIdValue,
            PO space,
            PO chatUser) {
        int requestId;

        try {
            requestId =
                    Integer.parseInt(requestIdValue);
        } catch (Exception e) {
            return GoogleChatCommandResult.text(
                    "Identificador de solicitud inválido.");
        }

        int adUserId =
                chatUser.get_ValueAsInt("AD_User_ID");

        int adRoleId =
                chatUser.get_ValueAsInt("AD_Role_ID");

        if (authorizationService.getReadRole(
                adUserId,
                adRoleId,
                MRequest.Table_ID) == null) {

            return GoogleChatCommandResult.text(
                    "No tienes permisos para consultar solicitudes.");
        }

        GoogleChatExecutionContext executionContext =
                new GoogleChatExecutionContext(
                        adUserId,
                        adRoleId);

        int cBPartnerId =
                space.get_ValueAsInt("C_BPartner_ID");

        MRequest request =
                requestService.findRequest(
                        executionContext.getCtx(),
                        cBPartnerId,
                        requestId);

        if (request == null) {
            return GoogleChatCommandResult.text(
                    "No se encontró la solicitud o "
                    + "no tienes permisos para consultarla.");
        }

        List<MRequestUpdate> updates =
                requestService.findUpdates(
                        executionContext.getCtx(),
                        requestId,
                        10);

        RequestUpdatesCardBuilder cardBuilder =
                new RequestUpdatesCardBuilder();

        return GoogleChatCommandResult.card(
                cardBuilder.build(
                        executionContext.getCtx(),
                        request,
                        updates));

    }

    private GoogleChatCommandResult requestDetail(
            String requestIdValue,
            PO space,
            PO chatUser) {

        int requestId;

        try {
            requestId =
                    Integer.parseInt(requestIdValue);
        } catch (Exception e) {
            return GoogleChatCommandResult.text(
                    "Identificador de solicitud inválido.");
        }

        int adUserId =
                chatUser.get_ValueAsInt(
                        "AD_User_ID");

        int adRoleId =
                chatUser.get_ValueAsInt(
                        "AD_Role_ID");

        if (authorizationService.getReadRole(
                adUserId,
                adRoleId,
                MRequest.Table_ID) == null) {

            return GoogleChatCommandResult.text(
                    "No tienes permisos para consultar solicitudes.");
        }

        GoogleChatExecutionContext executionContext =
                new GoogleChatExecutionContext(
                        adUserId,
                        adRoleId);

        int cBPartnerId =
                space.get_ValueAsInt(
                        "C_BPartner_ID");

        MRequest request =
                requestService.findRequest(
                        executionContext.getCtx(),
                        cBPartnerId,
                        requestId);

        if (request == null) {
            return GoogleChatCommandResult.text(
                    "No se encontró la solicitud o "
                    + "no tienes permisos para consultarla.");
        }

        RequestCardBuilder cardBuilder =
                new RequestCardBuilder();

        return GoogleChatCommandResult.card(
                cardBuilder.build(request));
    }
}
