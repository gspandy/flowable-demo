package com.plumdo.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.IdentityLink;
import org.flowable.engine.task.IdentityLinkType;
import org.flowable.engine.task.Task;

import com.plumdo.rest.model.ModelResponse;


/**
 * rest接口返回结果工厂类
 * @author wengwh
 *
 */
public class RestResponseFactory {

	protected List<RestVariableConverter> variableConverters = new ArrayList<RestVariableConverter>();

	public RestResponseFactory() {
		initializeVariableConverters();
	}
	
	protected void initializeVariableConverters() {
		variableConverters.add(new StringRestVariableConverter());
		variableConverters.add(new IntegerRestVariableConverter());
		variableConverters.add(new LongRestVariableConverter());
		variableConverters.add(new ShortRestVariableConverter());
		variableConverters.add(new DoubleRestVariableConverter());
		variableConverters.add(new BooleanRestVariableConverter());
		variableConverters.add(new DateRestVariableConverter());
		variableConverters.add(new ListRestVariableConverter());
		
	}
	

	public List<TaskResponse> createTaskResponseList(List<TaskExt> tasks) {
		List<TaskResponse> responseList = new ArrayList<TaskResponse>();
		for (TaskExt instance : tasks) {
			responseList.add(createTaskResponse(instance));
		}
		return responseList;
	}
	
	public TaskResponse createTaskResponse(TaskExt taskInstance) {
		TaskResponse result = new TaskResponse();
		createTaskResponse(result,taskInstance);
		return result;
	}
	
	public TaskCompleteResponse createTaskCompleteResponse(TaskExt taskInstance,List<IdentityLink> identityLinks) {
		TaskCompleteResponse result = new TaskCompleteResponse();
		createTaskResponse(result,taskInstance);
		result.setCandidate(createTaskIdentityResponseList(identityLinks));
		return result;
	}
	
	private void createTaskResponse(TaskResponse result,TaskExt taskInstance){
		result.setId(taskInstance.getId());
		result.setName(taskInstance.getName());
		result.setOwner(taskInstance.getOwner());
		result.setTaskDefinitionKey(taskInstance.getTaskDefinitionKey());
		result.setCreateTime(taskInstance.getCreateTime());
		result.setAssignee(taskInstance.getAssignee());
		result.setDescription(taskInstance.getDescription());
		result.setDueDate(taskInstance.getDueDate());
		result.setDelegationState(taskInstance.getDelegationState());
		result.setFormKey(taskInstance.getFormKey());
		result.setParentTaskId(taskInstance.getParentTaskId());
		result.setPriority(taskInstance.getPriority());
		result.setSuspended(taskInstance.isSuspended());
		result.setTenantId(taskInstance.getTenantId());
		result.setCategory(taskInstance.getCategory());
		result.setProcessDefinitionId(taskInstance.getProcessDefinitionId());
		result.setProcessDefinitionName(taskInstance.getProcessDefinitionName());
		result.setProcessDefinitionKey(taskInstance.getProcessDefinitionKey());
		result.setProcessDefinitionVersion(taskInstance.getProcessDefinitionVersion());
		result.setProcessInstanceId(taskInstance.getProcessInstanceId());
		result.setStartUserId(taskInstance.getStartUserId());
		result.setStartUserName(taskInstance.getStartUserName());
		result.setAttrStr1(taskInstance.getAttrStr1());
		result.setAttrStr2(taskInstance.getAttrStr2());
		result.setAttrStr3(taskInstance.getAttrStr3());
		result.setAttrStr4(taskInstance.getAttrStr4());
		result.setAttrStr5(taskInstance.getAttrStr5());
		result.setAttrStr6(taskInstance.getAttrStr6());
		result.setAttrStr7(taskInstance.getAttrStr7());
		result.setAttrDate1(taskInstance.getAttrDate1());
	}

