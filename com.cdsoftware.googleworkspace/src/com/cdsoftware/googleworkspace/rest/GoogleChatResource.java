package com.cdsoftware.googleworkspace.rest;

import com.cdsoftware.googleworkspace.chat.GoogleChatAuthorizationService;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommand;
import com.cdsoftware.googleworkspace.chat.GoogleChatCommandService;
import com.cdsoftware.googleworkspace.chat.GoogleChatEvent;
import com.cdsoftware.googleworkspace.chat.GoogleChatSpaceService;
import com.cdsoftware.googleworkspace.chat.GoogleChatUserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.compiere.model.MRole;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.Env;

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

	    GoogleChatUserService userService =
	            new GoogleChatUserService();

	    PO chatUser =
	            userService.findByGoogleUserId(googleUserId);
	    
	    GoogleChatSpaceService spaceService =
	            new GoogleChatSpaceService();

	    PO chatSpace =
	            spaceService.findByGoogleSpaceId(space);
	    
	    String respuesta;

	    if (chatUser == null) {

	        respuesta = "Tu usuario de Google Chat no está autorizado para usar esta aplicación.";

	    } else if (chatSpace == null) {

	        respuesta = "Este espacio de Google Chat no está configurado en iDempiere.";
	    } else {
	    	int adUserId = chatUser.get_ValueAsInt("AD_User_ID");

	    	System.out.println(
	    	        "AD_User humano: " + adUserId);

	    	GoogleChatAuthorizationService authorizationService =
	    	        new GoogleChatAuthorizationService();

	    	List<MRole> roles =
	    	        authorizationService.getRequestReadRoles(adUserId);

	    	System.out.println(">>> Roles con acceso de lectura a R_Request <<<");

	    	for (MRole role : roles) {
	    	    System.out.println(
	    	            role.getAD_Role_ID()
	    	            + " - "
	    	            + role.getName());
	    	}
	    	
	    	if (roles.isEmpty()) {

	    	    respuesta =
	    	            "Tu usuario no tiene un rol con acceso a solicitudes en iDempiere.";

	    	} else {
		        GoogleChatCommand command =
		                new GoogleChatCommand(mensaje);

		        if (command.isCommand()) {

		            GoogleChatCommandService commandService =
		                    new GoogleChatCommandService();

			            respuesta = commandService.execute(
			                    command,
			                    chatSpace,
			                    adUserId);

		        } else {

		            respuesta = "Hola " + nombre
		                    + ". Space reconocido: "
		                    + chatSpace.get_ValueAsString("Name")
		                    + ". Recibí tu mensaje: "
		                    + mensaje;
		        }
	    	}
	    	

	    }   

	    JsonObject message = new JsonObject();
	    message.addProperty("text", respuesta);
	    
	    JsonObject createMessageAction = new JsonObject();
	    createMessageAction.add("message", message);

	    JsonObject chatDataAction = new JsonObject();
	    chatDataAction.add("createMessageAction", createMessageAction);

	    JsonObject hostAppDataAction = new JsonObject();
	    hostAppDataAction.add("chatDataAction", chatDataAction);

	    JsonObject response = new JsonObject();
	    response.add("hostAppDataAction", hostAppDataAction);

	    String responseJson = gson.toJson(response);

	    System.out.println("Respuesta: " + responseJson);

	    return Response
	            .ok(responseJson, MediaType.APPLICATION_JSON)
	            .build();
	}
	
}
