package com.cdsoftware.googleworkspace.rest;

import com.cdsoftware.googleworkspace.chat.GoogleChatActionService;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommand;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommandResult;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommandService;
import com.cdsoftware.googleworkspace.chat.GoogleChatEvent;
import com.cdsoftware.googleworkspace.chat.GoogleChatSpaceService;
import com.cdsoftware.googleworkspace.chat.GoogleChatUserService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.compiere.model.PO;

@Path("/v1/google")
public class GoogleChatResource {

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public String hello() {
        return """
            {
                "message": "Hola desde Google Workspace"
            }
            """;
    }

    @POST
    @Path("chat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response chat(String body) {

        Gson gson = new Gson();

        GoogleChatEvent event =
                gson.fromJson(body, GoogleChatEvent.class);

        /*
         * Aplicación agregada a un Space
         */
        if (event.chat != null
                && event.chat.addedToSpacePayload != null
                && event.chat.addedToSpacePayload.space != null) {

            String googleSpaceId =
                    event.chat.addedToSpacePayload.space.name;

            System.out.println(
                    ">>> Google Chat app agregada a un Space <<<");

            System.out.println(
                    "Space: " + googleSpaceId);

            /*
             * Verificar si el Space ya está configurado
             */
            GoogleChatSpaceService spaceService =
                    new GoogleChatSpaceService();

            PO chatSpace =
                    spaceService.findByGoogleSpaceId(
                            googleSpaceId);

            GoogleChatCommandResult result;

            if (chatSpace == null) {

                result = GoogleChatCommandResult.text(
                        "iDempiere fue agregado correctamente "
                        + "a este espacio.\n\n"
                        + "Este espacio todavía no está "
                        + "configurado en iDempiere.\n\n"
                        + "Google Space ID: "
                        + googleSpaceId);

            } else {

                result = GoogleChatCommandResult.text(
                        "iDempiere fue agregado correctamente "
                        + "a este espacio.\n\n"
                        + "Configuración: "
                        + chatSpace.get_ValueAsString("Name"));
            }

            return buildResponse(
                    result,
                    gson);
        }

        if (event.chat != null
                && event.chat.buttonClickedPayload != null) {

            String action = null;
            String requestId = null;

            if (event.commonEventObject != null
                    && event.commonEventObject.parameters != null) {

                action =
                        event.commonEventObject.parameters.get("action");

                requestId =
                        event.commonEventObject.parameters.get("requestId");
            }

            String googleUserId =
                    event.chat.user != null
                            ? event.chat.user.name
                            : null;

            String googleSpaceId =
                    event.chat.buttonClickedPayload.space != null
                            ? event.chat.buttonClickedPayload.space.name
                            : null;

            PO space =
                    new GoogleChatSpaceService()
                            .findByGoogleSpaceId(
                                    googleSpaceId);

            if (space == null) {
                return buildResponse(
                        GoogleChatCommandResult.text(
                                "Este espacio de Google Chat no está "
                                + "configurado en iDempiere."),
                        gson);
            }

            PO chatUser =
                    new GoogleChatUserService()
                            .findByGoogleUserId(
                                    googleUserId);

            if (chatUser == null) {
                return buildResponse(
                        GoogleChatCommandResult.text(
                                "Tu usuario de Google Chat no está "
                                + "configurado en iDempiere."),
                        gson);
            }

            GoogleChatActionService actionService =
                    new GoogleChatActionService();

            GoogleChatCommandResult result =
                    actionService.execute(
                            action,
                            requestId,
                            space,
                            chatUser);

            return buildResponse(
                    result,
                    gson);
        }

        String nombre = event.chat.user.displayName;
        String email = event.chat.user.email;
        String mensaje =
                event.chat.messagePayload.message.argumentText;

        if (mensaje == null || mensaje.isBlank()) {
            mensaje =
                    event.chat.messagePayload.message.text;
        }

        mensaje = mensaje.trim();
        System.out.println(
                "Message.text: "
                + event.chat.messagePayload.message.text);

        System.out.println(
                "Message.argumentText: "
                + event.chat.messagePayload.message.argumentText);

        System.out.println(
                "Mensaje procesado: "
                + mensaje);
        String space = event.chat.messagePayload.space.name;
        String googleUserId = event.chat.user.name;

        System.out.println(">>> Evento de Google Chat <<<");
        System.out.println("Usuario: " + nombre);
        System.out.println("Email: " + email);
        System.out.println("Mensaje: " + mensaje);
        System.out.println("Space: " + space);

        /*
         * Identificar al usuario humano de Google Chat
         */
        GoogleChatUserService userService =
                new GoogleChatUserService();

        PO chatUser =
                userService.findByGoogleUserId(googleUserId);

        /*
         * Identificar el Space de Google Chat
         */
        GoogleChatSpaceService spaceService =
                new GoogleChatSpaceService();

        PO chatSpace =
                spaceService.findByGoogleSpaceId(space);

        GoogleChatCommandResult result;

        /*
         * Usuario no registrado
         */
        if (chatUser == null) {

            result = GoogleChatCommandResult.text(
                    "Tu usuario de Google Chat no está autorizado "
                    + "para usar esta aplicación.");

        /*
         * Space no registrado
         */
        } else if (chatSpace == null) {

            result = GoogleChatCommandResult.text(
                    "Este espacio de Google Chat no está configurado "
                    + "en iDempiere.");

        } else {

            /*
             * Usuario humano de iDempiere
             */
            int adUserId =
                    chatUser.get_ValueAsInt("AD_User_ID");

            int adRoleId =
                    chatUser.get_ValueAsInt("AD_Role_ID");

            System.out.println(
                    "AD_User humano: " + adUserId);

            System.out.println(
                    "AD_Role Google Chat: " + adRoleId);

            /*
             * Interpretar mensaje
             */
            GoogleChatCommand command =
                    new GoogleChatCommand(mensaje);

            if (command.isCommand()) {

                /*
                 * Ejecutar comando
                 */
                GoogleChatCommandService commandService =
                        new GoogleChatCommandService();

                result =
                        commandService.execute(
                                command,
                                chatSpace,
                                adUserId,
                                adRoleId);

            } else {

                result = GoogleChatCommandResult.text(
                        "Hola " + nombre
                        + ". Space reconocido: "
                        + chatSpace.get_ValueAsString("Name")
                        + ". Recibí tu mensaje: "
                        + mensaje);
            }
        }

        return buildResponse(
                result,
                gson);
    }