	public List<TaskIdentityResponse> createTaskIdentityResponseList(List<IdentityLink> identityLinks){
		List<TaskIdentityResponse> responseList = new ArrayList<TaskIdentityResponse>();
		for(IdentityLink identityLink : identityLinks){
			if(identityLink.getType().equals(IdentityLinkType.CANDIDATE)){
				responseList.add(createTaskIdentityResponse(identityLink));
			}
		}
		return responseList;
	}
	
	
	public TaskIdentityResponse createTaskIdentityResponse(IdentityLink identityLink){
		TaskIdentityResponse result = new TaskIdentityResponse();
		if(identityLink.getGroupId()!=null){
			result.setIdentityId(identityLink.getGroupId());
			result.setType(TaskIdentityResponse.AUTHORIZE_GROUP);
		}else if(identityLink.getUserId()!=null){
			result.setIdentityId(identityLink.getUserId());
			result.setType(TaskIdentityResponse.AUTHORIZE_USER);
		}
		return result;
	}
	
	public TaskNextActorResponse createTaskNextActorResponse(Task task,List<IdentityLink> identityLinks){
		TaskNextActorResponse taskNextActor = new TaskNextActorResponse();
		taskNextActor.setProcessDefinitionId(task.getProcessDefinitionId());
		taskNextActor.setTaskDefinitionKey(task.getTaskDefinitionKey());
		taskNextActor.setTaskDefinitionName(task.getName());
		for(IdentityLink identityLink :identityLinks){
			if(identityLink.getGroupId()!=null){
				taskNextActor.addActorInfo(identityLink.getGroupId(), TaskNextActorResponse.TYPE_GROUP, identityLink.getType());
			}else if(identityLink.getUserId()!=null){
				taskNextActor.addActorInfo(identityLink.getUserId(), TaskNextActorResponse.TYPE_USER, identityLink.getType());
			}
		}
		return taskNextActor;
	}
	
	public List<HistoricTaskResponse> createHistoricTaskResponseList(List<HistoricTaskExt> tasks) {
		List<HistoricTaskResponse> responseList = new ArrayList<HistoricTaskResponse>();
		for (HistoricTaskExt instance : tasks) {
			responseList.add(createHistoricTaskResponse(instance));
		}
		return responseList;
	}

	public HistoricTaskResponse createHistoricTaskResponse(HistoricTaskExt taskInstance) {
		HistoricTaskResponse result = new HistoricTaskResponse();
		result.setId(taskInstance.getId());
		result.setName(taskInstance.getName());
		result.setOwner(taskInstance.getOwner());
		result.setTaskDefinitionKey(taskInstance.getTaskDefinitionKey());
		result.setAssignee(taskInstance.getAssignee());
		result.setDescription(taskInstance.getDescription());
		result.setCategory(taskInstance.getCategory());
		result.setDueDate(taskInstance.getDueDate());
		result.setFormKey(taskInstance.getFormKey());
		result.setParentTaskId(taskInstance.getParentTaskId());
		result.setPriority(taskInstance.getPriority());
		result.setProcessDefinitionId(taskInstance.getProcessDefinitionId());
		result.setProcessDefinitionName(taskInstance.getProcessDefinitionName());
		result.setProcessDefinitionKey(taskInstance.getProcessDefinitionKey());
		result.setProcessDefinitionVersion(taskInstance.getProcessDefinitionVersion());
		result.setTenantId(taskInstance.getTenantId());
		result.setProcessInstanceId(taskInstance.getProcessInstanceId());
		result.setDurationInMillis(taskInstance.getDurationInMillis());
		result.setStartTime(taskInstance.getStartTime());
		result.setEndTime(taskInstance.getEndTime());
		result.setClaimTime(taskInstance.getClaimTime());
		result.setWorkTimeInMillis(taskInstance.getWorkTimeInMillis());
		result.setAttrStr1(taskInstance.getAttrStr1());
		result.setAttrStr2(taskInstance.getAttrStr2());
		result.setAttrStr3(taskInstance.getAttrStr3());
		result.setAttrStr4(taskInstance.getAttrStr4());
		result.setAttrStr5(taskInstance.getAttrStr5());
		result.setAttrStr6(taskInstance.getAttrStr6());
		result.setAttrStr7(taskInstance.getAttrStr7());
		result.setAttrDate1(taskInstance.getAttrDate1());
		return result;
	}

	
	public List<CcInfoResponse> createCcInfoResponseList(List<CcInfo> ccInfos) {
		List<CcInfoResponse> responseList = new ArrayList<CcInfoResponse>();
		for (CcInfo instance : ccInfos) {
			responseList.add(createCcInfoResponse(instance));
		}
		return responseList;
	}


