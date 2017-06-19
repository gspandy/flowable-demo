package com.plumdo.cmd;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

/**
 * 查找下一个节点
 * @author wengwh
 *
 */
public class FindNextActivitiesCmd implements Command<List<PvmActivity>> {
    private String processDefinitionId;
    private String activityId;

    public FindNextActivitiesCmd(String processDefinitionId, String activityId) {
        this.processDefinitionId = processDefinitionId;
        this.activityId = activityId;
    }

    public List<PvmActivity> execute(CommandContext commandContext) {
        ProcessDefinitionEntity processDefinitionEntity = Context
                .getProcessEngineConfiguration().getDeploymentManager()
                .findDeployedProcessDefinitionById(processDefinitionId);

        if (processDefinitionEntity == null) {
            throw new IllegalArgumentException(
                    "cannot find processDefinition : " + processDefinitionId);
        }

        ActivityImpl activity = processDefinitionEntity
                .findActivity(activityId);

        return this.getNextActivities(activity);
    }
    
    
    public List<PvmActivity> getNextActivities(PvmActivity pvmActivity) {
        List<PvmActivity> pvmActivities = new ArrayList<PvmActivity>();
        //外嵌子流程无法在子流程的定义去获取父流程的定义，所以直接返回空
        for (PvmTransition pvmTransition : pvmActivity.getOutgoingTransitions()) {
            PvmActivity targetActivity = pvmTransition.getDestination();

            if ("userTask".equals(targetActivity.getProperty("type"))) {
                pvmActivities.add(targetActivity);
            } else if("subProcess".equals(targetActivity.getProperty("type"))){
                for(PvmActivity activity : targetActivity.getActivities()){
                	if("startEvent".equals(activity.getProperty("type"))){
                		pvmActivities.addAll(this.getNextActivities(activity));
                		break;
                	}
                }
                
            } else if("endEvent".equals(targetActivity.getProperty("type"))){
            	
            	 if(targetActivity.getParent()!=null){
					 if(targetActivity.getParent() instanceof ActivityImpl){
						 pvmActivities.addAll(this.getNextActivities((ActivityImpl)targetActivity.getParent()));
					 }
				 }
            	 
            } else if("callActivity".equals(targetActivity.getProperty("type"))){
                pvmActivities.add(targetActivity);
            } else {
                pvmActivities.addAll(this.getNextActivities(targetActivity));
            }
        }

        return pvmActivities;
    }
}
