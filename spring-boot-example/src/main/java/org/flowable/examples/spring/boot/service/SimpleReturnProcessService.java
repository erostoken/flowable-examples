package org.flowable.examples.spring.boot.service;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * simple-return-process.bpmn20.xml 文件对应执行服务类
 *
 * @author xuwentao
 * @date 2024/12/12
 * @since 2.0.0
 */
@Slf4j
@Service
public class SimpleReturnProcessService {

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
     * 查询Flowable引擎收集的所有历史数据。 运行期的数据会同步至历史数据表, 因此运行期的数据表始终保持在一个比较低的数据量, 而历史表则越来越大
     * <br/> - 流程实例启动时间
     * <br/> - 谁在执行哪个任务
     * <br/> - 完成任务花费的事件
     * <br/> - 每个流程实例的执行路径
     */
    @Resource
    private HistoryService historyService;

    /**
     * 开始一个任务流，指定发起人和审批人
     *
     * @param initiator 发起人
     * @param approver  审批人
     */
    public void submitApplication(String initiator, String approver) {
        // simple-process.bpmn20.xml文件 flowable:assignee 指定了关键key initiator 和 approver
        Map<String, Object> variables = new HashMap<>();
        variables.put("initiator", initiator);
        variables.put("approver", approver);

        // 指定审批流程key - simple-return-process.bpmn20.xml文件中 process id所指定
        runtimeService.startProcessInstanceByKey("simpleReturnProcess", variables);
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
            log.info("SimpleGroupProcessService[getUserTasks] userId: {}, taskId: {}, taskName: {}.", userId, task.getId(), task.getName());
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
            log.info("SimpleGroupProcessService[completeTask] User {} does not have permission to complete this task.", userId);
            return;
        }

        taskService.complete(taskId);
        log.info("SimpleGroupProcessService[completeTask] Task completed.");
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
            log.info("SimpleReturnProcessService[returnApplication] User {} does not have permission to complete this task.", userId);
            return;
        }

        // 设置流程变量，根据审批结果控制流程流转
        Map<String, Object> variables = new HashMap<>();
        variables.put("approvalDecision", decision);
        // 完成任务
        taskService.complete(taskId, variables);
        log.info("SimpleReturnProcessService[returnApplication] Task {} completed with decision: {}", taskId, decision);
    }
}
