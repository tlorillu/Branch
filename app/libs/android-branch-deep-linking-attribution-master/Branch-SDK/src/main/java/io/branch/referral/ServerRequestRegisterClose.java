package io.branch.referral;

import android.app.Application;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * * <p>
 * The server request for closing any open session. Handles request creation and execution.
 * </p>
 */
class ServerRequestRegisterClose extends ServerRequest {
    
    /**
     * <p>Perform the state-safe actions required to terminate any open session, and report the
     * closed application event to the Branch API.</p>
     *
     * @param context Current {@link Application} context
     */
    public ServerRequestRegisterClose(Context context) {
        super(context, Defines.RequestPath.RegisterClose);
        JSONObject closePost = new JSONObject();
        try {
            closePost.put(Defines.Jsonkey.DeviceFingerprintID.getKey(), prefHelper_.getDeviceFingerPrintID());
            closePost.put(Defines.Jsonkey.IdentityID.getKey(), prefHelper_.getIdentityID());
            closePost.put(Defines.Jsonkey.SessionID.getKey(), prefHelper_.getSessionID());
            if (!prefHelper_.getLinkClickID().equals(PrefHelper.NO_STRING_VALUE)) {
                closePost.put(Defines.Jsonkey.LinkClickID.getKey(), prefHelper_.getLinkClickID());
            }
            if (DeviceInfo.getInstance() != null) {
                closePost.put(Defines.Jsonkey.AppVersion.getKey(), DeviceInfo.getInstance().getAppVersion());
            }
            setPost(closePost);
        } catch (JSONException ex) {
            ex.printStackTrace();
            constructError_ = true;
        }
    }
    
    public ServerRequestRegisterClose(Defines.RequestPath requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }
    
    @Override
    public boolean handleErrors(Context context) {
        if (!super.doesAppHasInternetPermission(context)) {
            return true;
        }
        return false;
    }
    
    @Override
    public void onRequestSucceeded(ServerResponse resp, Branch branch) {
        // Clear the latest session params on close
        prefHelper_.setSessionParams(PrefHelper.NO_STRING_VALUE);
    }
    
    @Override
    public void handleFailure(int statusCode, String causeMsg) {
        //No implementation on purpose
    }
    
    @Override
    public boolean isGetRequest() {
        return false;
    }
    
    @Override
    public void clearCallbacks() {
        //No implementation on purpose
    }
    
    @Override
    boolean isPersistable() {
        return false; // No need to retrieve close from previous session
    }
}
