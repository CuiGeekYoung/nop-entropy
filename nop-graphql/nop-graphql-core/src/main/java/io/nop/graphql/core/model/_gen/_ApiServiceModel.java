package io.nop.graphql.core.model._gen;

import io.nop.commons.collections.KeyedList; //NOPMD NOSONAR - suppressed UnusedImports - Used for List Prop
import io.nop.core.lang.json.IJsonHandler;
import io.nop.graphql.core.model.ApiServiceModel;
import io.nop.commons.util.ClassHelper;



// tell cpd to start ignoring code - CPD-OFF
/**
 * generate from [23:10:0:0]/nop/schema/api.xdef <p>
 * 
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
    "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S101","java:S1128","java:S1161"})
public abstract class _ApiServiceModel extends io.nop.core.resource.component.AbstractComponentModel {
    
    /**
     *  
     * xml name: className
     * 
     */
    private java.lang.String _className ;
    
    /**
     *  
     * xml name: description
     * 
     */
    private java.lang.String _description ;
    
    /**
     *  
     * xml name: displayName
     * 
     */
    private java.lang.String _displayName ;
    
    /**
     *  
     * xml name: method
     * 服务方法。接收ApiRequest<T>类型的请求，返回ApiResponse<R>类型的响应
     */
    private KeyedList<io.nop.graphql.core.model.ApiMethodModel> _methods = KeyedList.emptyList();
    
    /**
     *  
     * xml name: name
     * 
     */
    private java.lang.String _name ;
    
    /**
     *  
     * xml name: tagSet
     * 
     */
    private java.util.Set<java.lang.String> _tagSet ;
    
    /**
     * 
     * xml name: className
     *  
     */
    
    public java.lang.String getClassName(){
      return _className;
    }

    
    public void setClassName(java.lang.String value){
        checkAllowChange();
        
        this._className = value;
           
    }

    
    /**
     * 
     * xml name: description
     *  
     */
    
    public java.lang.String getDescription(){
      return _description;
    }

    
    public void setDescription(java.lang.String value){
        checkAllowChange();
        
        this._description = value;
           
    }

    
    /**
     * 
     * xml name: displayName
     *  
     */
    
    public java.lang.String getDisplayName(){
      return _displayName;
    }

    
    public void setDisplayName(java.lang.String value){
        checkAllowChange();
        
        this._displayName = value;
           
    }

    
    /**
     * 
     * xml name: method
     *  服务方法。接收ApiRequest<T>类型的请求，返回ApiResponse<R>类型的响应
     */
    
    public java.util.List<io.nop.graphql.core.model.ApiMethodModel> getMethods(){
      return _methods;
    }

    
    public void setMethods(java.util.List<io.nop.graphql.core.model.ApiMethodModel> value){
        checkAllowChange();
        
        this._methods = KeyedList.fromList(value, io.nop.graphql.core.model.ApiMethodModel::getName);
           
    }

    
    public io.nop.graphql.core.model.ApiMethodModel getMethod(String name){
        return this._methods.getByKey(name);
    }

    public boolean hasMethod(String name){
        return this._methods.containsKey(name);
    }

    public void addMethod(io.nop.graphql.core.model.ApiMethodModel item) {
        checkAllowChange();
        java.util.List<io.nop.graphql.core.model.ApiMethodModel> list = this.getMethods();
        if (list == null || list.isEmpty()) {
            list = new KeyedList<>(io.nop.graphql.core.model.ApiMethodModel::getName);
            setMethods(list);
        }
        list.add(item);
    }
    
    public java.util.Set<String> keySet_methods(){
        return this._methods.keySet();
    }

    public boolean hasMethods(){
        return !this._methods.isEmpty();
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
     * xml name: tagSet
     *  
     */
    
    public java.util.Set<java.lang.String> getTagSet(){
      return _tagSet;
    }

    
    public void setTagSet(java.util.Set<java.lang.String> value){
        checkAllowChange();
        
        this._tagSet = value;
           
    }

    

    @Override
    public void freeze(boolean cascade){
        if(frozen()) return;
        super.freeze(cascade);

        if(cascade){ //NOPMD - suppressed EmptyControlStatement - Auto Gen Code
        
           this._methods = io.nop.api.core.util.FreezeHelper.deepFreeze(this._methods);
            
        }
    }

    @Override
    protected void outputJson(IJsonHandler out){
        super.outputJson(out);
        
        out.put("className",this.getClassName());
        out.put("description",this.getDescription());
        out.put("displayName",this.getDisplayName());
        out.put("methods",this.getMethods());
        out.put("name",this.getName());
        out.put("tagSet",this.getTagSet());
    }

    public ApiServiceModel cloneInstance(){
        ApiServiceModel instance = newInstance();
        this.copyTo(instance);
        return instance;
    }

    protected void copyTo(ApiServiceModel instance){
        super.copyTo(instance);
        
        instance.setClassName(this.getClassName());
        instance.setDescription(this.getDescription());
        instance.setDisplayName(this.getDisplayName());
        instance.setMethods(this.getMethods());
        instance.setName(this.getName());
        instance.setTagSet(this.getTagSet());
    }

    protected ApiServiceModel newInstance(){
        return (ApiServiceModel) ClassHelper.newInstance(getClass());
    }
}
 // resume CPD analysis - CPD-ON