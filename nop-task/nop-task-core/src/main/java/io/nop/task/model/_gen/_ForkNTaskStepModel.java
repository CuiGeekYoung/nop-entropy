package io.nop.task.model._gen;

import io.nop.commons.collections.KeyedList; //NOPMD - suppressed UnusedImports - Used for List Prop
import io.nop.core.lang.json.IJsonHandler;



// tell cpd to start ignoring code - CPD-OFF
/**
 * generate from [117:14:0:0]/nop/schema/task/task.xdef <p>
 * 
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
    "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public abstract class _ForkNTaskStepModel extends io.nop.task.model.TaskStepsModel {
    
    /**
     *  
     * xml name: aggregator
     * 
     */
    private io.nop.task.model.TaskBeanModel _aggregator ;
    
    /**
     *  
     * xml name: autoCancelUnfinished
     * 
     */
    private boolean _autoCancelUnfinished  = false;
    
    /**
     *  
     * xml name: count
     * 
     */
    private io.nop.core.lang.eval.IEvalAction _count ;
    
    /**
     *  
     * xml name: joinType
     * 
     */
    private io.nop.task.model.TaskStepJoinType _joinType ;
    
    /**
     *  
     * xml name: var
     * 
     */
    private java.lang.String _var ;
    
    /**
     * 
     * xml name: aggregator
     *  
     */
    
    public io.nop.task.model.TaskBeanModel getAggregator(){
      return _aggregator;
    }

    
    public void setAggregator(io.nop.task.model.TaskBeanModel value){
        checkAllowChange();
        
        this._aggregator = value;
           
    }

    
    /**
     * 
     * xml name: autoCancelUnfinished
     *  
     */
    
    public boolean isAutoCancelUnfinished(){
      return _autoCancelUnfinished;
    }

    
    public void setAutoCancelUnfinished(boolean value){
        checkAllowChange();
        
        this._autoCancelUnfinished = value;
           
    }

    
    /**
     * 
     * xml name: count
     *  
     */
    
    public io.nop.core.lang.eval.IEvalAction getCount(){
      return _count;
    }

    
    public void setCount(io.nop.core.lang.eval.IEvalAction value){
        checkAllowChange();
        
        this._count = value;
           
    }

    
    /**
     * 
     * xml name: joinType
     *  
     */
    
    public io.nop.task.model.TaskStepJoinType getJoinType(){
      return _joinType;
    }

    
    public void setJoinType(io.nop.task.model.TaskStepJoinType value){
        checkAllowChange();
        
        this._joinType = value;
           
    }

    
    /**
     * 
     * xml name: var
     *  
     */
    
    public java.lang.String getVar(){
      return _var;
    }

    
    public void setVar(java.lang.String value){
        checkAllowChange();
        
        this._var = value;
           
    }

    

    public void freeze(boolean cascade){
        if(frozen()) return;
        super.freeze(cascade);

        if(cascade){ //NOPMD - suppressed EmptyControlStatement - Auto Gen Code
        
           this._aggregator = io.nop.api.core.util.FreezeHelper.deepFreeze(this._aggregator);
            
        }
    }

    protected void outputJson(IJsonHandler out){
        super.outputJson(out);
        
        out.put("aggregator",this.getAggregator());
        out.put("autoCancelUnfinished",this.isAutoCancelUnfinished());
        out.put("count",this.getCount());
        out.put("joinType",this.getJoinType());
        out.put("var",this.getVar());
    }
}
 // resume CPD analysis - CPD-ON