package com.cdsoftware.googleworkspace.chat;

import org.compiere.model.PO;

import com.cdsoftware.googleworkspace.chat.command.RequestCommand;

public class GoogleChatCommandService {

	public String execute(
	        GoogleChatCommand command,
	        PO chatSpace,
	        int adUserId,
	        int adRoleId) {

        switch (command.getCommand()) {

        case "solicitudes":
            return new RequestCommand().execute(
                    command,
                    chatSpace,
                    adUserId,
                    adRoleId);

            case "facturas":
                return "Ejecutando comando de facturas."
                        + formatArguments(command);

            default:
                return "Comando no reconocido: /"
                        + command.getCommand();
        }
    }

    private String formatArguments(GoogleChatCommand command) {

        if (command.getArguments().isEmpty()) {
            return "";
        }

        return "\nArgumentos: " + command.getArguments();
    }
}