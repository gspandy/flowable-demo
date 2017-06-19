package com.plumdo.cmd;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * 设置任务配置了完成人的流程变量
 * @author wengwh
 *
 */
public class SaveTaskAssigneeVarCmd extends NeedsActiveTaskCmd<Void>  {
	private static final long serialVersionUID = 1L;
	
	  
	public SaveTaskAssigneeVarCmd(String taskId) {
		super(taskId);
	}
	
	@Override
	protected Void execute(CommandContext commandContext,TaskEntity task) {
		BpmnModel bpmnModel = commandContext.getProcessEngineConfiguration()
									.getDeploymentManager().getBpmnModelById(task.getProcessDefinitionId());

		//因为taskDefinition中没有把extensionId解析进去，只能去bpmnModel中获取
		for (UserTask userTask : bpmnModel.getMainProcess().findFlowElementsOfType(UserTask.class)) {
			if(userTask.getId().equals(task.getTaskDefinitionKey())){
				if(StringUtils.isNotEmpty(userTask.getExtensionId()) && StringUtils.isNotEmpty(task.getAssignee())){
					task.setVariable(userTask.getExtensionId(), task.getAssignee());
				}
				break;
			}
		}
		return null;
	}

   
}