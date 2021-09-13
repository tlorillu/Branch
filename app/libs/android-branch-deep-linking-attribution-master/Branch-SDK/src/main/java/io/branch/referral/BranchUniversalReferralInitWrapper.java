package io.branch.referral;

import org.json.JSONObject;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.LinkProperties;

/**
 * Class for converting init session callback with {@link Branch.BranchReferralInitListener} to {@link Branch.BranchUniversalReferralInitListener}
 */
class BranchUniversalReferralInitWrapper implements Branch.BranchReferralInitListener {
    private final Branch.BranchUniversalReferralInitListener universalReferralInitListener_;

    public BranchUniversalReferralInitWrapper(Branch.BranchUniversalReferralInitListener universalReferralInitListener) {
        this.universalReferralInitListener_ = universalReferralInitListener;
    }

    @Override
    public void onInitFinished(JSONObject referringParams, BranchError error) {
        if (universalReferralInitListener_ != null) {
            if (error != null) {
                universalReferralInitListener_.onInitFinished(null, null, error);
            } else {
                BranchUniversalObject branchUniversalObject = BranchUniversalObject.getReferredBranchUniversalObject();
                LinkProperties linkProperties = LinkProperties.getReferredLinkProperties();
                universalReferralInitListener_.onInitFinished(branchUniversalObject, linkProperties, error);
            }
        }
    }
}