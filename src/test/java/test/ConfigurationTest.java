package test;

import static config.EnvironmentPipelineAWS.getBranchName;
import static config.RestAssuredExtension.generateAWSHeadersCredentials;
import static test.EnvBCPRTest.cloudFrontClient;
import static test.EnvBCPRTest.getDnsNameLoadBalancer;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import config.AbstractAPI;
import config.ResourcesAWS;
import config.RestAssuredExtension;
import config.RestAssuredPropertiesConfig;
import config.ServicesClientAWS;
import enums.ResourceAWS;
import features.commons.CommonsStepDefs;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.model.DistributionList;
import software.amazon.awssdk.services.cloudfront.model.DistributionSummary;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsRequest;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsResponse;
import software.amazon.awssdk.services.cloudfront.model.Origin;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ElasticLoadBalancingV2Exception;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetSessionTokenRequest;
import software.amazon.awssdk.services.sts.model.GetSessionTokenResponse;
import software.amazon.awssdk.services.sts.model.StsException;

import java.util.Optional;

public class ConfigurationTest extends AbstractAPI {
    private static final Logger log = Logger.getLogger(ConfigurationTest.class);
    public static ResponseOptions<Response> response = null;
    public static RestAssuredExtension rest = new RestAssuredExtension();
    public static ServicesClientAWS servicesClientAWS = new ServicesClientAWS();
    public static ElasticLoadBalancingV2Client elasticLoadBalancingV2Client =
            servicesClientAWS.getElasticLoadBalancingV2Client();

    @Test
    public void testGenerateAWSHeadersCredentials() {
        generateAWSHeadersCredentials();
    }


    @Ignore
    @Test
    public void GetSessionToken() {
        Region region = Region.US_EAST_1;
        StsClient stsClient =
                StsClient.builder()
                        .region(region)
                        .credentialsProvider(ProfileCredentialsProvider.create())
                        .build();
        getToken(stsClient);
        stsClient.close();
    }

    public static void getToken(StsClient stsClient) {
        try {
            GetSessionTokenRequest tokenRequest =
                    GetSessionTokenRequest.builder().durationSeconds(1500).build();

            GetSessionTokenResponse session_token_result = stsClient.getSessionToken(tokenRequest);
            log.info("The token value is " + session_token_result.credentials().sessionToken());

        } catch (StsException e) {
            log.error(e.getMessage());
            System.exit(1);
        }
    }


    @Test
    public void DecodeIdToken() {
        String idToken =
                "eyJraWQiOiJPd29hcitWSzZIMXZsclwveXAwbllkOG5sMW1UenNoTnRlRk5VOUQzUHoxST0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJkOGE0NjY0Mi02YWZmLTQwYmItOGYzMy0wYTFjNzVhYzRjZjAiLCJjb2duaXRvOmdyb3VwcyI6WyJhZG1pbnMtZ3JvdXAiXSwiY29nbml0bzpwcmVmZXJyZWRfcm9sZSI6ImFybjphd3M6aWFtOjo4NjQ5NjEzNTY4ODY6cm9sZVwvQWRtaW5zVFNUUm9sZSIsImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC51cy1lYXN0LTEuYW1hem9uYXdzLmNvbVwvdXMtZWFzdC0xX2F4TkRTRk5XNSIsImNvZ25pdG86dXNlcm5hbWUiOiJ5dWxpZXRzb3NhLXVhdCIsIm9yaWdpbl9qdGkiOiI0ZmY4ZWJjMi1jZmRlLTRlZmQtYWVjZC1jZmEyZjgxNDA5ZjgiLCJjb2duaXRvOnJvbGVzIjpbImFybjphd3M6aWFtOjo4NjQ5NjEzNTY4ODY6cm9sZVwvQWRtaW5zVFNUUm9sZSJdLCJhdWQiOiI2b2hvcGI0OXA2dTdxZzNwbzk0djJoZXE0NyIsImV2ZW50X2lkIjoiMGY5YmJkMDgtYTgxMi00ZjMxLWEwMTYtYTliZWQ2NTdiODMyIiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2ODAwMjczNDQsImV4cCI6MTY4MDAzMDk0NCwiaWF0IjoxNjgwMDI3MzQ0LCJqdGkiOiJjMmVhMDJlOS05ZjBlLTRiZjItOTRmNi0zMjgxNzE4NDliMGMifQ.FZBKXyqTed0BTEuMR6PxNg8mWBpmRu5s_XVejE9BTlLYzRfii_vUWr0KvS_a-1h0NWIT-7_n90VBKKavRwDOS9C9WP2FWOzqKy466Lg6sQGBO1sl1XLWnCBtqQrzcqvrczrXUH3GLkKDRse1LljPZlJC46RBkoCn-lRMqiPYnPyVFuOtU_eSMYtnU6eqy75wZmA0rXz3WdiK_Bz0WcogUAiiw1PW9lIzIfsnqem4iYkD8CS132cvdn-neehCZvaD-ICXYUxw_vu4zAhpNwA8fm8w54OheSVwsfJ4f_mMqYJRTnI3FfPeSRIpvg19EMEl3in6GGSDymPCwMPsO1IWcQ";
        DecodedJWT jwt = JWT.decode(idToken);
        String userId = jwt.getSubject();
        String username = jwt.getClaim("cognito:username").asString();
        String userRole = jwt.getClaim("cognito:preferred_role").asString();
        log.info("ID: " + userId);
        log.info("Username: " + username);
        log.info("Roles: " + userRole);
    }