	public CcInfoResponse createCcInfoResponse(CcInfo info) {
		CcInfoResponse result = new CcInfoResponse();
		result.setId(info.getId());
		result.setExecutionId(info.getExecutionId());
		result.setProcessInstanceId(info.getProcessInstanceId());
		result.setProcessDefinitionId(info.getProcessDefinitionId());
		result.setProcessDefinitionName(info.getProcessDefinitionName());
		result.setProcessDefinitionKey(info.getProcessDefinitionKey());
		result.setProcessDefinitionVersion(info.getProcessDefinitionVersion());
		result.setTaskName(info.getTaskName());
		result.setTaskId(info.getTaskId());
		result.setTaskDefinitionKey(info.getTaskDefinitionKey());
		result.setAssigner(info.getAssigner());
		result.setAssignee(info.getAssignee());
		result.setCreateTime(info.getCreateTime());
		result.setTenantId(info.getTenantId());
		return result;
	}
	
	public List<HistoricCcInfoResponse> createHistoricCcInfoResponseList(List<HistoricCcInfo> ccInfos) {
		List<HistoricCcInfoResponse> responseList = new ArrayList<HistoricCcInfoResponse>();
		for (HistoricCcInfo instance : ccInfos) {
			responseList.add(createHistoricCcInfoResponse(instance));
		}
		return responseList;
	}


	public HistoricCcInfoResponse createHistoricCcInfoResponse(HistoricCcInfo info) {
		HistoricCcInfoResponse result = new HistoricCcInfoResponse();
		result.setId(info.getId());
		result.setExecutionId(info.getExecutionId());
		result.setProcessInstanceId(info.getProcessInstanceId());
		result.setProcessDefinitionId(info.getProcessDefinitionId());
		result.setProcessDefinitionName(info.getProcessDefinitionName());
		result.setProcessDefinitionKey(info.getProcessDefinitionKey());
		result.setProcessDefinitionVersion(info.getProcessDefinitionVersion());
		result.setTaskName(info.getTaskName());
		result.setTaskId(info.getTaskId());
		result.setTaskDefinitionKey(info.getTaskDefinitionKey());
		result.setAssigner(info.getAssigner());
		result.setAssignee(info.getAssignee());
		result.setStartTime(info.getStartTime());
		result.setEndTime(info.getEndTime());
		result.setDurationInMillis(info.getDurationInMillis());
		result.setTenantId(info.getTenantId());
		return result;
	}
	
	
	public List<ProcessDefinitionResponse> createProcessDefinitionResponseList(List<ProcessDefinition> processDefinitions) {
		List<ProcessDefinitionResponse> responseList = new ArrayList<ProcessDefinitionResponse>();
		for (ProcessDefinition instance : processDefinitions) {
			responseList.add(createProcessDefinitionResponse(instance));
		}
		return responseList;
	}

	public ProcessDefinitionResponse createProcessDefinitionResponse(ProcessDefinition processDefinition) {
		ProcessDefinitionResponse response = new ProcessDefinitionResponse();
		response.setId(processDefinition.getId());
		response.setKey(processDefinition.getKey());
		response.setVersion(processDefinition.getVersion());
		response.setCategory(processDefinition.getCategory());
		response.setName(processDefinition.getName());
		response.setDescription(processDefinition.getDescription());
		response.setSuspended(processDefinition.isSuspended());
		response.setGraphicalNotationDefined(processDefinition.hasGraphicalNotation());
		response.setTenantId(processDefinition.getTenantId());
		return response;
	}
	
	public ProcessDefinitionNodeResponse createProcessDefinitionNodeResponse(ProcessDefinitionEntity processDefinition) {
		ProcessDefinitionNodeResponse result = new ProcessDefinitionNodeResponse();
		result.setProcessDefinitionId(processDefinition.getId());
		result.setProcessDefinitionName(processDefinition.getName());
		result.setProcessDefinitionKey(processDefinition.getKey());
		result.setProcessDefinitionVersion(processDefinition.getVersion());
		result.setTenantId(processDefinition.getTenantId());
		List<Map<String, String>> nodeInfo = new ArrayList<Map<String, String>>();
		List<TaskDefinition> taskDefinitions = new ArrayList<TaskDefinition>(processDefinition.getTaskDefinitions().values());
		for (TaskDefinition task : taskDefinitions) {
			Map<String, String> taskMap = new HashMap<String, String>();
			if(task.getNameExpression()!=null){
				taskMap.put("taskName", task.getNameExpression().getExpressionText());
			}else{
				taskMap.put("taskName","");
			}
			taskMap.put("taskDefinitionKey", task.getKey());
			nodeInfo.add(taskMap);
		}
		result.setNodeInfo(nodeInfo);
		return result;
	}
		
