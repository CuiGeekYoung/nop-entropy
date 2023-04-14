package io.nop.rule.core.model._gen;

import io.nop.commons.collections.KeyedList; //NOPMD - suppressed UnusedImports - Used for List Prop
import io.nop.core.lang.json.IJsonHandler;



// tell cpd to start ignoring code - CPD-OFF
/**
 * generate from [17:6:0:0]/nop/schema/rule.xdef <p>
 * 
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
    "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public abstract class _RuleDeciderModel extends io.nop.core.resource.component.AbstractComponentModel {
    
    /**
     *  
     * xml name: children
     * 
     */
    private KeyedList<io.nop.rule.core.model.RuleDeciderModel> _children = KeyedList.emptyList();
    
    /**
     *  
     * xml name: label
     * 
     */
    private java.lang.String _label ;
    
    /**
     *  
     * xml name: multiMatch
     * 
     */
    private boolean _multiMatch  = false;
    
    /**
     *  
     * xml name: name
     * 
     */
    private java.lang.String _name ;
    
    /**
     *  
     * xml name: outputs
     * 
     */
    private KeyedList<io.nop.rule.core.model.RuleOutputValueModel> _outputs = KeyedList.emptyList();
    
    /**
     *  
     * xml name: predicate
     * 
     */
    private io.nop.api.core.beans.TreeBean _predicate ;
    
    /**
     * 
     * xml name: children
     *  
     */
    
    public java.util.List<io.nop.rule.core.model.RuleDeciderModel> getChildren(){
      return _children;
    }

    
    public void setChildren(java.util.List<io.nop.rule.core.model.RuleDeciderModel> value){
        checkAllowChange();
        
        this._children = KeyedList.fromList(value, io.nop.rule.core.model.RuleDeciderModel::getName);
           
    }

    
    public io.nop.rule.core.model.RuleDeciderModel getChild(String name){
        return this._children.getByKey(name);
    }

    public boolean hasChild(String name){
        return this._children.containsKey(name);
    }

    public void addChild(io.nop.rule.core.model.RuleDeciderModel item) {
        checkAllowChange();
        java.util.List<io.nop.rule.core.model.RuleDeciderModel> list = this.getChildren();
        if (list == null || list.isEmpty()) {
            list = new KeyedList<>(io.nop.rule.core.model.RuleDeciderModel::getName);
            setChildren(list);
        }
        list.add(item);
    }
    
    public java.util.Set<String> keySet_children(){
        return this._children.keySet();
    }

    public boolean hasChildren(){
        return !this._children.isEmpty();
    }
    
    /**
     * 
     * xml name: label
     *  
     */
    
    public java.lang.String getLabel(){
      return _label;
    }

    
    public void setLabel(java.lang.String value){
        checkAllowChange();
        
        this._label = value;
           
    }

    
    /**
     * 
     * xml name: multiMatch
     *  
     */
    
    public boolean isMultiMatch(){
      return _multiMatch;
    }

    
    public void setMultiMatch(boolean value){
        checkAllowChange();
        
        this._multiMatch = value;
           
    }

    
    /**
     * 
     * xml name: name
     *  
     */
    
    public java.lang.String getName(){
      return _name;
    }

    
    public void setName(java.lang.String value){
        checkAllowChange();
        
        this._name = value;
           
    }

    
    /**
     * 
     * xml name: outputs
     *  
     */
    
    public java.util.List<io.nop.rule.core.model.RuleOutputValueModel> getOutputs(){
      return _outputs;
    }

    
    public void setOutputs(java.util.List<io.nop.rule.core.model.RuleOutputValueModel> value){
        checkAllowChange();
        
        this._outputs = KeyedList.fromList(value, io.nop.rule.core.model.RuleOutputValueModel::getName);
           
    }

    
    public io.nop.rule.core.model.RuleOutputValueModel getOutput(String name){
        return this._outputs.getByKey(name);
    }

    public boolean hasOutput(String name){
        return this._outputs.containsKey(name);
    }

    public void addOutput(io.nop.rule.core.model.RuleOutputValueModel item) {
        checkAllowChange();
        java.util.List<io.nop.rule.core.model.RuleOutputValueModel> list = this.getOutputs();
        if (list == null || list.isEmpty()) {
            list = new KeyedList<>(io.nop.rule.core.model.RuleOutputValueModel::getName);
            setOutputs(list);
        }
        list.add(item);
    }
    
    public java.util.Set<String> keySet_outputs(){
        return this._outputs.keySet();
    }

    public boolean hasOutputs(){
        return !this._outputs.isEmpty();
    }
    
    /**
     * 
     * xml name: predicate
     *  
     */
    
    public io.nop.api.core.beans.TreeBean getPredicate(){
      return _predicate;
    }

    
    public void setPredicate(io.nop.api.core.beans.TreeBean value){
        checkAllowChange();
        
        this._predicate = value;
           
    }

    

    public void freeze(boolean cascade){
        if(frozen()) return;
        super.freeze(cascade);

        if(cascade){ //NOPMD - suppressed EmptyControlStatement - Auto Gen Code
        
           this._children = io.nop.api.core.util.FreezeHelper.deepFreeze(this._children);
            
           this._outputs = io.nop.api.core.util.FreezeHelper.deepFreeze(this._outputs);
            
           this._predicate = io.nop.api.core.util.FreezeHelper.deepFreeze(this._predicate);
            
        }
    }

    protected void outputJson(IJsonHandler out){
        super.outputJson(out);
        
        out.put("children",this.getChildren());
        out.put("label",this.getLabel());
        out.put("multiMatch",this.isMultiMatch());
        out.put("name",this.getName());
        out.put("outputs",this.getOutputs());
        out.put("predicate",this.getPredicate());
    }
}
 // resume CPD analysis - CPD-ON