    @Test
    public void getPublicImageDomainCloudFront() {
        try {
            ListDistributionsResponse listDistributionsResponse = cloudFrontClient.listDistributions();
            DistributionList distributionList = listDistributionsResponse.distributionList();

            if (distributionList.hasItems()) {
                Optional<String> publicImageDomain = findPublicImageDomain(distributionList);
                publicImageDomain.ifPresent(log::info);
            }
        } catch (Exception e) {
            throw new SkipException(e.getMessage());
        }
    }

    private Optional<String> findPublicImageDomain(DistributionList distributionList) {
        return distributionList.items().stream()
                .flatMap(distribution -> distribution.origins().items().stream())
                .filter(origin -> StringUtils.containsIgnoreCase(origin.domainName(), ResourcesAWS.buildBucketsName(ResourceAWS.image)))
                .map(Origin::domainName)
                .findFirst();
    }


    @Test
    public void getIconImageDomainCloudFront() {
        String iconImagenDomain;
        try {
            ListDistributionsRequest request = ListDistributionsRequest.builder().build();
            ListDistributionsResponse listDistributionsResponse =
                    cloudFrontClient.listDistributions(request);
            DistributionList listDistribution = listDistributionsResponse.distributionList();

            if (listDistribution.hasItems()) {
                for (DistributionSummary distributionSummary : listDistribution.items()) {
                    for (Origin origin : distributionSummary.origins().items()) {
                        if (StringUtils.containsIgnoreCase(origin.domainName(),
                                ResourcesAWS.buildBucketsName(ResourceAWS.claims))) {
                            iconImagenDomain = distributionSummary.domainName();
                            log.info(iconImagenDomain);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new SkipException(e.getMessage());
        }
    }

    @Test
    public void getEndpointInternalMicroserviceLoadBalancer() {
        String expectedDnsName = getDnsNameLoadBalancer();

        try {
            DescribeLoadBalancersResponse loadBalancerList = elasticLoadBalancingV2Client.describeLoadBalancers();

            if (!loadBalancerList.hasLoadBalancers()) {
                throw new SkipException("No load balancers found");
            }

            log.info("DNS Names of Load Balancers:");
            boolean foundMatchingLoadBalancer = loadBalancerList.loadBalancers().stream()
                    .filter(lb -> StringUtils.equalsIgnoreCase(lb.dnsName(), expectedDnsName))
                    .peek(lb -> log.info(lb.dnsName()))
                    .findAny()
                    .isPresent();

            Assert.assertTrue(foundMatchingLoadBalancer, "No load balancer found with the expected DNS name");
        } catch (ElasticLoadBalancingV2Exception e) {
            throw new SkipException("Error describing load balancers: " + e.getMessage());
        }
    }


    @Test
    public void getBranchNameFrontendAPI() {
        getBranchName();
    }

    @DataProvider(name = "status")
    public Object[][] status() {
        return new Object[][]{
                {"isCreditCardExpiredTrue"}

        };
    }

    @Test(dataProvider = "status")
    public void testGetUserSessionByCardStatus(String status) {
        CommonsStepDefs commonsStepDefs = new CommonsStepDefs();
        commonsStepDefs.setAsMainTestUser(status);
    }

    @Test(
            groups = {"ConfigurationTest"},
            testName = "BCPRXRP-1023-GetEvironmentTurnOnOffEmail")
    public void testGetEnvironmentUseSqsAsNotification() {
        boolean useSqsAsNotificationChannel = RestAssuredPropertiesConfig.useSqsAsNotificationChannel;
        Assert.assertTrue(useSqsAsNotificationChannel);
    }

    @Test
    public void validateShortTimeCondition() {
        response = postMethodGraphQL("graphQL/getListWalletAccounts.graphql");
        boolean isDataResponse = response.getBody().jsonPath().get("data.listWalletAccounts[0]") != null;
        shortWait(5, !isDataResponse);
    }


    @Test
    public void validateShortTime() {
        shortWait(60);
    }

}

