package com.plumdo.cmd;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;

/**
 * 通过完成任务节点获取下一步的任务节点集合（包括子流程）
 * @author wengwh
 *
 */
public class GetNextTasksCmd implements Command<List<Task>> {
    private String taskId;

    public GetNextTasksCmd(String taskId) {
        this.taskId = taskId;
    }

    public List<Task> execute(CommandContext commandContext) {
    	HistoricTaskInstance task = commandContext.getHistoricTaskInstanceEntityManager().findHistoricTaskInstanceById(taskId);

    	if(task == null){
    		 throw new IllegalArgumentException("cannot find HistoricTaskInstance : " + taskId);
    	}
    	
        List<PvmActivity> pvmActivities = this.getNextActivities(task.getProcessDefinitionId(),task.getTaskDefinitionKey());
        
        return this.getNextTasks(task.getProcessInstanceId(), pvmActivities);
    }
    
    public List<PvmActivity> getNextActivities(String processDefinitionId,String activityId){
    
    	ProcessDefinitionEntity processDefinitionEntity = Context.getProcessEngineConfiguration().getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);

		if (processDefinitionEntity == null) {
		    throw new IllegalArgumentException("cannot find processDefinition : " + processDefinitionId);
		}
        ActivityImpl activity = processDefinitionEntity.findActivity(activityId);

        List<PvmActivity> pvmActivities = this.getNextActivities(activity);
        
        //多实例串行情况,加入自身节点
        if ("sequential".equals(activity.getProperty("multiInstance"))
        		&&("userTask".equals(activity.getProperty("type"))
    				||"callActivity".equals(activity.getProperty("type")))) {
        	pvmActivities.add(activity);
        }
        return pvmActivities;
		 
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
    
    /**
     * 获取下一步任务，通过下一步节点集合进行匹配(在子流程的情况涉及了历史表，效率问题)
     * @param processInstanceId
     * @param pvmActivities
     * @return
     */
    private List<Task> getNextTasks(String processInstanceId,List<PvmActivity> pvmActivities){

    	List<Task> tasks = new ArrayList<Task>();
		
		List<Task> runTasks = new TaskQueryImpl(Context.getCommandContext()).processInstanceId(processInstanceId).orderByTaskCreateTime().desc().list();

		for(PvmActivity pvmActivity : pvmActivities){
			if("callActivity".equals(pvmActivity.getProperty("type"))){
				//如果下一环节存在外嵌子流程,获取调用节点
				List<HistoricActivityInstance> historicActivityInstances = Context.getProcessEngineConfiguration().getHistoryService()
							.createHistoricActivityInstanceQuery()
							.activityId(pvmActivity.getId())
							.processInstanceId(processInstanceId)
							.unfinished()
							.list();
				
				for(HistoricActivityInstance instance : historicActivityInstances){

					//遍历调用节点，获取子流程实例
					HistoricProcessInstance historicProcessInstance = Context.getCommandContext().getHistoricProcessInstanceEntityManager()
													.findHistoricProcessInstance(instance.getCalledProcessInstanceId());
					
					//判断流程实例是否已经结束，防止子流程串行多实例死循环
					if(historicProcessInstance.getEndTime() == null){
						//获取子流程的开始节点的下一环节
						List<PvmActivity> childPvmActivities = this.getNextActivities(historicProcessInstance.getProcessDefinitionId(),historicProcessInstance.getStartActivityId());
					    //加入子流程的任务   
						tasks.addAll(getNextTasks(instance.getCalledProcessInstanceId(),childPvmActivities));
					}
				}
				
			}else{
				//如果是普通任务，就比较key相同就加入
				for(Task task : runTasks){
					if(pvmActivity.getId().equals(task.getTaskDefinitionKey())){
						tasks.add(task);
					}
				}
			}
		}
		//如果集合为空，可能结束了，判断是否有外嵌父流程
		if(CollectionUtils.isEmpty(tasks) && CollectionUtils.isEmpty(runTasks)){
			HistoricProcessInstance historicProcessInstance = Context.getCommandContext().getHistoricProcessInstanceEntityManager().findHistoricProcessInstance(processInstanceId);

			if(historicProcessInstance.getEndTime()!=null && historicProcessInstance.getSuperProcessInstanceId()!=null){
				List<HistoricActivityInstance> historicActivityInstances = Context.getProcessEngineConfiguration().getHistoryService()
						.createHistoricActivityInstanceQuery()
						.activityType("callActivity")
						.processInstanceId(historicProcessInstance.getSuperProcessInstanceId())
						.finished()
						.orderByHistoricActivityInstanceEndTime()
						.desc()
						.list();
				
				//没有提供查询调用实例ID查询历史节点，只好做循环判断
				for(HistoricActivityInstance instance : historicActivityInstances){
					if(processInstanceId.equals(instance.getCalledProcessInstanceId())){
						//获取调用节点的下一环节
						List<PvmActivity> childPvmActivities = this.getNextActivities(instance.getProcessDefinitionId(),instance.getActivityId());
					   
						tasks.addAll(getNextTasks(instance.getProcessInstanceId(),childPvmActivities));
						
						break;
					}
				}
			}
		}
   	 	return tasks;
	}
}