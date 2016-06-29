
Ext.define('Desktop.CodageWindow2', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'codage-win2',

    init : function(){
        this.launcher = {
            text: 'Codage',
            iconCls:'icon-grid',
            idmenu:"codage"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
       var theapp = this.app;
           var win = desktop.createWindow({
                //itemId: 'grid-win',
                title:'Codages',
				y:0,
				//x:400,
                //width:740,
				x:$(window).width()*0.7,
				width: $(window).width()*0.3,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'fit',
				app:theapp,				
				
					
					items: [
                    
					{						
						xtype:'treepanel',						
						enableDrag:true,												
						layout:'fit',						
						rootVisible: true,
						useArrows: true,
						frame: false,	
						exportCodage:function(record) {
							var win = Ext.create('Desktop.DownloadCodage');		
							win.app = this;
							win.recordCodage = record;
							win.show();
							
						}	,					
						viewConfig: {	
							copy:true,
							listeners : {
								 refresh : function (dataview) {
									
								  Ext.each(dataview.panel.columns, function (column) {
								   if (column.autoSizeColumn === true) {
										//console.log("autosize");
										column.autoSize();
									}
								  });								 
								 }
								 
								},
							getRowClass: function( record, index ) {
								if (record.get('checked') == false || hasParentInactive(record)) {
									//return 'rowinactive'; 
									return "x-item-disabled";
								} else if (record.get('type')=="codageString") {
									return "codage";
								} else {
									return '';
								}
							}		,
							plugins: {
											ptype: 'treeviewdragdrop',											
											ddGroup : 'modaliteDrop',
											enableDrag:true,
											nodeHighlightOnDrop: true,											
											dragText: 'Drag and drop to add to workflow'
										}		
										
						},
						selType: 'rowmodel',
						plugins: [
							Ext.create('Ext.grid.plugin.CellEditing', {
								clicksToEdit: 1								
							})
						],			
						
						editCodage:function(record) {
							if (record.get("type")=='range') { // TODO : quand tous les types seront gerés dans editcodage.js alors supprimer la condition 
								var win = Ext.create('Desktop.EditCodage',{recordCodage:record,codageTree:this});	
								win.show();
							}
							
						}	,
						
						columns:[
							{
								xtype: 'treecolumn',
								text:'',
								autoSizeColumn : true,
							    listeners : {
									beforecheckchange:function() {
									
									}
								},
								//autoSizeColumn: true ,
								renderer : function (value, metadata, record, rowIndex, colIndex, store){	
									if (record.get('type') == 'variable') {
										value = "Variable";
										metadata.tdCls = 'bold-cell'; 			
										//metadata.tdAttr  = 'data-qtip="'+removeQName(record.get("fullpath"))+'"';	
									} else
									if (record.get('type') == 'codageString') {
										value = "Codage ("+record.get("count")+")";
										//metadata.tdAttr  = 'data-qtip="nouvelle modalité"';		
									} else
									if (record.get('type') == 'codageNumeric') {
										value = "Codage ("+record.get("count")+")";
										//metadata.tdAttr  = 'data-qtip="nouvelle modalité"';		
									} else	
									if (record.get('type') == 'modalite') {
										value = "Modalite ("+record.get("count")+")";
										//metadata.tdAttr  = 'data-qtip="ancienne modalité"';	
										metadata.tdCls = 'italic-cell'; 		
									} else if (record.get('type') == 'range') {
										value = "Plage ("+record.get("count")+")";
										//metadata.tdAttr  = 'data-qtip="ancienne modalité"';	
										metadata.tdCls = 'italic-cell'; 		
									} 

									
									return value;
								}
							},
							{ 
							flex:1,
								renderer : function (value, metadata, record, rowIndex, colIndex, store){	
									if (record.get('type') == 'variable') {
										value = record.get("lastnode");
										metadata.tdCls = 'bold-cell'; 			
										metadata.tdAttr  = 'data-qtip="'+removeQName(record.get("fullpath"))+'"';	
									}
									if (record.get('type') == 'codageString') {
										value = record.get("newValue");
										metadata.tdAttr  = 'data-qtip="nouvelle modalité"';		
									}
									if (record.get('type') == 'codageNumeric') {
										value = record.get("newValue");
										metadata.tdAttr  = 'data-qtip="nouvelle valeure"';		
									}
									if (record.get('type') == 'modalite') {
										value = record.get("modalite");
										metadata.tdAttr  = 'data-qtip="ancienne modalité"';	
										metadata.tdCls = 'italic-cell'; 		
									}
									
									if (record.get('type') == 'range') {
										var fromValue = record.get("minValue");
										var toValue = record.get("maxValue");
										if (fromValue!=null && toValue!=null) {
											value = "["+fromValue+";"+toValue+"[";
										} else if (fromValue!=null) {
											value = ">="+fromValue;
										} else {
											value = "<"+toValue;
										}
										metadata.tdAttr  = 'data-qtip="anciennes valeures"';	
										metadata.tdCls = 'italic-cell'; 		
									}
									
									
									return value;
								},
								getEditor: function(record){	
										
										if (record.get('checked') == false || hasParentInactive(record)) {
											
											
											return;
										}
										
										//console.log("getEditor",record);
										if (record.get('type') == "range") {
											return null;
										} else
										if (record.get('type') == "codageNumeric") {
											var me = this.up("treepanel")	;										
											var comp = Ext.create('Ext.grid.CellEditor', {								
												field: {
													xtype:"textfield", 
													emptyText:'(vide)',
													value:record.get("newValue"),
													
													enableKeyEvents: true,
													listeners: {															
														blur:function( field, event, eOpts ) {
															record.set("newValue",field.getValue());
															me.save();
														},
														
														specialkey: function (field, e) {
																if (e.getKey() == e.ENTER) {
																	//field.setValue(field.getRawValue());
																	record.set("newValue",field.getValue());
																}
															}
															
														} // listeners
													}
											});

											return comp;
										} else	
										if (record.get('type') == "codageString") {
											
											var mods=[record.get('newValue')];
											
											var newValue = record.get('newValue'); // on ajoute la valeur initiale (sans la perdre car dans le champs newValue)
											if (mods.indexOf(newValue)<0) { // si cette valeur n'est pas déjà dans la liste alors on peut l'ajouter
												mods.push(newValue);
											}
											
											record.eachChild(function(v) {
												//console.log("each children, children = ",v);
												
												var val = null;
												
												if (v.get('type') == 'modalite') {
													val = v.get("modalite");
												} else if (v.get("type") == 'codageString') {
													val = v.get("newValue");
												}
												
												if (mods.indexOf(val)<0) {
													mods.push(val);
												}
											});
											var me = this.up("treepanel")	;										
											var comp = Ext.create('Ext.grid.CellEditor', {								
												field: {
													xtype:"combo", 
													emptyText:'(vide)',
													value:record.get("newValue"),
													store:mods,
													enableKeyEvents: true,
													listeners: {	
														//todo : à virer je pense : 
														afterrender:function(field) {
															
															field.setRawValue(record.get("newValue"));
															field.setValue(record.get("newValue"));
														},
														blur:function( field, event, eOpts ) {
															field.setValue(field.getRawValue());														
															record.set("newValue",field.getRawValue());
															me.save();
														},
														changeOld:function( field, newValue, oldValue, eOpts) {	
															field.setValue(field.getRawValue());														
															record.set("newValue",field.getRawValue());
															me.save();
														},
														specialkey: function (field, e) {
																if (e.getKey() == e.ENTER) {
																	field.setValue(field.getRawValue());
																	record.set("newValue",field.getRawValue());
																}
															}
															
														} // listeners
													}
											});
											//todo : à virer je pense : 
											comp.setValue(record.get("newValue"));
											//comp.items[0].setValue(record.get("newValue"));
											comp.down("combo").setRawValue(record.get("newValue"));
											comp.field.setRawValue(record.get("newValue"));
											comp.field.setValue(record.get("newValue"));
											
											
											return comp;
										}
										
									}
							},
							{
								xtype: 'actioncolumn',
								width:90,
								  items: [
									{
										  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
											  // hide this action if row data flag indicates it is not deletable											  
												if ($.inArray(r.get("type"), ['range'])) {
													return "hiddenicon";
												} else {
												  return "x-myhidden-display";											  
												}
										  },
										  handler: function(view, rowIndex, colIndex, item, e, record, row) {
											  //do something
											 this.up("treepanel").editCodage(record);
											 
											  
										  },
										  icon: 'resources/images/edit-16.png',
										  tooltip:"edition"
									  },
									{
										  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
											  // hide this action if row data flag indicates it is not deletable											  
												if (r.get("type")=='modalite' || r.get("type")=='range') {
													return "hiddenicon";
												} else {
												  return "x-myhidden-display";											  
												}
										  },
										  handler: function(view, rowIndex, colIndex, item, e, record, row) {
											  //do something
											 var me = this.up("treepanel")	;
											  
											  me.exportCodage(record);
										  },
										  icon: 'resources/images/icon-enter.png',
										  tooltip:"exporter"
									  },
									  {
										  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
											  // hide this action if row data flag indicates it is not deletable											  
												  return "x-myhidden-display";											  
										  },
										  handler: function(view, rowIndex, colIndex, item, e, record, row) {
											  //do something
											 var me = this.up("treepanel")	;
											  var msg="Confirmez vous la suppression du/des codage(s)";
											  Ext.MessageBox.show({
												title: 'Suppression du codage',
												msg:msg,
												buttons: Ext.MessageBox.OKCANCEL,
												icon: Ext.MessageBox.WARNING,
												fn: function(btn){
													if(btn == 'ok'){
														//console.log("remove of record : ",record);
														while(record.firstChild) {
															record.removeChild(record.firstChild);
														}
														record.parentNode.removeChild(record);
														//me.getStore().remove(record); // todo : child too !!
														me.save();
													} else {
														return;
													}
												}
											});
										  },
										  icon: 'resources/images/remove.png',
										  tooltip:"supprimer"
									  }
								  ]                             
								
							}
							] ,// fin columns,
							dockedItems: [
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
										{
											xtype:'combo',
											 displayField:'path',
											 valueField:'fullpath',											 
											 itemId:"combo_path",
											 editable:false,
											
											 value:"",
											 listeners : {
													'change':function(val,newValue, oldValue) {	
														
														var panel_store = this.up("panel").getStore();
														if (newValue=="") {
															panel_store.clearFilter();
														} else {
															panel_store.clearFilter();
															var chemin_node = panel_store.findRecord('fullpath',newValue);	
															if (chemin_node!=null) {
																this.up("panel").expandNode( chemin_node, true );
															}
															panel_store.filterBy(function (record) {
																if (record.get('type') == 'variable') {
																	if (record.get('fullpath') == newValue) {
																		return true;
																	}
																	return false;
																} else {
																	return true;
																}								
															});
														}
													}
											},
											 flex:1,
											 fieldLabel:'filtre Variable',
											 itemId:"chemin"
										},
										{
										text: 'Actions',                      
										menu: {
											xtype: 'menu',                          
											items: [{
													text: 'export',
													iconCls: 'edit',
													listeners: {
														click:function(button) {
															var panel = button.up("window").down("treepanel")
															panel.exportCodage(null);
															
														}
													}
												}, {
													text: 'import',
													scale: 'small',
													listeners: {
														click:function(button) {																														
															var win = Ext.create('Desktop.ImportCodage');		
															win.app = this;
															var parentCaller = button.up("window").down("treepanel");
															//console.log("parentCaller = ",parentCaller);
															win.parentCaller = parentCaller;
															win.show();
														}
													}
												}]                          
										}
										}
									]
								}
								
							],
							save:function() {
								var tree = this;
								//var data = Ext.pluck(tree.getStore().data.items,'data');
								//var data = Ext.pluck(tree.getStore().root,'data');
								var data = [];
								
								var children = tree.getStore().getRootNode().childNodes;
								pmid = 1;
								for (var i=0;i<children.length;i++) {									
									data.push(getCodage(children[i]));
								}
								
								
								//removeUnusedField(data);
								
								var json = Ext.encode(data); 
								
								// var json = Ext.encode(Ext.pluck(store.data.items, 'data'));
								
								
								
								tree.setLoading(true);
								$.post("/service/commands",{cmd:'updateCodages',codages:json},function() {
									tree.setLoading(false);
									tree.getStore().removeAll(); // sinon pas de reinit de modifiedRecords !
									
									tree.load_codage();
									tree.up("window").app.fireEvent("codageUpdated",this);
									//modalites_panel.getStore().load();
								});
							},
							reload_count:function() {
								
								var tree= this;
								var children = tree.getStore().getRootNode().childNodes;
								
								
								tree.getView().refresh();
							},
							load_codage:function() {
								
								var tree = this; //this.up("window").down("treepanel");
								
									var combo = this.up('window').down("combo");
									
									var s_store = new Ext.create('Ext.data.TreeStore',{
											autoLoad:false,
											proxy: {
												type: 'ajax',
												//url: 'check-nodes.json',
												url:'/service/commands',
												extraParams: {
													cmd:'getPrefCodages'													
												} //  reader : idProperty: 'sitecode'
											},
											listeners:{
												'load':function(store) {
													
													var s = combo;
													var dat=[{fullpath:"",path:"All"}];
													
													for (var i=0;i<store.getCount();i++) {
														var d =  store.getAt(i);
														if (d.get("type")=="variable") {
															dat.push({fullpath:d.get("fullpath"),path:d.get("lastnode")});							
														}
													}
													tree.setStore(store);
													tree.getView().refresh();
													
													
													s.clearValue();
													
													s.setStore(new Ext.create('Ext.data.Store',{fields: ['fullpath','path'],data:dat}));
													
													s.setValue(""); // fait : type error : a is null;
													
													
												}
												
											}
										});
										
										s_store.load();
							},
							listeners: {								
								checkchange:function(node, checked, eOpts) {
									
									if ( hasParentInactive(node)) { // un parent est inactif ou bien checked == true (car cela veut dire que checked was false) !!!
										node.data.checked =  !node.data.checked; // on remet l'ancienne valeur										
										
									} else {
										this.save();
									}
									
									
									
								},
								
								viewready: function (tree) {
									
									var view = tree.getView();			
									var dd = view.findPlugin('treeviewdragdrop'); // http://docs.sencha.com/extjs/5.1/5.1.0-apidocs/#!/api/Ext.tree.ViewDropZone-method-onNodeEnter																		
									
									dd.dropZone.notifyEnter = function( source, e, data ) {
										if (source.view == view) { // drag from the codage tree
											return; 
										}
										Ext.WindowManager.bringToFront(tree.up("window"));
																				
										var current_path="";
										try {
											//console.log("notifyEnter, source=",source);
											current_path = source.view.grid.current_path;
											
										}
										catch(err) {
											current_path = "";
										}
										
										
										var combo = tree.up('window').down("combo");
										combo.setValue(current_path);
										
										return this.dropNotAllowed;
										//return true;
									};
									
									dd.dragZone.onBeforeDrag = function (data, e) {										
										var record = view.getRecord(e.getTarget(view.itemSelector));
										
										if (record.get("type")!="variable" ) {
											return true;
										} else  {
											return false;
										}
										
									};
									
									dd.dropZone.notifyOver = function( source, e, data ) {
										
										var rec = null;
										try {
											rec = view.getRecord(e.getTarget(view.itemSelector));
										} catch (err) {
											rec = null;
										}
										//console.log("notifyOver");
										
										if (rec!=null && rec.isLeaf()) // on interdit le drop sur un noeud terminal (une modalite)
											return  this.dropNotAllowed ;
										else {
											if (source.view == view) { // drag from the tree itself
												if (rec == null) { // drop dans la zone blanche
													return  this.dropNotAllowed ;
												}
												if (rec.get("type") == "modalite") { // drop dans la zone blanche
													return  this.dropNotAllowed ;
												}
												
												if (rec === data.records[0]) { // disallow drop into the same record
													return  this.dropNotAllowed ;
												}
												
												// check if the source node is parent of target node if so then forbid drop
												if (isParent(data.records[0],rec)) {
													return  this.dropNotAllowed ;
												}
												
												// disallow a drop on the direct parent
												if (data.records[0].parentNode == rec) {
													return  this.dropNotAllowed ;
												}
												
												if (rec.get('type')=='variable' && data.records[0].get("type") != 'codageNumeric' && data.records[0].get("type") != 'codageString' && data.records[0].get("type") != 'codageNumeric') { // disallow drop into the 'Variable' node unless type is codage
													return  this.dropNotAllowed ;
												}
												
												// drag and drop from the codage tree, so we  must allow drop only inside the same path
												// get the parent path of the dragged record
												// and get also the parent path of the target rec 
												// if they are equals : drop allowed else not allowed
												var rec_source = data.records[0];
												var source_fullpath = getFullpathFromAncestor(rec_source);
												var target_fullpath = getFullpathFromAncestor(rec);
												if (source_fullpath == target_fullpath) {
													return this.dropAllowed;
												} else {
													return  this.dropNotAllowed ;
												}
												
											} else {
												return this.dropAllowed;
											}
										}
										
									};
									
									dd.dropZone.notifyDrop = function( source, e, data ) {
										if (this.notifyOver(source,e,data) == this.dropNotAllowed) {									
											return;
										}
										
										
										
										var rec = null;
										try {
											rec = view.getRecord(e.getTarget(view.itemSelector));	
										} catch(err) {
												rec = null;
											};
										
										if (source.view == view) {
											// notifyDrop from the same view
											
											rec.appendChild(data.records[0]); 
											tree.save();
											return true;
										}
										
										
										
										if (rec == null) { // drop dans la zone blanche
											//console.log("rec is null");
											var current_path=""; // get the current_path from the grid source (if any, otherwise it's another component)
											var current_path_type="";
											try {
												current_path = source.view.grid.current_path;
												current_path_type = source.view.grid.current_path_type;
											}
											catch(err) {
												Ext.Msg.alert("current_path error");
												return false;
												
											};
											
											view.getStore().clearFilter(); // pour savoir si c'est cela qui bloque : oui
											  // suspendLayout : peut etre à faire 
												
												
												
												var chemin_node = view.getStore().findRecord('fullpath',current_path);	
												var new_node = null;
												if (chemin_node == null) {
													var rootNode = view.getStore().getRootNode();
													var n = {
														type:"variable",
														lastnode:getLastNode(current_path),
														fullpath:current_path,														
														expanded:true,
														checked:true
														};
													new_node = rootNode.appendChild(n);
												}	else {
													// current_rec != null -> cela signifie qu'il y a déjà un stockage
													new_node = chemin_node;
												}												
												
												
												
												if (!isNumericSchemaType(current_path_type)) {
													new_node = new_node.appendChild({type:'codageString',count:-1,newValue:'',expanded:false,iconCls:'noicon',checked:true}); //active:true,
													for(var i=0;i<data.records.length;i++) {
														new_node.appendChild({type:"modalite",count:-1,modalite:data.records[i].get('modalite'),leaf:true,iconCls:'noicon',checked:true}); // getAt(i) sinon, ,active:true												
													}
												} else {
													new_node = new_node.appendChild({type:'codageNumeric',count:-1,newValue:'',expanded:false,iconCls:'noicon',checked:true}); //active:true,
													var minv = null;
													var maxv = null;
													for(var i=0;i<data.records.length;i++) {
														var v = parseFloat(data.records[i].get('modalite'))	;
														if (minv==null) {
															minv=v;
														}
														if (maxv==null) {
															maxv=v;
														}
														if (v<minv) {
															minv = v;
														}
														if (v>maxv) {
															maxv = v;
														}
													}
													new_node.appendChild({type:"range",count:-1,minValue:minv,maxValue:maxv,leaf:true,iconCls:'noicon',checked:true}); // getAt(i) sinon, ,active:true												
												}
												new_node.expand();
												
												
												var combo = tree.up('window').down("combo");
												var st = combo.getStore();
												var current_rec = st.findRecord( 'fullpath', current_path);
												if (current_rec ==  null) {
													var m = st.getRange();												
													m.push({fullpath:current_path,path:getLastNode(current_path)}); // ajout définitivement le choix dans le store : oui mais ne fonctionne pas
													combo.setStore(Ext.create('Ext.data.Store',{fields: ['fullpath','path'],data:m}));
													
													
												}
												combo.setValue(""); // todo : faire l'équivalent d'un change !! pour remettre le filtre
												combo.setValue(current_path);
												view.refresh();		
												tree.save();
												return true;
										} else {
											// TODO : si current_path_type is numeric alors ajouter un range et non pas un modalite
											if (rec.get('type') == 'variable') { // nouveau codage mais dans un path existant												
												var new_node = rec.appendChild({type:'codageString',count:-1,newValue:'',expanded:true,iconCls:'noicon',active:true,checked:true}); //
												for(var i=0;i<data.records.length;i++) {
													new_node.appendChild({type:"modalite",count:-1,modalite:data.records[i].get('modalite'),leaf:true,iconCls:'noicon',active:true,checked:true}); // getAt(i) sinon												
												}
												
												
												
											} else {
												for(var i=0;i<data.records.length;i++) {
													rec.appendChild({type:"modalite",count:-1,modalite:data.records[i].get('modalite'),leaf:true,iconCls:'noicon',checked:true}); // getAt(i) sinon	,,active:true											
												}		
											}			
											//console.log("end");
											view.refresh();		
											tree.save();											
											return true;
										}
										return true;
									}
									
									
								},
								afterrender:function() {
									this.load_codage();
									/*
									this.up("window").addTool({
									  type:'refresh',
									  handler: function() {
										//this.up("window").down("grid").getStore().load();
										console.log("refresh count");
										this.up("window").down("treepanel").reload_count();
									  },
									  scope:this
									});
									*/									
								}
							}
					} // fin xtreepanel
					  
                ]
               
            });
        
        return win;
    }
});

