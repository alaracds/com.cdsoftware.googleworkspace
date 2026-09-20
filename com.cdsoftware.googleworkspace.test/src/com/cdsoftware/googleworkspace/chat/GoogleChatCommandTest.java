package com.cdsoftware.googleworkspace.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GoogleChatCommandTest {

    @Test
    void recognizesCommandAndNormalizesItsName() {
        GoogleChatCommand command =
                new GoogleChatCommand("  /SoLiCiTuDeS   abiertas  ");

        assertThat(command.isCommand()).isTrue();
        assertThat(command.getCommand()).isEqualTo("solicitudes");
        assertThat(command.getArguments()).isEqualTo("abiertas");
    }

    @Test
    void preservesArgumentsAfterTheFirstWhitespaceSeparator() {
        GoogleChatCommand command =
                new GoogleChatCommand("/solicitud  10000042 detalle");

        assertThat(command.getCommand()).isEqualTo("solicitud");
        assertThat(command.getArguments()).isEqualTo("10000042 detalle");
    }

    @Test
    void acceptsCommandWithoutArguments() {
        GoogleChatCommand command =
                new GoogleChatCommand("/solicitudes");

        assertThat(command.isCommand()).isTrue();
        assertThat(command.getArguments()).isEmpty();
    }

    @Test
    void rejectsPlainText() {
        GoogleChatCommand command =
                new GoogleChatCommand("solicitudes abiertas");

        assertThat(command.isCommand()).isFalse();
        assertThat(command.getCommand()).isNull();
        assertThat(command.getArguments()).isNull();
    }

    @Test
    void rejectsNullAndBlankText() {
        GoogleChatCommand nullCommand = new GoogleChatCommand(null);
        GoogleChatCommand blankCommand = new GoogleChatCommand("   ");

        assertThat(nullCommand.isCommand()).isFalse();
        assertThat(blankCommand.isCommand()).isFalse();
    }
}