    private Response buildResponse(
            GoogleChatCommandResult result,
            Gson gson) {

        JsonObject message = new JsonObject();

        if (result.hasCard()) {

            JsonArray cards = new JsonArray();

            JsonObject cardWrapper = new JsonObject();
            cardWrapper.addProperty(
                    "cardId",
                    "idempiere-card");

            cardWrapper.add(
                    "card",
                    result.getCard());

            cards.add(cardWrapper);

            message.add(
                    "cardsV2",
                    cards);

        } else {

            message.addProperty(
                    "text",
                    result.getText());
        }

        JsonObject createMessageAction =
                new JsonObject();

        createMessageAction.add(
                "message",
                message);

        JsonObject chatDataAction =
                new JsonObject();

        chatDataAction.add(
                "createMessageAction",
                createMessageAction);

        JsonObject hostAppDataAction =
                new JsonObject();

        hostAppDataAction.add(
                "chatDataAction",
                chatDataAction);

        JsonObject response =
                new JsonObject();

        response.add(
                "hostAppDataAction",
                hostAppDataAction);

        String responseJson =
                gson.toJson(response);

        System.out.println(
                "Respuesta: " + responseJson);

        return Response
                .ok(
                        responseJson,
                        MediaType.APPLICATION_JSON)
                .build();
    }

}
