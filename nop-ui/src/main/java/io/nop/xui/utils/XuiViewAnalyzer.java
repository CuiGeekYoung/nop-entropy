/**
 * Copyright (c) 2017-2023 Nop Platform. All rights reserved.
 * Author: canonical_entropy@163.com
 * Blog:   https://www.zhihu.com/people/canonical-entropy
 * Gitee:  https://gitee.com/canonical-entropy/nop-chaos
 * Github: https://github.com/entropy-cloud/nop-chaos
 */
package io.nop.xui.utils;

import io.nop.api.core.beans.FieldSelectionBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.api.core.util.Guard;
import io.nop.api.core.util.IComponentModel;
import io.nop.api.core.util.SourceLocation;
import io.nop.commons.collections.IKeyedList;
import io.nop.commons.util.CollectionHelper;
import io.nop.commons.util.StringHelper;
import io.nop.core.CoreConstants;
import io.nop.core.reflect.bean.BeanTool;
import io.nop.core.reflect.hook.IPropGetMissingHook;
import io.nop.core.resource.component.ResourceComponentManager;
import io.nop.xlang.xmeta.IObjMeta;
import io.nop.xlang.xmeta.IObjPropMeta;
import io.nop.xlang.xmeta.SchemaLoader;
import io.nop.xlang.xmeta.layout.ILayoutGroupModel;
import io.nop.xlang.xmeta.layout.LayoutTableModel;
import io.nop.xui.XuiConstants;
import io.nop.xui.model.UiDisplayMeta;
import io.nop.xui.model.UiFormCellModel;
import io.nop.xui.model.UiFormModel;
import io.nop.xui.model.UiGridColModel;
import io.nop.xui.model.UiGridModel;
import io.nop.xui.model.UiRefViewModel;

import java.util.Set;
import java.util.function.Consumer;

import static io.nop.xui.XuiConstants.GRAPHQL_JSON_COMPONENT_PROP;
import static io.nop.xui.XuiConstants.GRAPHQL_LABEL_PROP;
import static io.nop.xui.XuiErrors.ARG_CELL_ID;
import static io.nop.xui.XuiErrors.ARG_COL_ID;
import static io.nop.xui.XuiErrors.ARG_FORM_ID;
import static io.nop.xui.XuiErrors.ARG_GRID_ID;
import static io.nop.xui.XuiErrors.ARG_PROP_NAME;
import static io.nop.xui.XuiErrors.ARG_VIEW_PATH;
import static io.nop.xui.XuiErrors.ERR_XUI_FORM_CELL_UNKNOWN_DEPEND;
import static io.nop.xui.XuiErrors.ERR_XUI_GRID_COL_UNKNOWN_DEPEND;
import static io.nop.xui.XuiErrors.ERR_XUI_UNKNOWN_FORM;
import static io.nop.xui.XuiErrors.ERR_XUI_UNKNOWN_GRID;
import static io.nop.xui.XuiErrors.ERR_XUI_UNKNOWN_JSON_COMPONENT_PROP;

public class XuiViewAnalyzer {

