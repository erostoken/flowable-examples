package org.flowable.examples.spring.boot.service;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * simple-process.bpmn20.xml 文件对应执行服务类
 *
 * @author xuwentao
 * @date 2024/12/9
 * @since 2.0.0
 */
@Service
public class SimpleProcessService {

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

        // 指定审批流程key - simple-process.bpmn20.xml文件中 process id所指定
        runtimeService.startProcessInstanceByKey("simpleProcess", variables);
    }

    /**
     * 查询任务
     *
     * @param assignee 受理人
     * @return 任务列表
     */
    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    /**
     * 执行任务
     * TODO 这里没有校验taskId的执行权限，任一拿到taskId的人员都可以直接执行，存在漏洞
     *
     * @param taskId 任务ID
     */
    public void approveApplication(String taskId) {
        taskService.complete(taskId);
    }

}
