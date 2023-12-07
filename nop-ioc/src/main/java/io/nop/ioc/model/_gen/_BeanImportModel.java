package io.nop.ioc.model._gen;

import io.nop.commons.collections.KeyedList; //NOPMD - suppressed UnusedImports - Used for List Prop
import io.nop.core.lang.json.IJsonHandler;



// tell cpd to start ignoring code - CPD-OFF
/**
 * generate from [251:6:0:0]/nop/schema/beans.xdef <p>
 * 多次import同一资源只会实际执行一次。所有的bean不允许重名，从而避免出现import顺序不同导致结果不同。
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
    "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116"})
public abstract class _BeanImportModel extends io.nop.core.resource.component.AbstractComponentModel {
    
    /**
     *  
     * xml name: resource
     * 
     */
    private java.lang.String _resource ;
    
    /**
     * 
     * xml name: resource
     *  
     */
    
    public java.lang.String getResource(){
      return _resource;
    }

    
    public void setResource(java.lang.String value){
        checkAllowChange();
        
        this._resource = value;
           
    }

    

    @Override
    public void freeze(boolean cascade){
        if(frozen()) return;
        super.freeze(cascade);

        if(cascade){ //NOPMD - suppressed EmptyControlStatement - Auto Gen Code
        
        }
    }

    @Override
    protected void outputJson(IJsonHandler out){
        super.outputJson(out);
        
        out.put("resource",this.getResource());
    }
}
 // resume CPD analysis - CPD-ON