    /**
     * 分析表格对象，得到字段对应的GraphQL selection描述
     */
    public FieldSelectionBean getListSelection(UiGridModel gridModel, IObjMeta objMeta) {
        FieldSelectionBean selection = new FieldSelectionBean();
        appendPkFields(selection, objMeta);

        for (UiGridColModel colModel : gridModel.getCols()) {
            String prop = colModel.getProp() == null ? colModel.getId() : colModel.getProp();
            IObjPropMeta propMeta = objMeta.getProp(prop);
            if (propMeta != null) {
                if (!propMeta.isReadable())
                    continue;

                addRelationDispProp(propMeta, objMeta, selection);
                if (!prop.equals(colModel.getId())) {
                    selection.addCompositeField(colModel.getId(), true).setName(prop);
                } else {
                    selection.addCompositeField(colModel.getId(), false);
                }
                addLabelProp(propMeta, selection);
                addJsonComponent(propMeta, objMeta, selection);
                addDepend(propMeta.getDepends(), objMeta, name -> {
                    selection.addCompositeField(name, false);
                }, name -> {
                    throw new NopException(ERR_XUI_GRID_COL_UNKNOWN_DEPEND).source(colModel)
                            .param(ARG_GRID_ID, gridModel.getId()).param(ARG_COL_ID, colModel.getId())
                            .param(ARG_PROP_NAME, name);
                });
            }

            Set<String> depends = colModel.getDepends();
            addDepend(depends, objMeta, name -> {
                selection.addCompositeField(name, false);
            }, name -> {
                throw new NopException(ERR_XUI_GRID_COL_UNKNOWN_DEPEND).source(colModel)
                        .param(ARG_GRID_ID, gridModel.getId()).param(ARG_COL_ID, colModel.getId())
                        .param(ARG_PROP_NAME, name);
            });

            if (colModel.getSelection() != null) {
                selection.addCompositeField(colModel.getId(), true).mergeFields(colModel.getSelection().getFields());
            } else {
                addViewDepends(selection, colModel);
            }
        }

        if (gridModel.getSelection() != null) {
            selection.merge(gridModel.getSelection());
        }
        return selection;
    }

    public FieldSelectionBean getFormSelection(UiFormModel formModel, IObjMeta objMeta) {
        Guard.notNull(formModel, "formModel");
        Guard.notNull(objMeta, "objMeta");

        FieldSelectionBean selection = new FieldSelectionBean();
        appendPkFields(selection, objMeta);

        if (formModel.getLayout() != null) {
            for (ILayoutGroupModel group : formModel.getLayout().getGroups()) {
                collectSelection(selection, group, objMeta, formModel);
            }

            if (formModel.getSelection() != null) {
                selection.merge(formModel.getSelection());
            }
        }
        return selection;
    }

    void appendPkFields(FieldSelectionBean selection, IObjMeta objMeta) {
        if (!CollectionHelper.isEmpty(objMeta.getPrimaryKey())) {
            // 总是选择主键
            selection.addField(XuiConstants.PROP_ID);
            for (String pk : objMeta.getPrimaryKey()) {
                selection.addField(pk);
            }
        }
    }

    private void collectSelection(FieldSelectionBean selection, ILayoutGroupModel group, IObjMeta objMeta,
                                  UiFormModel formModel) {

        LayoutTableModel table = (LayoutTableModel) group;
        table.forEachLayoutCell(lc -> {
            UiFormCellModel cellModel = formModel.getCell(lc.getId());
            String prop = cellModel == null || cellModel.getProp() == null ? lc.getId() : cellModel.getProp();
            IObjPropMeta propMeta = objMeta.getProp(prop);
            if (propMeta != null) {
                // 不可读的数据不会进入selection
                if (!propMeta.isReadable())
                    return;

                addRelationDispProp(propMeta, objMeta, selection);

                if (!prop.equals(lc.getId())) {
                    selection.addCompositeField(lc.getId(), true).setName(prop);
                } else {
                    selection.addCompositeField(lc.getId(), false);
                }
                addLabelProp(propMeta, selection);
                addJsonComponent(propMeta, objMeta, selection);
                addDepend(propMeta.getDepends(), objMeta, name -> {
                    selection.addCompositeField(name, false);
                }, name -> {
                    throw new NopException(ERR_XUI_FORM_CELL_UNKNOWN_DEPEND).source(cellModel)
                            .param(ARG_FORM_ID, formModel.getId()).param(ARG_CELL_ID, lc.getId())
                            .param(ARG_PROP_NAME, name);
                });
            }

            if (cellModel != null) {
                Set<String> depends = cellModel.getDepends();
                addDepend(depends, objMeta, name -> {
                    selection.addCompositeField(name, false);
                }, name -> {
                    throw new NopException(ERR_XUI_FORM_CELL_UNKNOWN_DEPEND).source(cellModel)
                            .param(ARG_FORM_ID, formModel.getId()).param(ARG_CELL_ID, lc.getId())
                            .param(ARG_PROP_NAME, name);
                });

                if (cellModel.getSelection() != null) {
                    selection.addCompositeField(lc.getId(), true).mergeFields(cellModel.getSelection().getFields());
                } else {
                    addViewDepends(selection, cellModel);
                }
            }
        });
    }

