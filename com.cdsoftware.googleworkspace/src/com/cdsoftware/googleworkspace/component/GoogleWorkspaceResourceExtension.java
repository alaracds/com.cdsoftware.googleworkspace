package com.cdsoftware.googleworkspace.component;

import java.util.Set;

import com.cdsoftware.googleworkspace.rest.GoogleChatResource;
import com.trekglobal.idempiere.rest.api.ResourceExtension;

public class GoogleWorkspaceResourceExtension
        implements ResourceExtension {

    public GoogleWorkspaceResourceExtension() {
        System.out.println(
            ">>> GOOGLE WORKSPACE ResourceExtension CREADO <<<"
        );
    }

    @Override
    public Set<Class<?>> getResourceClasses() {

        System.out.println(">>> Registrando GoogleChatResource <<<");
        System.out.println(">>> Clase: " + GoogleChatResource.class.getName());

        for (var annotation : GoogleChatResource.class.getAnnotations()) {
            System.out.println(">>> Anotacion: " + annotation);
        }

        return Set.of(GoogleChatResource.class);
    }
}