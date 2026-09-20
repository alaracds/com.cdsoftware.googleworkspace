package com.cdsoftware.googleworkspace.rest;

import com.cdsoftware.googleworkspace.chat.GoogleChatCommand;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommandService;
import com.cdsoftware.googleworkspace.chat.GoogleChatEvent;
import com.cdsoftware.googleworkspace.chat.GoogleChatSpaceService;
import com.cdsoftware.googleworkspace.chat.GoogleChatUserService;
import com.google.gson.Gson;
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

        String nombre = event.chat.user.displayName;
        String email = event.chat.user.email;
        String mensaje = event.chat.messagePayload.message.text;
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

        String respuesta;

        /*
         * Usuario no registrado
         */
        if (chatUser == null) {

            respuesta =
                    "Tu usuario de Google Chat no está autorizado "
                    + "para usar esta aplicación.";

        /*
         * Space no registrado
         */
        } else if (chatSpace == null) {

            respuesta =
                    "Este espacio de Google Chat no está configurado "
                    + "en iDempiere.";

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

                respuesta = commandService.execute(
                        command,
                        chatSpace,
                        adUserId,
                        adRoleId);

            } else {

                respuesta = "Hola " + nombre
                        + ". Space reconocido: "
                        + chatSpace.get_ValueAsString("Name")
                        + ". Recibí tu mensaje: "
                        + mensaje;
            }
        }

        /*
         * Construir respuesta para Google Chat
         */
        JsonObject message = new JsonObject();
        message.addProperty("text", respuesta);

        JsonObject createMessageAction = new JsonObject();
        createMessageAction.add("message", message);

        JsonObject chatDataAction = new JsonObject();
        chatDataAction.add(
                "createMessageAction",
                createMessageAction);

        JsonObject hostAppDataAction = new JsonObject();
        hostAppDataAction.add(
                "chatDataAction",
                chatDataAction);

        JsonObject response = new JsonObject();
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