function hasParentInactive(record) {
		
		var current_node = record.parentNode;
		while (current_node.parentNode!=null) {			
			
			if (!current_node.get('checked')) {
				return true;
			}
			current_node = current_node.parentNode;
		}
		return false;
	}	

var count_path = "";	


var pmid = 1;
function getCodage(v) {
	var r = {};
	if (v.data.type == 'variable') {
		r.type = v.data.type;
		r.lastnode = v.data.lastnode;
		r.fullpath = v.data.fullpath;
		r.checked = v.data.checked;
		//r.active = v.data.active;
		r.expanded = v.data.expanded;
		r.children=[];
		for(var i=0;i<v.childNodes.length;i++) {
			r.children.push(getCodage(v.childNodes[i]));
		}
	} else if (v.data.type == 'codageString' || v.data.type == 'codageNumeric') {	
		r.pmid = pmid++;
		r.type = v.data.type;	
		r.count = v.data.count;
		r.newValue = v.data.newValue;
		r.iconCls = v.data.iconCls;
		r.checked = v.data.checked;
		//r.active = v.data.active;
		r.expanded = v.data.expanded;
		r.children=[];
		for(var i=0;i<v.childNodes.length;i++) {
			r.children.push(getCodage(v.childNodes[i]));
		}
	} 
	else if (v.data.type == 'modalite') {
		
		r.type = v.data.type;
		r.count = v.data.count;
		r.modalite = v.data.modalite;
		r.iconCls = v.data.iconCls;
		r.checked = v.data.checked;
		//r.active = v.data.active;
		r.leaf = v.data.leaf;
	} else if (v.data.type == 'range') {
		
		r.type = v.data.type;
		r.count = v.data.count;
		r.minValue = v.data.minValue;
		r.maxValue = v.data.maxValue;
		r.iconCls = v.data.iconCls;
		r.checked = v.data.checked;
		//r.active = v.data.active;
		r.leaf = v.data.leaf;
	}
	
	return r;
}

	function recurseRemoveUnusedField(f) {
	}
	function removeUnusedField(d) {
		for (var i=0;i<d.length;d++) {
			delete d[i].id;
			
		}
	}
	
	

function isParent(source_rec,target_rec) {
	if (target_rec == null) {
		return false;
	}
	
	if (source_rec === target_rec) {
		return true;
	} 
	return isParent(source_rec,target_rec.parentNode);
}

function getFullpathFromAncestor(record) {
	if (record.get("fullpath")!=null) {
		return record.get("fullpath");
	}
	if (record == null) {
		return null;
	}
	return getFullpathFromAncestor(record.parentNode);
}
	

/*
function isParent(source_rec,target_rec) {
	if (target_rec == null) {
		return false;
	}
	
	if (source_rec === target_rec) {
		return true;
	} 
	return isParent(source_rec,target_rec.parentNode);
}

function getFullpathFromAncestor(record) {
	if (record.get("fullpath")!=null) {
		return record.get("fullpath");
	}
	if (record == null) {
		return null;
	}
	return getFullpathFromAncestor(record.parentNode);
}
*/