package com.cdsoftware.googleworkspace.chat;

import java.util.Properties;

import org.compiere.util.Env;

public class GoogleChatExecutionContext {

    private final int adUserId;
    private final int adRoleId;
    private final Properties ctx;

    public GoogleChatExecutionContext(
            int adUserId,
            int adRoleId) {

        this.adUserId = adUserId;
        this.adRoleId = adRoleId;

        this.ctx = new Properties();
        this.ctx.putAll(Env.getCtx());

        Env.setContext(
                this.ctx,
                Env.AD_USER_ID,
                adUserId);

        Env.setContext(
                this.ctx,
                Env.AD_ROLE_ID,
                adRoleId);
    }

    public int getAD_User_ID() {
        return adUserId;
    }

    public int getAD_Role_ID() {
        return adRoleId;
    }

    public Properties getCtx() {
        return ctx;
    }
}
