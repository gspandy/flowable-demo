package com.plumdo.cmd;

import java.util.List;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;

import cn.starnet.activiti.engine.exception.ActivitiForbiddenException;

/**
 * 回收任务
 * 规则：先查找回退的节点，开始删除分支，根据回退节点创建分支以及任务，对子流程等几种特殊节点做处理
 * @author wengwh
 *
 */
public class RecoverTaskCmd extends ReturnTaskCmd {

	private static final long serialVersionUID = 1L;
	

	public RecoverTaskCmd(String taskId) {
		super(taskId);
	}

	@Override
	public List<Task> execute(CommandContext commandContext) {
		
		HistoricTaskInstance hisTaskInst = commandContext.getHistoricTaskInstanceEntityManager().findHistoricTaskInstanceById(taskId);
		
		
	    if (hisTaskInst == null) {
	    	throw new ActivitiObjectNotFoundException("Cannot find task with id " + taskId, HistoricTaskInstance.class);
	    }
		    
	    if (hisTaskInst.getEndTime()==null) {
	    	throw new ActivitiForbiddenException("only recover finished task ");
	    }

	    if(!isNearestHistoricTask(hisTaskInst)){
	    	throw new ActivitiForbiddenException("the task  with id " + taskId+ "is not nearest finished");
	    }
	    
		List<Task> tasks = new GetNextTasksCmd(hisTaskInst.getId()).execute(commandContext);
	   
		if (CollectionUtils.isEmpty(tasks)) {
	    	throw new ActivitiObjectNotFoundException("Cannot find task with id " + taskId+" the next run task",Task.class);
	    }
	  
		TaskEntity nextTask = commandContext.getTaskEntityManager().findTaskById(tasks.get(0).getId());
		
	    return super.execute(commandContext, nextTask);
	}
	
	private boolean isNearestHistoricTask(HistoricTaskInstance hisTaskInst){
		List<HistoricTaskInstance> historicTaskInstances = new HistoricTaskInstanceQueryImpl(Context.getCommandContext().getProcessEngineConfiguration().getCommandExecutor())
				.processInstanceId(hisTaskInst.getProcessInstanceId())
				.taskDefinitionKey(hisTaskInst.getTaskDefinitionKey())
				.finished()
				.orderByTaskCreateTime().desc()
				.list();
		if(historicTaskInstances.get(0).getId().equals(hisTaskInst.getId())){
			return true;
		}else{
			return false;
		}
	}
	
}
