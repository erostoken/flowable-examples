package org.flowable.examples.spring.boot.service;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.identitylink.api.IdentityLinkInfo;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * simple-group-process.bpmn20.xml 文件对应执行服务类
 *
 * @author xuwentao
 * @date 2024/12/10
 * @since 2.0.0
 */
@Slf4j
@Service
public class SimpleGroupProcessService {

    @Resource
    private TaskService taskService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private IdentityService identityService;

    @Autowired
    public void init(IdentityService identityService) {
        log.info("SimpleGroupProcessService[init] 初始化 组 和 用户");
        // 创建组
        if (identityService.createGroupQuery().groupId("initiators").singleResult() == null) {
            Group applicantsGroup = identityService.newGroup("initiators");
            applicantsGroup.setName("Initiators Group");
            identityService.saveGroup(applicantsGroup);
        }

        if (identityService.createGroupQuery().groupId("approvers").singleResult() == null) {
            Group approversGroup = identityService.newGroup("approvers");
            approversGroup.setName("Approvers Group");
            identityService.saveGroup(approversGroup);
        }

        // 创建用户并分配到组
        if (identityService.createUserQuery().userId("alice").singleResult() == null) {
            User alice = identityService.newUser("alice");
            alice.setFirstName("Alice");
            identityService.saveUser(alice);
            identityService.createMembership("alice", "initiators");
        }

        if (identityService.createUserQuery().userId("bob").singleResult() == null) {
            User bob = identityService.newUser("bob");
            bob.setFirstName("Bob");
            identityService.saveUser(bob);
            identityService.createMembership("bob", "approvers");
        }
    }

    /**
     * 开始一个任务流，指定发起人和审批人
     *
     * @param initiators 发起组
     * @param approvers  审批组
     */
    public void startProcess(String initiators, String approvers) {
        // simple-group-process.bpmn20.xml文件 flowable:assignee 指定了关键key initiators 和 approvers
        Map<String, Object> variables = new HashMap<>();
        variables.put("initiators", initiators);
        variables.put("approvers", approvers);

        // 指定审批流程key - simple-group-process.bpmn20.xml文件中 process id所指定
        runtimeService.startProcessInstanceByKey("simpleGroupProcess", variables);
        log.info("SimpleGroupProcessService[startProcess] Process started.");

        for (Task task : taskService.createTaskQuery().list()) {
            log.info("SimpleGroupProcessService[startProcess] taskId: {}, taskName: {}.", task.getId(), task.getName());
        }
    }

    /**
     * 查询组任务
     *
     * @param group 组名称
     * @return 组任务列表
     */
    public List<Task> getGroupTasks(String group) {
        List<Task> list = taskService.createTaskQuery().taskCandidateGroup(group).list();
        for (Task task : list) {
            log.info("SimpleGroupProcessService[getGroupTasks] group: {}, taskId: {}, taskName: {}.", group, task.getId(), task.getName());
        }
        return list;
    }

    /**
     * 认领任务
     *
     * @param taskId 任务ID
     * @param userId 用户唯一标识
     */
    public void claimTask(String taskId, String userId) {
        // 获取任务的候选组
        List<String> candidateGroups = taskService.getIdentityLinksForTask(taskId).stream()
                /*
                 * 定义了用户/组的角色，主要有以下几种类型：
                 *
                 * 类型值	描述
                 * assignee	任务的被分配人（唯一分配）
                 * owner	任务的所有者
                 * candidate	候选用户或候选组，表示可以操作任务（领取或完成）
                 * participant	流程或任务的参与者，通常用于记录任务的协作用户
                 * starter	启动流程实例的用户（适用于流程定义）
                 * manager	流程或任务的管理者（用户扩展定义）
                 */
                .filter(link -> "candidate".equals(link.getType()))
                .map(IdentityLinkInfo::getGroupId)
                .collect(Collectors.toList());

        // 验证用户是否属于候选组
        boolean isUserInCandidateGroup = identityService.createGroupQuery().groupIds(candidateGroups)
                .groupMember(userId)
                .count() > 0;

        if (!isUserInCandidateGroup) {
            log.info("SimpleGroupProcessService[claimTask] User {} is not authorized to claim this task.", userId);
            return;
        }

        taskService.claim(taskId, userId);
        log.info("SimpleGroupProcessService[claimTask] Task claimed by {}", userId);
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
     * 完成任务
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

}
