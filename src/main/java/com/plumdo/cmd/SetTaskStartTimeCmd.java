package com.plumdo.cmd;

import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

public class SetTaskStartTimeCmd extends NeedsActiveTaskCmd<Void> {

	private static final long serialVersionUID = 1L;

	public SetTaskStartTimeCmd(String taskId) {
		super(taskId);
	}

	protected Void execute(CommandContext commandContext, TaskEntity task) {
		task.setCreateTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
		return null;
	}

}
