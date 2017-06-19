package com.plumdo.cmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.TaskInfo;

import cn.starnet.activiti.engine.db.entity.ActLog;
import cn.starnet.activiti.engine.db.entity.manager.ActLogEntityManger;

/**
 * 保存监控日志
 * @author wengwh
 *
 */
public class SaveTaskActLogCmd implements Command<Void>, Serializable {
	  
	private static final long serialVersionUID = 1L;
	protected String taskId;
	protected String actType;
	protected String actName;
	protected String nextUserId;
	protected String nextUserName;
	protected List<String> nextUserIds;
	protected String comment;
	  
	public SaveTaskActLogCmd(String taskId,String actType,String actName,String nextUserId,String comment) {
		this.taskId = taskId;
		this.actType = actType;
		this.actName = actName;
		this.nextUserId = nextUserId;
		this.comment = comment;
	}
	
	public SaveTaskActLogCmd(String taskId,String actType,String actName,List<String> nextUserIds,String comment) {
		this.taskId = taskId;
		this.actType = actType;
		this.actName = actName;
		this.nextUserIds = nextUserIds;
		this.comment = comment;
	}
	public SaveTaskActLogCmd(String taskId,String actType,String actName,String nextUserId,String nextUserName,String comment) {
		this.taskId = taskId;
		this.actType = actType;
		this.actName = actName;
		this.nextUserId = nextUserId;
		this.nextUserName = nextUserName;
		this.comment = comment;
	}
	  
	public Void execute(CommandContext commandContext) {
		TaskInfo task = commandContext.getTaskEntityManager().findTaskById(taskId);
		if(task == null)
			task = commandContext.getHistoricTaskInstanceEntityManager().findHistoricTaskInstanceById(taskId);
	    
	    if(task == null) {
	    	throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskId + "'.",HistoricTaskInstance.class);
	    }
	    
	    ActLog actLog = commandContext.getSession(ActLogEntityManger.class).createNewActLog();
	   
	    ProcessDefinition processDefinition = commandContext.getProcessDefinitionEntityManager().findProcessDefinitionById(task.getProcessDefinitionId());
	    actLog.setProcessDefinitionId(processDefinition.getId());
	    actLog.setProcessDefinitionName(processDefinition.getName());
	    actLog.setProcessDefinitionKey(processDefinition.getKey());
	    actLog.setProcessDefinitionVersion(processDefinition.getVersion());

	    actLog.setProcessInstanceId(task.getProcessInstanceId());
	    
	    actLog.setTaskId(task.getId());
	    actLog.setTaskName(task.getName());
	    actLog.setTaskDefinitionKey(task.getTaskDefinitionKey());
		
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
	    	if(nextUserName != null){
	    		actLog.setNextUserName(nextUserName);
	    	}else{
		    	User user = commandContext.getUserIdentityManager().findUserById(nextUserId);
		    	if(user!=null)
		    		actLog.setNextUserName(user.getFirstName());
	    	}
	    }else if(nextUserIds != null){
	    	List<String> nextUserNames = new ArrayList<String>();
	    	for(String userId : nextUserIds){
	    		User user = commandContext.getUserIdentityManager().findUserById(userId);
		    	if(user!=null)
		    		nextUserNames.add(user.getFirstName());
	    	}
	    	actLog.setNextUserId(nextUserIds.toString());
    		actLog.setNextUserName(nextUserNames.toString());
	    }
	    actLog.setComment(comment);
	    
	    actLog.setTenantId(task.getTenantId());
	    
	    commandContext.getSession(ActLogEntityManger.class).insertActLog(actLog);
	    
	    return null;
	}

}
