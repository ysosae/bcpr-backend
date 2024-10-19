package config;


import com.amazonaws.services.cloudfront.model.NoSuchDistributionException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.DistributionList;
import software.amazon.awssdk.services.cloudfront.model.DistributionSummary;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsRequest;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsResponse;
import software.amazon.awssdk.services.cloudfront.model.Origin;

public class ImageDomainAWS {
  private static final Logger log = Logger.getLogger(ImageDomainAWS.class);


   public static String getIconImageDomain(CloudFrontClient cloudFrontClient, String domainName) {
    String domainNamePublic = "";
    try {
      List<DistributionSummary> distributionSummaries = getDistributionSummaries(cloudFrontClient);
      domainNamePublic = findDomainNamePublic(distributionSummaries, domainName);
      Assert.assertNotNull(searchDomainNamePublic(distributionSummaries, domainNamePublic));
      domainNamePublic = searchAlternativeDomainNamePublic(distributionSummaries, domainNamePublic);

    } catch (Exception e) {
      log.error("Error retrieving icon image domain: {} " + e.getMessage());
    }

    return domainNamePublic;
  }

  public static String searchAlternativeDomainNamePublic(List<DistributionSummary> distributionSummaries, String domainNamePublic){
    String alternativeDomainNamePublic = "";
    try {
      if (distributionSummaries.isEmpty()) {
        log.warn("No distribution summaries found.");
        return domainNamePublic;
      }

      if (StringUtils.isEmpty(domainNamePublic)) {
        log.warn("No matching domain name found in distribution summaries.");
        return domainNamePublic;
      }

      for (DistributionSummary distributionSummary : distributionSummaries) {
        for (Origin origin : distributionSummary.origins().items()) {
          if (StringUtils.containsIgnoreCase(origin.domainName(), domainNamePublic)) {
            alternativeDomainNamePublic = distributionSummary.aliases().items().get(0);
            log.info("Found matching domain name: {}" + alternativeDomainNamePublic);
            return alternativeDomainNamePublic;
          }
        }
      }
    } catch (Exception e) {
      log.error("Error retrieving icon image domain: {} " + e.getMessage());
    }

    return alternativeDomainNamePublic;
  }

  public static String searchDomainNamePublic(List<DistributionSummary> distributionSummaries, String domainNamePublic){
   try {
     if (distributionSummaries.isEmpty()) {
        log.warn("No distribution summaries found.");
        return domainNamePublic;
      }

      if (StringUtils.isEmpty(domainNamePublic)) {
        log.warn("No matching domain name found in distribution summaries.");
        return domainNamePublic;
      }

      for (DistributionSummary distributionSummary : distributionSummaries) {
        for (Origin origin : distributionSummary.origins().items()) {
          if (StringUtils.containsIgnoreCase(origin.domainName(), domainNamePublic)) {
            domainNamePublic = distributionSummary.domainName();
            log.info("Found matching domain name: {}" + domainNamePublic);
            return domainNamePublic;
          }
        }
      }
    } catch (Exception e) {
      log.error("Error retrieving icon image domain: {} " + e.getMessage());
    }

    return domainNamePublic;
  }

  private static List<DistributionSummary> getDistributionSummaries(CloudFrontClient cloudFrontClient) {
    List<DistributionSummary> distributionSummaries = new ArrayList<>();
    try {
      ListDistributionsRequest request = ListDistributionsRequest.builder().build();
      ListDistributionsResponse response = cloudFrontClient.listDistributions(request);
      DistributionList distributionList = response.distributionList();

      if (distributionList.hasItems()) {
        distributionSummaries.addAll(distributionList.items());
      }
    } catch (NoSuchDistributionException e) {
      log.error("No such distribution: {} " + e.getMessage());
    }

    return distributionSummaries;
  }

  private static String findDomainNamePublic(List<DistributionSummary> distributionSummaries, String domainName) {
    for (DistributionSummary distributionSummary : distributionSummaries) {
      for (Origin origin : distributionSummary.origins().items()) {
        if (StringUtils.containsIgnoreCase(origin.domainName(), domainName)) {
          log.info("Matching origin domain found: {} " + origin.domainName());
          return origin.domainName();
        }
      }
    }
    return "";
  }
}


