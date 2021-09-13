package io.branch.referral;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.branch.referral.Defines.PreinstallKey;
import io.branch.referral.utils.AssetUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BranchPreinstallFileTest extends BranchTest {

    @Test
    public void testResultSuccess() {
        initBranchInstance();

        final ServerRequestQueue queue = ServerRequestQueue.getInstance(getTestContext());
        Assert.assertEquals(0, queue.getSize());
        initSessionResumeActivity(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(1, queue.getSize());

                String branchFileData = AssetUtils.readJsonFile(getTestContext(), "pre_install_apps.branch");
                Assert.assertTrue(branchFileData.length() > 0);

                JSONObject branchFileJson = null;
                try {
                    branchFileJson = new JSONObject(branchFileData);
                    BranchPreinstall.getBranchFileContent(branchFileJson, branch, getTestContext());

                    ServerRequest initRequest = queue.peekAt(0);
                    doFinalUpdate(initRequest);
                    doFinalUpdateOnMainThread(initRequest);

                    Assert.assertTrue(hasV1InstallPreinstallCampaign(initRequest));
                    Assert.assertTrue(hasV1InstallPreinstallPartner(initRequest));
                    Assert.assertTrue(hasV1InstallPreinstallCustomData(initRequest));
                } catch (Exception e) {
                    Assert.fail("parsing of test resources failed");
                }
            }
        }, null);
    }

    @Test
    public void testResultNullFile() {
        String branchFileData = AssetUtils
                .readJsonFile(getTestContext(), "pre_install_apps_null.branch");
        Assert.assertFalse(branchFileData.length() > 0);
    }

    @Test
    public void testResultPackageNameNotPresent() {
        initBranchInstance();

        final ServerRequestQueue queue = ServerRequestQueue.getInstance(getTestContext());
        Assert.assertEquals(0, queue.getSize());
        initSessionResumeActivity(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(1, queue.getSize());

                String branchFileData = AssetUtils
                        .readJsonFile(getTestContext(), "pre_install_apps_no_package.branch");
                Assert.assertTrue(branchFileData.length() > 0);
                try {
                    JSONObject branchFileJson = new JSONObject(branchFileData);
                    BranchPreinstall.getBranchFileContent(branchFileJson, branch,
                            getTestContext());

                    ServerRequest initRequest = queue.peekAt(0);
                    doFinalUpdate(initRequest);
                    doFinalUpdateOnMainThread(initRequest);

                    Assert.assertFalse(hasV1InstallPreinstallCampaign(initRequest));
                    Assert.assertFalse(hasV1InstallPreinstallPartner(initRequest));
                    Assert.assertFalse(hasV1InstallPreinstallCustomData(initRequest));
                } catch (JSONException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }, null);
    }

    @Test
    public void testResultFileNotPresent() {
        String branchFileData = AssetUtils.readJsonFile(getTestContext(), "pre_install_apps_not_present.branch");
        Assert.assertFalse(branchFileData.length() > 0);
    }

    @Test
    public void testAppLevelDataOverride() {
        initBranchInstance();
        branch.setPreinstallPartner("partner1");
        branch.setPreinstallCampaign("campaign1");

        final ServerRequestQueue queue = ServerRequestQueue.getInstance(getTestContext());
        Assert.assertEquals(0, queue.getSize());
        initSessionResumeActivity(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(1, queue.getSize());

                String branchFileData = AssetUtils.readJsonFile(getTestContext(), "pre_install_apps.branch");
                Assert.assertTrue(branchFileData.length() > 0);
                try {
                    JSONObject branchFileJson = new JSONObject(branchFileData);
                    BranchPreinstall.getBranchFileContent(branchFileJson, branch, getTestContext());

                    ServerRequest initRequest = queue.peekAt(0);
                    doFinalUpdate(initRequest);
                    doFinalUpdateOnMainThread(initRequest);

                    Assert.assertTrue(hasV1InstallPreinstallCampaign(initRequest));
                    Assert.assertTrue(hasV1InstallPreinstallPartner(initRequest));
                    Assert.assertTrue(hasV1InstallPreinstallCustomData(initRequest));
                    Assert.assertEquals(getPreinstallData(branch, PreinstallKey.partner.getKey()), "partner1");
                } catch (JSONException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }, null);
    }

    // Check to see if the preinstall campaign is available (V1)
    private boolean hasV1InstallPreinstallCampaign(ServerRequest request) {
        JSONObject jsonObject = request.getGetParams();
        String preinstallCampaign = jsonObject.optString(PreinstallKey.partner.getKey());
        return (preinstallCampaign.length() > 0);
    }

    // Check to see if the preinstall partner is available (V1)
    private boolean hasV1InstallPreinstallPartner(ServerRequest request) {
        JSONObject jsonObject = request.getGetParams();
        String preinstallPartner = jsonObject.optString(PreinstallKey.partner.getKey());
        return (preinstallPartner.length() > 0);
    }

    // Check to see if the preinstall custom data is available (V1)
    private boolean hasV1InstallPreinstallCustomData(ServerRequest request) throws JSONException {
        JSONObject jsonObject = request.getGetParams().getJSONObject("metadata");
        String custom_key = jsonObject.optString("custom_key");
        return (custom_key.length() > 0);
    }

    // Check to see if the preinstall data from the app overrides the data from the system file
    private String getPreinstallData(Branch branch, String key) {
        return PrefHelper.getInstance(getTestContext()).getInstallMetaData(key);
    }
}
