package io.nop.task.model._gen;

import io.nop.commons.collections.KeyedList; //NOPMD - suppressed UnusedImports - Used for List Prop
import io.nop.core.lang.json.IJsonHandler;



// tell cpd to start ignoring code - CPD-OFF
/**
 * generate from [109:14:0:0]/nop/schema/task/task.xdef <p>
 * 
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
    "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public abstract class _SleepTaskStepModel extends io.nop.task.model.TaskStepModel {
    
    /**
     *  
     * xml name: sleepTime
     * 
     */
    private io.nop.core.lang.eval.IEvalAction _sleepTime ;
    
    /**
     * 
     * xml name: sleepTime
     *  
     */
    
    public io.nop.core.lang.eval.IEvalAction getSleepTime(){
      return _sleepTime;
    }

    
    public void setSleepTime(io.nop.core.lang.eval.IEvalAction value){
        checkAllowChange();
        
        this._sleepTime = value;
           
    }

    

    public void freeze(boolean cascade){
        if(frozen()) return;
        super.freeze(cascade);

        if(cascade){ //NOPMD - suppressed EmptyControlStatement - Auto Gen Code
        
        }
    }

    protected void outputJson(IJsonHandler out){
        super.outputJson(out);
        
        out.put("sleepTime",this.getSleepTime());
    }
}
 // resume CPD analysis - CPD-ON