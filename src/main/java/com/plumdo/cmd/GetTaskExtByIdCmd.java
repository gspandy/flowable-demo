package com.plumdo.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

import cn.starnet.activiti.engine.db.entity.impl.TaskExtEntity;
import cn.starnet.activiti.engine.db.entity.manager.TaskExtEntityManger;

/**
 * 根据ID获取任务
 * @author wengwh
 *
 */
public class GetTaskExtByIdCmd implements Command<TaskExtEntity>, Serializable {

	private static final long serialVersionUID = 1L;
	private String taskId;
  
	public GetTaskExtByIdCmd(String taskId) {
		this.taskId = taskId;
	}

	public TaskExtEntity execute(CommandContext commandContext) {
		if(taskId == null) {
			throw new ActivitiIllegalArgumentException("taskId is null");
	    }

	    return commandContext.getSession(TaskExtEntityManger.class).findTaskExtById(taskId);
	}

}
