package com.plumdo.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.ProcessDefinition;

import cn.starnet.activiti.engine.db.entity.ActLog;
import cn.starnet.activiti.engine.db.entity.manager.ActLogEntityManger;

/**
 * 保存监控日志
 * @author wengwh
 *
 */
public class SaveProcessActLogCmd implements Command<Void>, Serializable {
	  
	private static final long serialVersionUID = 1L;
	protected String processInstanceId;
	protected String actType;
	protected String actName;
	protected String nextUserId;
	protected String comment;
	  
	public SaveProcessActLogCmd(String processInstanceId,String actType,String actName,String nextUserId,String comment) {
		this.processInstanceId = processInstanceId;
		this.actType = actType;
		this.actName = actName;
		this.nextUserId = nextUserId;
		this.comment = comment;
	}
	  
	public Void execute(CommandContext commandContext) {
		HistoricProcessInstance processInstance = commandContext.getHistoricProcessInstanceEntityManager().findHistoricProcessInstance(processInstanceId);
	    
	    if(processInstance == null) {	
	    	throw new ActivitiObjectNotFoundException("Could not find a processInstance with id '" + processInstanceId + "'.",HistoricProcessInstance.class);
	    }
	    
	    ActLog actLog = commandContext.getSession(ActLogEntityManger.class).createNewActLog();

	    ProcessDefinition processDefinition = commandContext.getProcessDefinitionEntityManager().findProcessDefinitionById(processInstance.getProcessDefinitionId());
	    actLog.setProcessDefinitionId(processDefinition.getId());
	    actLog.setProcessDefinitionName(processDefinition.getName());
	    actLog.setProcessDefinitionKey(processDefinition.getKey());
	    actLog.setProcessDefinitionVersion(processDefinition.getVersion());

	    actLog.setProcessInstanceId(processInstance.getId());
	    
	    actLog.setActTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
	    actLog.setActType(actType);
	    actLog.setActName(actName);
	    
	    if(Authentication.getAuthenticatedUserId() != null){
	    	actLog.setUserId(Authentication.getAuthenticatedUserId());
	    	User user = commandContext.getUserIdentityManager().findUserById(Authentication.getAuthenticatedUserId());
	    	if(user!=null)
	    		actLog.setUserName(user.getFirstName());
	    }
	    
	    if(nextUserId != null){
	    	actLog.setNextUserId(nextUserId);
	    	User user = commandContext.getUserIdentityManager().findUserById(nextUserId);
	    	if(user!=null)
	    		actLog.setNextUserName(user.getFirstName());
	    }
	    
	    actLog.setComment(comment);
	    actLog.setTenantId(processInstance.getTenantId());
	    
	    commandContext.getSession(ActLogEntityManger.class).insertActLog(actLog);
	    
	    return null;
	}

}
