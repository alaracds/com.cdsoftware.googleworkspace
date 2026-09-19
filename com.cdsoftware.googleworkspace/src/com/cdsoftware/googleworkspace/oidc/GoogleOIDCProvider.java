package com.cdsoftware.googleworkspace.oidc;

import javax.ws.rs.container.ContainerRequestContext;

import org.compiere.model.MRole;
import org.compiere.model.MSession;
import org.compiere.model.MUser;
import org.compiere.model.Query;
import org.compiere.util.Env;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.trekglobal.idempiere.rest.api.model.MOIDCService;
import com.trekglobal.idempiere.rest.api.oidc.AbstractOIDCProvider;
import com.trekglobal.idempiere.rest.api.oidc.AuthenticatedUser;

public class GoogleOIDCProvider extends AbstractOIDCProvider {

    private static final String GOOGLE_SERVICE_EMAIL =
            "service-396259929666@gcp-sa-gsuiteaddons.iam.gserviceaccount.com";

    private static final String IDEMPIERE_USER = "gchat";

    private static final String IDEMPIERE_ROLE = "Web Service Execution";

    private static final int AD_ORG_ID = 11; // HQ

    public GoogleOIDCProvider() {
        System.out.println(">>> GoogleOIDCProvider CREADO <<<");
    }

    @Override
    public AuthenticatedUser getAuthenticatedUser(
            DecodedJWT decodedJwt,
            ContainerRequestContext requestContext,
            MOIDCService oidcService) {

        System.out.println(">>> GoogleOIDCProvider.getAuthenticatedUser() <<<");

        int AD_Client_ID = oidcService.getAD_Client_ID();

        /*
         * 1. Validar la identidad técnica enviada por Google.
         *
         * La firma, issuer, audience y expiración ya fueron
         * validados por MOIDCService antes de llegar aquí.
         */
        String email = decodedJwt.getClaim("email").asString();
        Boolean emailVerified =
                decodedJwt.getClaim("email_verified").asBoolean();

        if (!GOOGLE_SERVICE_EMAIL.equals(email)) {
            throw new JWTVerificationException(
                    "Invalid Google service account");
        }

        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new JWTVerificationException(
                    "Google service account email is not verified");
        }

        /*
         * 2. Buscar usuario técnico iDempiere.
         */
        MUser user = new Query(
                Env.getCtx(),
                MUser.Table_Name,
                "AD_Client_ID=? AND Value=?",
                null)
                .setOnlyActiveRecords(true)
                .setParameters(AD_Client_ID, IDEMPIERE_USER)
                .first();

        if (user == null) {
            throw new JWTVerificationException(
                    "iDempiere user gchat not found");
        }

        int AD_User_ID = user.getAD_User_ID();

        /*
         * 3. Buscar rol técnico.
         */
        MRole role = new Query(
                Env.getCtx(),
                MRole.Table_Name,
                "AD_Client_ID=? AND Name=?",
                null)
                .setOnlyActiveRecords(true)
                .setParameters(AD_Client_ID, IDEMPIERE_ROLE)
                .first();

        if (role == null) {
            throw new JWTVerificationException(
                    "iDempiere role not found");
        }

        int AD_Role_ID = role.getAD_Role_ID();

        /*
         * 4. Verificar que gchat realmente tenga asignado ese rol.
         */
        int userRoleId = getSingleRoleIDOnly(AD_Client_ID, user);

        if (userRoleId != AD_Role_ID) {
            throw new JWTVerificationException(
                    "Invalid role for gchat user");
        }

        /*
         * 5. Validar acceso del rol a HQ.
         */
        Env.setContext(
                Env.getCtx(),
                Env.AD_CLIENT_ID,
                AD_Client_ID);

        role.setAD_User_ID(AD_User_ID);

        if (!role.isOrgAccess(AD_ORG_ID, true)) {
            throw new JWTVerificationException(
                    "Role has no access to organization HQ");
        }

        /*
         * 6. Crear/obtener sesión iDempiere.
         */
        MSession session = MSession.get(Env.getCtx());

        if (session == null) {
            session = MSession.create(Env.getCtx());
            session.setWebSession("idempiere-rest-oidc");
            session.saveEx();
        }

        System.out.println(
                ">>> Google autenticado como iDempiere <<<");
        System.out.println("Client: " + AD_Client_ID);
        System.out.println("Org: " + AD_ORG_ID);
        System.out.println("Role: " + AD_Role_ID);
        System.out.println("User: " + AD_User_ID);
        System.out.println(
                "Session: " + session.getAD_Session_ID());

        /*
         * 7. Entregar identidad autenticada a idempiere-rest.
         */
        return new AuthenticatedUser(
                AD_Client_ID,
                AD_ORG_ID,
                AD_Role_ID,
                AD_User_ID,
                session.getAD_Session_ID());
    }
}