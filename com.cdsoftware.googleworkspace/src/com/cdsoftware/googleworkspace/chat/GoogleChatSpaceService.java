package com.cdsoftware.googleworkspace.chat;

import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.Env;

public class GoogleChatSpaceService {

    public PO findByGoogleSpaceId(String googleSpaceId) {

        return new Query(
                Env.getCtx(),
                "CDS_GChatSpace",
                "GoogleSpaceID=?",
                null)
                .setOnlyActiveRecords(true)
                .setParameters(googleSpaceId)
                .first();
    }
}
