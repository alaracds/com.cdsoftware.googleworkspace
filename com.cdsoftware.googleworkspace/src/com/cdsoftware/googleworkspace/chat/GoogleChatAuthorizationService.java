package com.cdsoftware.googleworkspace.chat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.compiere.model.MRequest;
import org.compiere.model.MRole;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class GoogleChatAuthorizationService {

    /**
     * Returns the active roles assigned to the user that can read R_Request.
     *
     * The access check follows idempiere-rest's RestUtils.hasAccess policy:
     * access through an active window that contains the table, with table
     * access as fallback.
     *
     * @param adUserId AD_User_ID to evaluate
     * @return roles with read access to R_Request
     */
    public List<MRole> getRequestReadRoles(int adUserId) {

        List<MRole> assignedRoles = getUserRoles(adUserId);
        List<Integer> requestWindowIds = getWindowIds(MRequest.Table_ID);
        List<MRole> accessibleRoles = new ArrayList<>();

        for (MRole role : assignedRoles) {
            if (role.isUseUserOrgAccess()) {
                role.setAD_User_ID(adUserId);
            }

            if (hasReadAccess(role, MRequest.Table_ID, requestWindowIds)) {
                accessibleRoles.add(role);
            }
        }

        return accessibleRoles;
    }

    private List<MRole> getUserRoles(int adUserId) {

        return new Query(
                Env.getCtx(),
                MRole.Table_Name,
                "EXISTS ("
                + "SELECT 1 "
                + "FROM AD_User_Roles ur "
                + "WHERE ur.AD_Role_ID=AD_Role.AD_Role_ID "
                + "AND ur.AD_User_ID=? "
                + "AND ur.IsActive='Y'"
                + ")",
                null)
                .setClient_ID()
                .setOnlyActiveRecords(true)
                .setParameters(adUserId)
                .list();
    }

    private boolean hasReadAccess(
            MRole role,
            int tableId,
            List<Integer> windowIds) {

        for (int windowId : windowIds) {
            if (role.getWindowAccess(windowId) != null) {
                return true;
            }
        }

        return role.isTableAccess(tableId, true);
    }

    private List<Integer> getWindowIds(int tableId) {

        String sql = "SELECT DISTINCT w.AD_Window_ID "
                + "FROM AD_Window w "
                + "INNER JOIN AD_Tab t ON (t.AD_Window_ID=w.AD_Window_ID) "
                + "WHERE w.IsActive='Y' "
                + "AND t.IsActive='Y' "
                + "AND t.AD_Table_ID=?";

        List<Integer> windowIds = new ArrayList<>();

        try (PreparedStatement statement = DB.prepareStatement(sql, null)) {
            statement.setInt(1, tableId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    windowIds.add(resultSet.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not evaluate window access for table " + tableId,
                    exception);
        }

        return windowIds;
    }
}
