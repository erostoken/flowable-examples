package org.flowable.examples.spring.boot.service;

import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * code 生成流程 对应执行服务类
 *
 * @author xuwentao
 * @date 2024/12/12
 * @since 2.0.0
 */
@Slf4j
@Service
public class SimpleCodeProcessService {

    /**
     * 管理与控制部署(deployments)与流程定义(process definitions)的操作
     * <br/> - 部署上传定义文件xml至引擎, 部署包中的所有流程都可以启动。
     * <br/> - 查询引擎现有的部署与流程定义。
     * <br/> - 暂停或激活部署中的某些流程，或整个部署。
     * <br/> - 获取各种资源，比如部署中保存的文件，或者引擎自动生成的流程图。
     * <br/> - 获取POJO版本的流程定义。它可以用Java而不是XML的方式查看流程。
     */
    @Resource
    private RepositoryService repositoryService;
    /**
     * 运行服务
     */
    @Resource
    private RuntimeService runtimeService;
    /**
     * 参与操作的任务
     * <br/> - 查询分派给用户或组的任务
     * <br/> - 创建独立运行(standalone)任务。这是一种没有关联到流程实例的任务。
     * <br/> - 决定任务的执行用户(assignee)，或者将用户通过某种方式与任务关联。
     * <br/> - 认领(claim)任务。认领是指某人决定成为任务的执行用户，也即他将会完成这个任务。
     * <br/> - 完成(complete)任务是指“做这个任务要求的工作”，通常是填写某个表单。
     */
    @Resource
    private TaskService taskService;

    /**
     * 动态创建审批流程模型
     *
     * simple-return-process.bpmn20.xml 翻译
     */
    public void deploySimpleReturnProcess() {
        // 创建流程模型
        BpmnModel bpmnModel = new BpmnModel();

        // 定义开始事件
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");

        // 定义申请人任务
        UserTask applyTask = new UserTask();
        applyTask.setId("submitRequestTask");
        applyTask.setName("Submit Request");
        applyTask.setAssignee("${initiator}");

        // 定义审批人任务
        UserTask approveTask = new UserTask();
        approveTask.setId("approveRequestTask");
        approveTask.setName("Approve Request");
        approveTask.setAssignee("${approver}");

        // 定义结束事件
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");

        // 定义流程中的流转
        SequenceFlow startToApply = new SequenceFlow("startEvent", "submitRequestTask");
        SequenceFlow applyToApprove = new SequenceFlow("submitRequestTask", "approveRequestTask");
        SequenceFlow approveToEnd = new SequenceFlow("approveRequestTask", "endEvent");
        SequenceFlow approveToApply = new SequenceFlow("approveRequestTask", "submitRequestTask"); // 驳回

        // 设置条件流转
        approveToEnd.setConditionExpression("${approvalDecision == 'approve'}");
        approveToApply.setConditionExpression("${approvalDecision == 'reject'}");

        // 构建流程图
        Process process = new Process();
        process.setId("simpleReturnProcessV2");
        process.addFlowElement(startEvent);
        process.addFlowElement(applyTask);
        process.addFlowElement(approveTask);
        process.addFlowElement(endEvent);
        process.addFlowElement(startToApply);
        process.addFlowElement(applyToApprove);
        process.addFlowElement(approveToEnd);
        process.addFlowElement(approveToApply);

        bpmnModel.addProcess(process);

        // 部署流程
        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel("simple-return-process-v2.bpmn20.xml", bpmnModel)
                .name("Leave Request Process")
                .deploy();

        System.out.println("Deployment ID: " + deployment.getId());
    }

    /**
     * 开始一个任务流，指定发起人和审批人
     *
     * @param initiator 发起人
     * @param approver  审批人
     */
    public void submitApplication(String initiator, String approver) {
        // simple-return-process-v2.bpmn20.xml文件 flowable:assignee 指定了关键key initiator 和 approver
        Map<String, Object> variables = new HashMap<>();
        variables.put("initiator", initiator);
        variables.put("approver", approver);

        // 指定审批流程key - simple-return-process.bpmn20.xml文件中 process id所指定
        runtimeService.startProcessInstanceByKey("simpleReturnProcessV2", variables);
    }

    /**
     * 查询用户任务
     *
     * @param userId 用户唯一标识
     * @return 用户任务列表
     */
    public List<Task> getUserTasks(String userId) {
        List<Task> list = taskService.createTaskQuery().taskAssignee(userId).list();
        for (Task task : list) {
            log.info("SimpleCodeProcessService[getUserTasks] userId: {}, taskId: {}, taskName: {}.", userId, task.getId(), task.getName());
        }
        return list;
    }

    /**
     * 申请任务
     *
     * @param taskId 任务ID
     */
    public void completeTask(String taskId, String userId) {
        // 校验用户是否为任务的被分配人
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskAssignee(userId)
                .singleResult();

        if (Objects.isNull(task)) {
            log.info("SimpleCodeProcessService[completeTask] User {} does not have permission to complete this task.", userId);
            return;
        }

        taskService.complete(taskId);
        log.info("SimpleCodeProcessService[completeTask] Task completed.");
    }

    /**
     * 审批任务
     *
     * @param taskId 任务ID
     * @param userId 驳回人
     * @param decision 决策：approve（通过）或 reject（驳回）
     */
    public void approveApplication(String taskId, String userId, String decision) {
        // 校验用户是否为任务的被分配人
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskAssignee(userId)
                .singleResult();
        if (Objects.isNull(task)) {
            log.info("SimpleReturnProcessService[approveApplication] User {} does not have permission to complete this task.", userId);
            return;
        }

        // 设置流程变量，根据审批结果控制流程流转
        Map<String, Object> variables = new HashMap<>();
        variables.put("approvalDecision", decision);
        // 完成任务
        taskService.complete(taskId, variables);
        log.info("SimpleReturnProcessService[approveApplication] Task {} completed with decision: {}", taskId, decision);
    }
}
