Example project for using the Flowable starters with Spring boot.
It automatically deploys all process and cases from the `processes` and `cases` folders respectively.
Exposes the REST endpoints for the 6 engines of Flowable:
* `process-api` for the Process Engine
* `cmmn-api` for the CMMN Engine
* `dmn-api` for the DMN Engine
* `idm-api` for the IDM Engine
* `form-api` for the Form Engine
* `content-api` for the Content Engine

## 参考文档
https://zhuanlan.zhihu.com/p/620451866
https://www.flowable.com/open-source/docs/bpmn/ch02-GettingStarted

## Flowable 6.8.1
### flowable-rest

访问地址: (启动未访问通)
http://localhost:10086/flowable-rest/docs/ (login/password: rest-admin/test)


### flowable-ui

访问地址:
http://localhost:10086/flowable-ui (login/password: admin/test)

### 表设计
#### 1 ACT_RE_*
’RE’表示repository（存储）。RepositoryService接口操作的表。带此前缀的表包含的是静态信息，如，流程定义，流程的资源（图片，规则等）。

#### 2 ACT_RU_*
’RU’表示runtime。这是运行时的表存储着流程变量，用户任务，变量，职责（job）等运行时的数据。flowable只存储实例执行期间的运行时数据，当流程实例结束时，将删除这些记录。这就保证了这些运行时的表小且快。

#### 3 ACT_ID_*
’ID’表示identity(组织机构)。这些表包含标识的信息，如用户，用户组，等等。

一般在正式系统中, 会将这些表用业务系统的组织机构, 角色表进行替换。

#### 4 ACT_HI_*
’HI’表示history。就是这些表包含着历史的相关数据，如结束的流程实例，变量，任务，等等。

#### 5 ACT_GE_*
普通数据，各种情况都使用的数据。