    private void addJsonComponent(IObjPropMeta propMeta, IObjMeta objMeta, FieldSelectionBean selection) {
        String jsonComponentProp = (String) propMeta.prop_get(GRAPHQL_JSON_COMPONENT_PROP);
        if (!StringHelper.isEmpty(jsonComponentProp)) {
            IObjPropMeta jsonProp = objMeta.getProp(jsonComponentProp);
            if (jsonProp == null)
                throw new NopException(ERR_XUI_UNKNOWN_JSON_COMPONENT_PROP)
                        .source(propMeta)
                        .param(ARG_PROP_NAME, jsonComponentProp);
            selection.addField(jsonComponentProp);
        }
    }

    void addViewDepends(FieldSelectionBean selection, UiDisplayMeta dispMeta) {
        UiRefViewModel refView = dispMeta.getView();
        if (refView == null)
            return;

        refView.validate();

        String fileType = StringHelper.fileType(refView.getPath());
        if (!XuiConstants.FILE_TYPE_VIEW_XML.equals(fileType))
            return;

        // 分析view模型得到关联子表选项
        IComponentModel viewModel = loadViewModel(refView.getPath());
        IObjMeta viewObjMeta = getViewObjMeta(viewModel);
        IPropGetMissingHook page = getPage(viewModel, refView.getPage());
        if (page != null) {
            UiFormModel formModel = getForm(refView.getLocation(), viewModel, page);
            if (formModel != null) {
                FieldSelectionBean subSelection = getFormSelection(formModel, getObjMeta(formModel, viewObjMeta));
                selection.addCompositeField(dispMeta.getId(), true).mergeFields(subSelection.getFields());
            } else {
                UiGridModel gridModel = getGrid(refView.getLocation(), viewModel, page);
                if (gridModel != null) {
                    FieldSelectionBean subSelection = getListSelection(gridModel, getObjMeta(gridModel, viewObjMeta));
                    selection.addCompositeField(dispMeta.getId(), true).mergeFields(subSelection.getFields());
                }
            }
        } else {
            UiFormModel formModel = getForm(refView.getLocation(), viewModel, refView.getForm());
            if (formModel != null) {
                FieldSelectionBean subSelection = getFormSelection(formModel, getObjMeta(formModel, viewObjMeta));
                selection.addCompositeField(dispMeta.getId(), true).mergeFields(subSelection.getFields());
            } else {
                UiGridModel gridModel = getGrid(refView.getLocation(), viewModel, refView.getGrid());
                if (gridModel != null) {
                    FieldSelectionBean subSelection = getListSelection(gridModel, getObjMeta(gridModel, viewObjMeta));
                    selection.addCompositeField(dispMeta.getId(), true).mergeFields(subSelection.getFields());
                }
            }
        }
    }

    private IObjMeta getObjMeta(UiFormModel formModel, IObjMeta viewObjMeta) {
        if (!StringHelper.isEmpty(formModel.getObjMeta()))
            return formModel.loadObjMeta();
        return viewObjMeta;
    }

    private IObjMeta getObjMeta(UiGridModel gridModel, IObjMeta viewObjMeta) {
        if (!StringHelper.isEmpty(gridModel.getObjMeta()))
            return gridModel.loadObjMeta();
        return viewObjMeta;
    }

    static void addDepend(Set<String> depends, IObjMeta objMeta, Consumer<String> action, Consumer<String> onMissing) {
        if (depends == null || depends.isEmpty())
            return;

        for (String depend : depends) {
            // 后台识别的特殊字段或者忽略配置
            if (depend.startsWith(CoreConstants.PREFIX_INTERNAL_DEPENDS))
                continue;

            String prop = StringHelper.firstPart(depend, '.');
            if (objMeta.getProp(prop) == null) {
                onMissing.accept(depend);
            } else {
                action.accept(depend);
            }
        }
    }

