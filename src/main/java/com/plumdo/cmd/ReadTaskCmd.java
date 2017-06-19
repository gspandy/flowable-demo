package com.plumdo.cmd;

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

import cn.starnet.activiti.engine.db.entity.CcInfo;
import cn.starnet.activiti.engine.db.entity.impl.HistoricCcInfoEntity;
import cn.starnet.activiti.engine.db.entity.manager.CcInfoEntityManger;
import cn.starnet.activiti.engine.db.entity.manager.HistoricCcInfoEntityManger;

/**
 * 查阅任务
 * @author wengwh
 *
 */
public class ReadTaskCmd implements Command<Void>, Serializable  {

	private static final long serialVersionUID = 1L;
	private String taskId;
	private String assignee;
  
	public ReadTaskCmd(String taskId,String assignee) {
		this.taskId = taskId;
		this.assignee = assignee;
	}
  
	public Void execute(CommandContext commandContext) {
		
		List<CcInfo> ccInfos = commandContext.getSession(CcInfoEntityManger.class).createNewCcInfoQuery().taskId(taskId).assignee(assignee).list();

		if(ccInfos == null || ccInfos.size()==0){
			throw new ActivitiObjectNotFoundException("Cannot find cc task with id " + taskId + " and assignee " + assignee, CcInfo.class);
		}
		
		for(CcInfo ccInfo : ccInfos){
			commandContext.getSession(CcInfoEntityManger.class).deleteCcInfo(ccInfo.getId());
		    
		    HistoricCcInfoEntity historicCcInfo = commandContext.getSession(HistoricCcInfoEntityManger.class).findHistoricCcInfoById(ccInfo.getId());
		    historicCcInfo.setEndTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
		    historicCcInfo.setDurationInMillis(historicCcInfo.getEndTime().getTime()-historicCcInfo.getStartTime().getTime());
		    historicCcInfo.setDeleteReason(HistoricCcInfoEntity.ACTIVITI_DELETED);

		    commandContext.getSession(HistoricCcInfoEntityManger.class).updateHistoricCcInfo(historicCcInfo);
		}
	    
	    return null;
	}

}