	public List<HistoricProcessInstanceResponse> createHistoricProcessInstancResponseList(List<HistoricProcessInstanceExt> processInstances) {
		List<HistoricProcessInstanceResponse> responseList = new ArrayList<HistoricProcessInstanceResponse>();
		for (HistoricProcessInstanceExt instance : processInstances) {
			responseList.add(createHistoricProcessInstanceResponse(instance));
		}
		return responseList;
	}

	public HistoricProcessInstanceResponse createHistoricProcessInstanceResponse(HistoricProcessInstanceExt processInstance) {
		HistoricProcessInstanceResponse result = new HistoricProcessInstanceResponse();
		result.setId(processInstance.getId());
		result.setBusinessKey(processInstance.getBusinessKey());
		result.setStartTime(processInstance.getStartTime());
		result.setEndTime(processInstance.getEndTime());
		result.setDurationInMillis(processInstance.getDurationInMillis());
		result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
		result.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
		result.setProcessDefinitionName(processInstance.getProcessDefinitionName());
		result.setProcessDefinitionVersion(processInstance.getProcessDefinitionVersion());
		result.setStartActivityId(processInstance.getStartActivityId());
		result.setStartUserId(processInstance.getStartUserId());
		result.setStartUserName(processInstance.getStartUserName());
		result.setSuperProcessInstanceId(processInstance.getSuperProcessInstanceId());
		result.setTenantId(processInstance.getTenantId());
		result.setAttrStr1(processInstance.getAttrStr1());
		result.setAttrStr2(processInstance.getAttrStr2());
		result.setAttrStr3(processInstance.getAttrStr3());
		result.setAttrStr4(processInstance.getAttrStr4());
		result.setAttrStr5(processInstance.getAttrStr5());
		result.setAttrStr6(processInstance.getAttrStr6());
		result.setAttrStr7(processInstance.getAttrStr7());
		result.setAttrDate1(processInstance.getAttrDate1());
		return result;
	}
	
	public List<ProcessInstanceResponse> createProcessInstanceResponseList(List<ProcessInstanceExt> processInstances) {
		List<ProcessInstanceResponse> responseList = new ArrayList<ProcessInstanceResponse>();
		for (ProcessInstanceExt instance : processInstances) {
			responseList.add(createProcessInstanceResponse(instance));
		}
		return responseList;
	}

	public ProcessInstanceResponse createProcessInstanceResponse(ProcessInstanceExt processInstance) {
		ProcessInstanceResponse result = new ProcessInstanceResponse();
		result.setId(processInstance.getId());
		result.setSuspended(processInstance.isSuspended());
		result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
		result.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
		result.setProcessDefinitionName(processInstance.getProcessDefinitionName());
		result.setProcessDefinitionVersion(processInstance.getProcessDefinitionVersion());
		result.setStartTime(processInstance.getStartTime());
		result.setStartUserId(processInstance.getStartUserId());
		result.setStartUserName(processInstance.getStartUserName());
		result.setCurrentActivityId(processInstance.getActivityId());
		result.setCurrentActivityName(processInstance.getActivityName());
		result.setSuperProcessInstanceId(processInstance.getSuperProcessInstanceId());
		result.setBusinessKey(processInstance.getBusinessKey());
		result.setTenantId(processInstance.getTenantId());
		result.setAttrStr1(processInstance.getAttrStr1());
		result.setAttrStr2(processInstance.getAttrStr2());
		result.setAttrStr3(processInstance.getAttrStr3());
		result.setAttrStr4(processInstance.getAttrStr4());
		result.setAttrStr5(processInstance.getAttrStr5());
		result.setAttrStr6(processInstance.getAttrStr6());
		result.setAttrStr7(processInstance.getAttrStr7());
		result.setAttrDate1(processInstance.getAttrDate1());
		return result;
	}

