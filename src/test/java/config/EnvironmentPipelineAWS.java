package config;

import static config.ResourcesAWS.servicesClientAWS;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import software.amazon.awssdk.services.codepipeline.CodePipelineClient;
import software.amazon.awssdk.services.codepipeline.model.ActionState;
import software.amazon.awssdk.services.codepipeline.model.GetPipelineStateRequest;
import software.amazon.awssdk.services.codepipeline.model.GetPipelineStateResponse;
import software.amazon.awssdk.services.codepipeline.model.PipelineSummary;
import software.amazon.awssdk.services.codepipeline.model.StageState;

public class EnvironmentPipelineAWS {
  private static final Logger log = Logger.getLogger(EnvironmentPipelineAWS.class);
  public static CodePipelineClient codePipelineClient = servicesClientAWS.getCodePipelineClient();

  public static List<String>
    environments = Arrays.asList("BCPR-DOP-API", "BCPR-DEV-API", "BCPR-TST-API", "BCPR-QA-API");
  public static List<String> pipelines = Arrays.asList(
    "BCPR-DOP-Api-Pipeline-Pipeline9850B417-1SJDFE0KBQRVR",
    "BCPR-DEV-Api-Pipeline-Pipeline9850B417-1GQQD3EUR0JDZ",
    "BCPR-TST-Api-Pipeline-Pipeline9850B417-PE1SKBULSH34",
    "BCPR-QA-Api-Pipeline-Pipeline9850B417-LUAAK6PQLSJM"
  );


  public static String getBranchName() {
    AtomicReference<String> branchName = new AtomicReference<>("");
    List<PipelineSummary> pipelineContextList;
    for (int i = 0; i < environments.size(); i++) {
      String environment = environments.get(i);
      String pipelineName = pipelines.get(i);
      log.info("Ambiente " + environment + ":");

      GetPipelineStateRequest pipelineStateRequest = GetPipelineStateRequest.builder()
        .name(pipelineName)
        .build();

      GetPipelineStateResponse pipelineStateResponse =
        codePipelineClient.getPipelineState(pipelineStateRequest);

      for (StageState stageState : pipelineStateResponse.stageStates()) {
        if ("Deploy".equalsIgnoreCase(stageState.stageName())) {
          for (ActionState actionState : stageState.actionStates()) {
            if (actionState.latestExecution() != null &&
              actionState.latestExecution().externalExecutionId() != null) {
              actionState.latestExecution().externalExecutionId();
              System.out.println("BranchName: " + branchName);
            }
          }
        }
      }

      if (codePipelineClient.listPipelines().hasPipelines()) {
        pipelineContextList = codePipelineClient.listPipelines().pipelines();
        for (PipelineSummary pipelineSummery :
          pipelineContextList) {
          if (StringUtils.equalsIgnoreCase(pipelineSummery.name(), pipelineName)) {

          }
        }
      }


    }
    return branchName.get();
  }
}

