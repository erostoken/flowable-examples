package org.flowable.examples.spring.boot;

import org.flowable.examples.spring.boot.service.SimpleReturnProcessService;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * simple-group-process.bpmn20.xml 文件对应执行测试类
 *
 * @author xuwentao
 * @date 2024/12/12
 * @since 2.0.0
 */
@SpringBootTest
public class SimpleReturnProcessTests {

    @Resource
    private SimpleReturnProcessService simpleReturnProcessService;

    /**
     * 提交一个 申请人(initiator)、审批人(approver)的任务
     */
    @Test
    void submitApplication() {
        simpleReturnProcessService.submitApplication("initiator", "approver");
    }

    /**
     * 查询 申请人所存在任务, 提交任务
     */
    @Test
    void submitRequestTask() {
        String userId = "initiator";
        List<Task> initiatorTask = simpleReturnProcessService.getUserTasks(userId);

        for (Task task : initiatorTask) {
            simpleReturnProcessService.completeTask(task.getId(), userId);
        }
    }

    /**
     * 查询 审批人所存在任务, 审批任务
     */
    @Test
    void approveRequestTask() {
        String userId = "approver";
        String decision = "approve"; // 驳回操作
        List<Task> approverTask = simpleReturnProcessService.getUserTasks(userId);

        for (Task task : approverTask) {
            simpleReturnProcessService.approveApplication(task.getId(), userId, decision);
        }
    }

    /**
     * 查询 审批人所存在任务, 驳回任务
     */
    @Test
    void returnRequestTask() {
        String userId = "approver";
        String decision = "reject"; // 驳回操作
        List<Task> approverTask = simpleReturnProcessService.getUserTasks(userId);

        for (Task task : approverTask) {
            simpleReturnProcessService.approveApplication(task.getId(), userId, decision);
        }
    }

}