	public ProcessInstanceStartResponse createProcessInstanceStartResponse(ProcessInstance processInstance, List<Task> tasks) {
		ProcessInstanceStartResponse result = new ProcessInstanceStartResponse();
		result.setId(processInstance.getId());
		result.setBusinessKey(processInstance.getBusinessKey());
		result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
		//接口有提供获取定义名称和key但是在启动api里面没有设置进去，只能通过获取定义获取
		result.setProcessDefinitionName(((ExecutionEntity)processInstance).getProcessDefinition().getName());
		result.setProcessDefinitionKey(((ExecutionEntity)processInstance).getProcessDefinition().getKey());
		
		result.setCurrentActivityId(processInstance.getActivityId());
		result.setTenantId(processInstance.getTenantId());
		List<Map<String, String>> taskInfo = new ArrayList<Map<String, String>>();
		for (Task task : tasks) {
			Map<String, String> taskMap = new HashMap<String, String>();
			taskMap.put("taskId", task.getId());
			taskMap.put("taskName", task.getName());
			taskMap.put("taskDefinitionKey", task.getTaskDefinitionKey());
			taskInfo.add(taskMap);
		}
		result.setTaskInfo(taskInfo);
		return result;
	}
	
	

	
	public List<ProcessInstanceTraceResponse> createProcessInstanceTraceResponseList(List<ActLog> actLogs) {
		List<ProcessInstanceTraceResponse> responseList = new ArrayList<ProcessInstanceTraceResponse>();
		for (ActLog instance : actLogs) {
			responseList.add(createProcessInstanceTraceResponse(instance));
		}
		return responseList;
	}

	public ProcessInstanceTraceResponse createProcessInstanceTraceResponse(ActLog actLog) {
		ProcessInstanceTraceResponse result = new ProcessInstanceTraceResponse();
		result.setId(actLog.getId());
		result.setActName(actLog.getActName());
		result.setActType(actLog.getActType());
		result.setActTime(actLog.getActTime());
		result.setProcessDefinitionId(actLog.getProcessDefinitionId());
		result.setProcessDefinitionName(actLog.getProcessDefinitionName());
		result.setProcessDefinitionKey(actLog.getProcessDefinitionKey());
		result.setProcessDefinitionVersion(actLog.getProcessDefinitionVersion());
		result.setProcessInstanceId(actLog.getProcessInstanceId());
		result.setTaskId(actLog.getTaskId());
		result.setTaskDefinitionKey(actLog.getTaskDefinitionKey());
		result.setTaskName(actLog.getTaskName());
		result.setUserId(actLog.getUserId());
		result.setUserName(actLog.getUserName());
		result.setNextUserId(actLog.getNextUserId());
		result.setNextUserName(actLog.getNextUserName());
		result.setComment(actLog.getComment());
		result.setTenantId(actLog.getTenantId());
		return result;
	}
	public List<ModelResponse> createModelResponseList(List<Model> models) {
		List<ModelResponse> responseList = new ArrayList<ModelResponse>();
		for (Model instance : models) {
			responseList.add(createModelResponse(instance));
		}
		return responseList;
	}

	public ModelResponse createModelResponse(Model model) {
		ModelResponse response = new ModelResponse();
		response.setCategory(model.getCategory());
		response.setCreateTime(model.getCreateTime());
		response.setId(model.getId());
		response.setKey(model.getKey());
		response.setLastUpdateTime(model.getLastUpdateTime());
		response.setMetaInfo(model.getMetaInfo());
		response.setName(model.getName());
		response.setVersion(model.getVersion());
		if(model.getDeploymentId()!=null){
			response.setDeployed(true);
		}else{
			response.setDeployed(false);
		}
		response.setTenantId(model.getTenantId());
		return response;
	}
		  

	public Object getVariableValue(RestVariable restVariable) {
		Object value = null;

		if (restVariable.getType() != null) {
			RestVariableConverter converter = null;
			for (RestVariableConverter conv : variableConverters) {
				if (conv.getRestTypeName().equals(restVariable.getType())) {
					converter = conv;
					break;
				}
			}
			if (converter == null) {
				throw new ActivitiIllegalArgumentException("Variable '"
						+ restVariable.getName() + "' has unsupported type: '"
						+ restVariable.getType() + "'.");
			}
			value = converter.getVariableValue(restVariable);

		} else {
			value = restVariable.getValue();
		}
		return value;
	}

}