package com.cdsoftware.googleworkspace.chat;

import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.Env;

public class GoogleChatUserService {

    public PO findByGoogleUserId(String googleUserId) {

        if (googleUserId == null || googleUserId.isBlank()) {
            return null;
        }

        return new Query(
                Env.getCtx(),
                "CDS_GChatUser",
                "GoogleUserID=?",
                null)
                .setClient_ID()
                .setOnlyActiveRecords(true)
                .setParameters(googleUserId)
                .first();
    }
}