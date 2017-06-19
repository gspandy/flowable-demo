package com.plumdo.cmd;

import java.io.Serializable;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;


/**
 * 根据key获取最新流程定义
 * 
 * @author wengwh
 * 
 */
public class GetProcessDefinitionLatestByKeyCmd implements Command<ProcessDefinitionEntity>, Serializable {

	private static final long serialVersionUID = 1L;
	protected String processDefinitionKey;
	protected String tenantId;

	public GetProcessDefinitionLatestByKeyCmd(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	public GetProcessDefinitionLatestByKeyCmd(String processDefinitionKey,String tenantId) {
		this.processDefinitionKey = processDefinitionKey;
		this.tenantId = tenantId;
	}
	
	public ProcessDefinitionEntity execute(CommandContext commandContext) {
		if(tenantId != null){
			return commandContext.getProcessEngineConfiguration()
					.getDeploymentManager()
					.findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
		}else{
			return commandContext.getProcessEngineConfiguration()
					.getDeploymentManager()
					.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
		}
	}

}
