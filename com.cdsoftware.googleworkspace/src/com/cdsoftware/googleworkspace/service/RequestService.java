package com.cdsoftware.googleworkspace.service;

import java.util.List;
import java.util.Properties;

import org.compiere.model.MRequest;
import org.compiere.model.MRequestUpdate;
import org.compiere.model.Query;

public class RequestService {

    public enum StatusFilter {
        OPEN,
        CLOSED,
        ALL
    }

    public List<MRequest> findRequests(
            Properties ctx,
            int cBPartnerId,
            StatusFilter statusFilter,
            int limit) {

        String whereClause = "C_BPartner_ID=?";

        switch (statusFilter) {

            case OPEN:
                whereClause +=
                        " AND R_Status_ID IN ("
                        + "SELECT R_Status_ID "
                        + "FROM R_Status "
                        + "WHERE IsOpen='Y'"
                        + ")";
                break;

            case CLOSED:
                whereClause +=
                        " AND R_Status_ID IN ("
                        + "SELECT R_Status_ID "
                        + "FROM R_Status "
                        + "WHERE IsClosed='Y'"
                        + ")";
                break;

            case ALL:
                break;
        }

        return new Query(
                ctx,
                MRequest.Table_Name,
                whereClause,
                null)
                .setClient_ID()
                .setOnlyActiveRecords(true)
                .setApplyAccessFilter(true)
                .setParameters(cBPartnerId)
                .setOrderBy("Created DESC")
                .setPageSize(limit)
                .list();
    }

    public MRequest findRequest(
            Properties ctx,
            int cBPartnerId,
            String documentNo) {

        return new Query(
                ctx,
                MRequest.Table_Name,
                "C_BPartner_ID=? AND DocumentNo=?",
                null)
                .setClient_ID()
                .setOnlyActiveRecords(true)
                .setApplyAccessFilter(true)
                .setParameters(
                        cBPartnerId,
                        documentNo)
                .first();
    }

    public MRequest findRequest(
            Properties ctx,
            int cBPartnerId,
            int requestId) {

        return new Query(
                ctx,
                MRequest.Table_Name,
                "C_BPartner_ID=? AND R_Request_ID=?",
                null)
                .setClient_ID()
                .setOnlyActiveRecords(true)
                .setApplyAccessFilter(true)
                .setParameters(cBPartnerId, requestId)
                .first();
    }

    public List<MRequestUpdate> findUpdates(
            Properties ctx,
            int requestId,
            int limit) {

        return new Query(
                ctx,
                MRequestUpdate.Table_Name,
                "R_Request_ID=?",
                null)
                .setClient_ID()
                .setOnlyActiveRecords(true)
                .setApplyAccessFilter(true)
                .setParameters(requestId)
                .setOrderBy("Created DESC")
                .setPageSize(limit)
                .list();
    }
}
