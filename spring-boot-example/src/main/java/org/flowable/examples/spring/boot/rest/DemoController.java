package org.flowable.examples.spring.boot.rest;

import org.flowable.engine.DynamicBpmnService;
import org.flowable.engine.FormService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Demo 控制层
 *
 * @author xuwentao
 * @date 2024/12/6
 * @since 2.0.0
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    /**
     * 流程引擎的引用, 可以通过它获得所有api的服务对象
     */
    @Resource
    private ProcessEngine processEngine;
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
     * 用户及组的管理
     * <br/> - 用于管理（创建，更新，删除，查询……）组与用户。
     * <br/> - 在运行时, 系统并不做任何用户检查, 即用户表中可能不存在该用户。
     */
    @Resource
    private IdentityService identityService;
    /**
     * 是可选服务。提供简单的表单功能
     * <br/> - 引入了开始表单(start form)与任务表单(task form)的概念。
     * <br/> - 开始表单是在流程实例启动前显示的表单，而任务表单是用户完成任务时显示的表单。
     */
    @Resource
    private FormService formService;
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
     * 通常在用Flowable编写用户应用时不需要使用。它可以读取数据库表与表原始数据的信息，也提供了对作业(job)的查询与管理操作。
     * <br/> - 使用作业，包括定时器(timer)，异步操作(asynchronous continuation)，延时暂停/激活(delayed suspension/activation)等等
     */
    @Resource
    private ManagementService managementService;
    /**
     * 可用于修改流程定义中的部分内容，而不需要重新部署它。例如可以修改流程定义中一个用户任务的办理人设置，或者修改一个服务任务中的类名。
     */
    @Resource
    private DynamicBpmnService dynamicBpmnService;

}
