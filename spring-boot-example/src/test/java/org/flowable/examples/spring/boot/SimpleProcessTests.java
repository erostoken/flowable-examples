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

import org.flowable.examples.spring.boot.service.SimpleProcessService;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * simple-process.bpmn20.xml 文件对应执行测试类
 *
 * @author xuwentao
 * @date 2024/12/9
 * @since 2.0.0
 */
@SpringBootTest
class SimpleProcessTests {

	@Resource
	private SimpleProcessService simpleProcessService;

	/**
	 * 提交一个 申请人(initiator)、审批人(approver)的任务
	 */
	@Test
	void submitApplication() {
		simpleProcessService.submitApplication("initiator", "approver");
	}

	/**
	 * 查询 申请人所存在任务, 提交任务
	 */
	@Test
	void submitRequestTask() {
		List<Task> initiatorTask = simpleProcessService.getTasks("initiator");
		System.out.println(initiatorTask);

		for (Task task : initiatorTask) {
			simpleProcessService.approveApplication(task.getId());
		}
	}

	/**
	 * 查询 审批人所存在任务, 审批任务
	 */
	@Test
	void approveRequestTask() {
		List<Task> approverTask = simpleProcessService.getTasks("approver");
		System.out.println(approverTask);

		for (Task task : approverTask) {
			simpleProcessService.approveApplication(task.getId());
		}
	}

}
