���Ǽ����ר�ң���ͨԪģ�͡�Ԫ���ݵȸ���ϸ���ѭҵ��ͨ�õı���淶��SQL��������֪��ʲô�ǲ����������XML������������������Ҫ������������󲢷��ضԱ���Ĳ�������

��֪orders��Ľṹ����:

```xml
 <entity name="orders" displayName="����">  
            <columns>  
                <column name="id" displayName="ID" mandatory="true" primary="true" sqlType="INT" precision="11" scale="0" />  
                <column name="user_id" displayName="�û�ID" mandatory="true" sqlType="INT" precision="11" scale="0" orm:ref-table="users"/>  
                <column name="product_id" displayName="��ƷID" mandatory="true" sqlType="INT" precision="11" scale="0" orm:ref-table="products"/>  
                <column name="quantity" displayName="����" mandatory="true" sqlType="INT" precision="11" scale="0"/>  
            </columns>  
        </entity>
```

�����ֶ�a��ɾ���ֶ�quantity�Լ��޸��ֶ�c�Ĳ�����������

```xml
<entity name="orders">
   <columns>
      <column name="a" displayName="�����ֶ�" mandatory="true" sqlType="INT" />
      <column name="quantity" x:override="remove" />
      <column name="c" displayName="�޸ĵ��ֶ���" />
   </columns>
</entity>
```



�����������£�

```
����״̬��ص��ֶΣ��Լ���Ȩ�޹����ֶΣ����⽫id�ֶε������޸�Ϊ�ַ�����ɾ���ֶ�user_id
```    


���⣺

�������޸ĵ��ֶζ�������Ӧ�Ĳ���XML��ʲô��