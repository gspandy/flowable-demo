package com.plumdo.cmd;


import java.util.List;

import org.activiti.engine.identity.User;
import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

import cn.starnet.activiti.engine.db.entity.impl.CcInfoEntity;
import cn.starnet.activiti.engine.db.entity.impl.HistoricCcInfoEntity;
import cn.starnet.activiti.engine.db.entity.manager.CcInfoEntityManger;
import cn.starnet.activiti.engine.db.entity.manager.HistoricCcInfoEntityManger;

/**
 * 抄送任务
 * @author wengwh
 *
 */
public class CcTaskCmd extends NeedsActiveTaskCmd<Void> {
	  
	private static final long serialVersionUID = 1L;

	protected String assigner;
	protected List<String> userIds;
	protected List<String> groupIds;
	
	public CcTaskCmd(String taskId,String assigner,List<String> userIds,List<String> groupIds) {
		super(taskId);
	    this.assigner = assigner;
	    this.userIds = userIds;
	    this.groupIds = groupIds;
	}
	
	@Override
	protected Void execute(CommandContext commandContext, TaskEntity task) {
		
		for(String userId : userIds){
			execute(commandContext, task, userId);
		}
		
		for(String groupId : groupIds){
			List<User> users = commandContext.getProcessEngineConfiguration().getIdentityService().createUserQuery().memberOfGroup(groupId).list();
			for(User user : users){
				this.execute(commandContext, task, user.getId());
			}
		}
		
		return null;
	}
	
	private boolean execute(CommandContext commandContext, TaskEntity task,String assignee) {
		long count = commandContext.getSession(CcInfoEntityManger.class).createNewCcInfoQuery().taskId(taskId).assignee(assignee).count();
		if(count > 0){
//			throw new ActivitiForbiddenException("cc the task with id " + taskId + " and assignee " + assignee + " already exist");
			return false;
		}
		CcInfoEntity ccInfo = new CcInfoEntity(task);
		ccInfo.setAssignee(assignee);
		
		if(assigner != null){
			ccInfo.setAssigner(assigner);
		}else if(Authentication.getAuthenticatedUserId() != null){
			ccInfo.setAssigner(Authentication.getAuthenticatedUserId());
		}
		
		commandContext.getSession(CcInfoEntityManger.class).insertCcInfo(ccInfo);
		 
		HistoricCcInfoEntity historicCcInfo = new HistoricCcInfoEntity(ccInfo);
		    
	    commandContext.getSession(HistoricCcInfoEntityManger.class).insertHistoricCcInfo(historicCcInfo);
		
	    return true;
	}
	
	
	
}
