/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.examples.spring.boot;

import org.flowable.examples.spring.boot.service.SimpleGroupProcessService;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * simple-group-process.bpmn20.xml 文件对应执行测试类
 *
 * @author xuwentao
 * @date 2024/12/9
 * @since 2.0.0
 */
@SpringBootTest
class SimpleGroupProcessTests {

	@Resource
	private SimpleGroupProcessService simpleGroupProcessService;

	/**
	 * 提交一个 申请组(initiators)、审批组(approvers)的任务
	 */
	@Test
	void submitApplication() {
		simpleGroupProcessService.startProcess("initiators" , "approvers");
	}

	/**
	 * 查询 申请人所存在任务, 提交任务
	 */
	@Test
	void submitRequestTask() {
		String userId = "alice";
		List<Task> initiatorTask = simpleGroupProcessService.getGroupTasks("initiators");

		for (Task task : initiatorTask) {
			simpleGroupProcessService.claimTask(task.getId(), userId);
		}

		List<Task> userTasks = simpleGroupProcessService.getUserTasks(userId);

		for (Task userTask : userTasks) {
			simpleGroupProcessService.completeTask(userTask.getId(), userId);
		}
	}

	/**
	 * 查询 审批人所存在任务, 审批任务
	 */
	@Test
	void approveRequestTask() {
		String userId = "bob";
		List<Task> approverTask = simpleGroupProcessService.getGroupTasks("approvers");

		for (Task task : approverTask) {
			simpleGroupProcessService.claimTask(task.getId(), userId);
		}

		List<Task> userTasks = simpleGroupProcessService.getUserTasks(userId);

		for (Task userTask : userTasks) {
			simpleGroupProcessService.completeTask(userTask.getId(), userId);
		}
	}

}