    void addRelationDispProp(IObjPropMeta propMeta, IObjMeta objMeta, FieldSelectionBean selection) {
        // 主键上的一对一关联被忽略
        if (objMeta.isPrimaryKeyProp(propMeta.getName()))
            return;

        IObjPropMeta relProp = XuiHelper.getRelationProp(propMeta, objMeta);
        if (relProp != null) {
            if (!addLabelProp(propMeta, selection)) {
                String rightProp = (String) relProp.prop_get(XuiConstants.EXT_JOIN_RIGHT_DISPLAY_PROP);
                if (!StringHelper.isEmpty(rightProp)) {
                    selection.addCompositeField(relProp.getName() + '.' + rightProp, false);
                }
            }
        }
    }

    boolean addLabelProp(IObjPropMeta propMeta, FieldSelectionBean selection) {
        String labelProp = getLabelProp(propMeta);
        if (!StringHelper.isEmpty(labelProp)) {
            selection.addField(labelProp);
            return true;
        }
        return false;
    }

    String getLabelProp(IObjPropMeta propMeta) {
        String labelProp = (String) propMeta.prop_get(GRAPHQL_LABEL_PROP);
        return labelProp;
    }

    static IComponentModel loadViewModel(String viewPath) {
        return ResourceComponentManager.instance().loadComponentModel(viewPath);
    }

    static IObjMeta getViewObjMeta(IComponentModel viewModel) {
        String objMetaPath = (String) BeanTool.instance().getProperty(viewModel, "objMeta");
        if (StringHelper.isEmpty(objMetaPath))
            return null;

        return SchemaLoader.loadXMeta(objMetaPath);
    }

    static IKeyedList<UiGridModel> getGrids(IComponentModel viewModel) {
        return (IKeyedList<UiGridModel>) BeanTool.instance().getProperty(viewModel, "grids");
    }

    static IKeyedList<UiFormModel> getForms(IComponentModel viewModel) {
        return (IKeyedList<UiFormModel>) BeanTool.instance().getProperty(viewModel, "forms");
    }

    static IKeyedList<IPropGetMissingHook> getPages(IComponentModel viewModel) {
        return (IKeyedList<IPropGetMissingHook>) BeanTool.instance().getProperty(viewModel, "pages");
    }

    static IPropGetMissingHook getPage(IComponentModel viewModel, String pageId) {
        IKeyedList<IPropGetMissingHook> pages = getPages(viewModel);
        return pages == null ? null : pages.getByKey(pageId);
    }

    static UiGridModel getGrid(SourceLocation loc, IComponentModel viewModel, IPropGetMissingHook page) {
        if (page.prop_has("grid")) {
            String gridId = (String) page.prop_get("grid");
            return getGrid(loc, viewModel, gridId);
        }
        return null;
    }

    static UiGridModel getGrid(SourceLocation loc, IComponentModel viewModel, String gridId) {
        if (StringHelper.isEmpty(gridId))
            return null;
        IKeyedList<UiGridModel> grids = getGrids(viewModel);

        UiGridModel grid = grids == null ? null : grids.getByKey(gridId);
        if (grid == null)
            throw new NopException(ERR_XUI_UNKNOWN_GRID).loc(loc).param(ARG_VIEW_PATH, viewModel.resourcePath())
                    .param(ARG_GRID_ID, gridId);

        return grid;
    }

    static UiFormModel getForm(SourceLocation loc, IComponentModel viewModel, IPropGetMissingHook page) {
        if (page.prop_has("form")) {

            String formId = (String) page.prop_get("form");
            return getForm(loc, viewModel, formId);
        }
        return null;
    }

    static UiFormModel getForm(SourceLocation loc, IComponentModel viewModel, String formId) {
        if (StringHelper.isEmpty(formId)) {
            return null;
        }
        IKeyedList<UiFormModel> forms = getForms(viewModel);
        UiFormModel form = forms == null ? null : forms.getByKey(formId);
        if (form == null)
            throw new NopException(ERR_XUI_UNKNOWN_FORM).loc(loc).param(ARG_VIEW_PATH, viewModel.resourcePath())
                    .param(ARG_FORM_ID, formId);
        return form;
    }
}