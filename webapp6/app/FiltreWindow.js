 
var menuOperator = new Ext.menu.Menu({
        items: [
                { text: "changer d'operateur",
					listeners: {
						click: function(item){					
						    var operator = item.parentMenu.record.get("value");
							if (operator == "and") {
								operator = "or";
							} else {
								operator = "and";
							}
							item.parentMenu.record.set("value",operator)	;
							item.parentMenu.treepanel.save();	
						}
					}
				},
				{ text: "insérer un operateur",
					listeners: {
						click: function(item){
							item.parentMenu.record.appendChild({type:'operator',value:'and',expanded:false,children:[],checked:true}); 
							item.parentMenu.treepanel.save();	
						}
					}
				}
        ]
    });

Ext.define('Desktop.FiltreWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'filtre-win',

    init : function(){
        this.launcher = {
            text: 'Filtre Window',
            iconCls:'icon-grid'
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var theapp = this.app;
           var win = desktop.createWindow({               
                title:'Filtre',
				y:0,				
				x:$(window).width()*0.7,
				width: $(window).width()*0.3,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'fit',
				app:theapp,		
				listeners: {
					afterrender:function() {						
									var combo = this.down("#choix_filtre");
									var tree = this.down("treepanel");
									combo.store.on('load',function(store, records, successful, eOpts) {
										
										if (records.length>0 && tree.getStore().getProxy().extraParams.filtreId == -1) {
											combo.setValue(records[0].get("id"));
										} else if (records.length>0) {
											combo.setValue(tree.getStore().getProxy().extraParams.filtreId);
											
										}										
									});
									
									combo.store.load();
								
					}
				}, // fin listeners
				items: [
					{
						xtype:'treepanel',						
						enableDrag:true,	
						rootVisible:false,	
						useArrows: true,						
						layout:'fit',
						selType: 'rowmodel',
						hideHeaders: true,
						//filtreId:-1,
						store:new Ext.create('Ext.data.TreeStore',{
							autoLoad:false,
							proxy: {
								type: 'ajax',								
								url:'/service/commands',
								extraParams: {
									cmd:'getFiltreById',
									filtreId:-1
								} 
								
							}		
						}),
						viewConfig: {	
							copy:true,
							listeners : {
								 refresh : function (dataview) {									
								  Ext.each(dataview.panel.columns, function (column) {
								   if (column.autoSizeColumn === true) {										
										column.autoSize();
									}
								  });								 
								 }
								 
								},
							getRowClass: function( record, index ) {
								if (record.get('checked') == false || hasParentInactive(record)) {									
									return "x-item-disabled";
								} else {
									return '';
								}
							},		
							plugins: {
											ptype: 'treeviewdragdrop',											
											ddGroup : 't2div',
											//ddGroup: {'t2div':true, 'modaliteDrop':true},
											enableDrag:true,
											nodeHighlightOnDrop: true,											
											dragText: 'Déplacement'
										}	
						},
						columns:[
							{
								xtype: 'treecolumn',
								//dataIndex:'type',
								autoSizeColumn : true,
								renderer:function (value, metadata, record, rowIndex, colIndex, store) {
									if (record.get("type")=='operator') {
										switch (record.get("value")) {
											case 'and': return "ET"; break;
											case 'or': return "OU"; break;
											case 'not': return "NON"; break;
										}
											
									} else {
										return formatCondition(record); 
									}
								}
							},
							
							{
								xtype: 'actioncolumn',
								width:90,
								  items: [
									{
										  getClass: function(v, metadata, r, rowIndex, colIndex, store) {											  						  
												if ($.inArray(r.get("type"), ['condition'])) {
													return "hiddenicon"; // afficher l'icone
												} else {
												  return "x-myhidden-display";		// ne jamais affciher l'icone									  
												}
										  },
										  handler: function(view, rowIndex, colIndex, item, e, record, row) {
											  
												var treeWindow = this.up("treepanel");
												treeWindow.updateCondition(record);
																							  
										  },
										  icon: 'resources/images/edit-16.png',
										  tooltip:"edition"
									  },
									{
										  getClass: function(v, metadata, r, rowIndex, colIndex, store) {												
												 return "x-myhidden-display";
										  },
										  handler: function(view, rowIndex, colIndex, item, e, record, row) {
											  console.log("export");
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
												var me = this.up("treepanel")	;
											  var msg="Confirmez vous la suppression";
											  Ext.MessageBox.show({
												title: 'Suppression',
												msg:msg,
												buttons: Ext.MessageBox.OKCANCEL,
												icon: Ext.MessageBox.WARNING,
												fn: function(btn){
													if(btn == 'ok'){
														
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
								
							} // actioncolumn
						], // column du treepanel
						listeners:{
							itemcontextmenu:function(view, record, item, index, event){
								if (record.get("type")!="operator") {
									return;
								}
								console.log("item",item);
								//treePanelCurrentNode = record;
								menuOperator.record = record;
								menuOperator.treepanel = this;
								menuOperator.showAt(event.getXY());
								event.stopEvent();
							},
			
							afterrender:function() {								
								this.setDisabled(true);
								
								var tree = this;
								this.getStore().on('load',function(store, records, successful, eOpts) {										
										if (records.length > 0) {
											
											tree.setDisabled(false);
										} 
									});
							},
							checkchange:function(node, checked, eOpts) {									
									if ( hasParentInactive(node)) { // un parent est inactif ou bien checked == true (car cela veut dire que checked was false) !!!
										node.data.checked = !node.data.checked; // on remet l'ancienne valeur										
										this.getView().refresh();
									} else {
										this.save();
									}																		
								},
							viewready: function (tree) {								
								var view = tree.getView();			
								var dd = view.findPlugin('treeviewdragdrop'); // http://docs.sencha.com/extjs/5.1/5.1.0-apidocs/#!/api/Ext.tree.ViewDropZone-method-onNodeEnter																		
								
								dd.dropZone.addToGroup( 'modaliteDrop' );
														
								dd.dropZone.notifyOver = function( source, e, data ) {
									
									if (tree.getStore().getProxy().extraParams.filtreId == -1) {
										return this.dropNotAllowed;
									}
									var rec = null;
									try {
										rec = view.getRecord(e.getTarget(view.itemSelector));
									} catch (err) {
										rec = null;
									}
									
									if (rec == null) {
										// over zone blanche ->
										
										return this.dropNotAllowed;
									} else {
										if (rec.isLeaf()) {
											return this.dropNotAllowed;
										} else {
											return this.dropAllowed;
										}
									}
								}; // notifyOver
								
								dd.dropZone.notifyDrop = function( source, e, data ) {
									
									if (tree.getStore().getProxy().extraParams.filtreId == -1) {
										return;
									}
									if (this.notifyOver(source,e,data) == this.dropNotAllowed) {									
											return;
										}
									
									var rec = null;
									try {
										rec = view.getRecord(e.getTarget(view.itemSelector));
									} catch (err) {
										rec = null;
									}	
									
									if (source.view == view) {
										rec.appendChild(data.records[0]); 
										tree.save();	
									} else {										
										tree.addNewCondition(source,data,rec);
										//TODO : ouvrir la fenetre d'edition de filtre pour la création d'une nouvelle condition
									}
									
									return true;	
								}; // notifyDrop
							}	// viewready
						},
						save:function() {
							
							var children = this.getStore().getRootNode().childNodes;
							
							var data = null;
							if (children.length>0) {
								data = getFiltre(children[0]);
							}
							
							var json = Ext.encode(data); 
							
							
							this.setLoading(true);
							var tree = this;
							
							var filtreId = this.getStore().getProxy().extraParams.filtreId;
							
								$.post("/service/commands",{cmd:'updateFiltre',filtreId:filtreId,filtre:json},function(result) {
									
									tree.setLoading(false);
									tree.getStore().removeAll(); // sinon pas de reinit de modifiedRecords !
									
									
									tree.getStore().load();
									tree.up("window").app.fireEvent("filtreUpdated",this); // TODO ajouter le filtreId
									
								});
							
							//this.getView().refresh();
							
						},
						updateCondition:function(record) {							
							var tree = this;
							var callback=function(condition) {
								//destinationRec.appendChild(condition);
								record.data = condition;
								tree.save();
							}
							var condition = record.data;
							
							var curwin = Ext.create('Desktop.EditFiltre',{treeWindow :this,condition:condition,callback:callback});
							this.mask();															
							curwin.show();	
							// TODO : call the editFiltre window with callback(condition)
							// callback = add the new condition to the destinationRec, then save the tree
						},
						addNewCondition:function(source,sourceData,destinationRec) {
							console.log("add new condition");
							console.log("source.view = ",source.view); // permet de savoir si c'est une modalites ou un path
							console.log("sourceData",sourceData);
							console.log("destinationRec",destinationRec);
							var tree = this;
							var callback=function(condition) {
								destinationRec.appendChild(condition);
								tree.save();
							}
							var leftpart = getDragLeftPart(source,sourceData);
							var rightpart = getDragRightPart(source,sourceData);
							var condition = {"type":"condition","rightpart":rightpart,operator:null,"leftpart":leftpart,"checked":true,"leaf":true};
							
							var curwin = Ext.create('Desktop.EditFiltre',{treeWindow :this,condition:condition,callback:callback});
							this.mask();															
							curwin.show();	
						
						}
					} // treepanel
					
                ],
                tbar: [
						{
							xtype:'combo',
							 displayField:'name',
							 valueField:'id',											 
							 itemId:"choix_filtre",
							 editable:false,
							 flex:1,
							 store : new Ext.data.Store({
									autoLoad:false,
								fields: ['id','name'],
								proxy: {
									type: 'ajax',
									url: '/service/commands?cmd=listFiltre',
									reader: {
										type:'json',
										rootProperty:"filtres"
									}
								}
							}),
							listeners:{
								change:function(combo, newValue, oldValue, eOpts) {
									
									var tree = combo.up("window").down("treepanel");
									tree.getStore().getProxy().extraParams.filtreId = newValue;
									tree.getStore().load();
									
								}
							}
						}
						,{ //---- debut menu action
										text: 'Actions',                      
										menu: {
											xtype: 'menu',                          
											items: [{
													text: 'nouveau',
													iconCls: 'edit',
													listeners: {
														click:function(button) {
															var combo = this.up("window").down("combo");
															var tree = this.up("window").down("treepanel");
															var okCallback = function (btn, text){																
																$.getJSON('/service/commands',{cmd:'createFiltre',name:text}, function(result) {
																	
																	tree.getStore().getProxy().extraParams.filtreId = result.filtreId;
																	
																	combo.getStore().reload();
																});
															};
															Ext.MessageBox.prompt('Nouveau filtre', 'Entrez un nom:', okCallback);
														}
													}
												}, {
													text: 'suppression',													
													listeners: {
														click:function(button) {
															console.log("suppression");
														}
													}
												},
												{
													text: 'export',													
													listeners: {
														click:function(button) {
															console.log("export");
														}
													}
												},
												
												]                          
										}
									}
									//---- fin menu action
						] // fin tbar
            });
        
        return win;
    }
});

function formatCondition(record) {
	var leftPart = record.get("leftpart");
	var operator = record.get("operator");
	var rightPart = record.get("rightpart");
	
	return formatPart(leftPart)+formatOperateur(operator)+formatPart(rightPart);
}

function formatPart(p) {	
	if (p==null) {
		return "";
	} else if (p.type == "node") {
		return getLastNode(p.path);
	} else if (p.type=="value") {
		return p.value;
	} else {
		return "??";
	}
}

function getDragLeftPart(source,sourceData) {
	if (sourceData.records[0]!=null && sourceData.records[0].get("schemaNode")!=null) { // drag from a schema treepanel, 
		var els = schemaNode_to_array(sourceData);										
		
		return {"type":"node","modifiers":null,"path":getFullPathNS_from_array(els),"path_type":sourceData.records[0].data.type};
	} else {// drag from modalite ou tableau brut
		var current_path = source.view.grid.current_path;
		var current_path_type = source.view.grid.current_path_type;
		return {"type":"node","modifiers":null,"path":current_path,"path_type":current_path_type};
	}
	
}

function getDragRightPart(source,sourceData) {
	if (sourceData.records[0]!=null && sourceData.records[0].get("schemaNode")!=null) { // drag from a schema treepanel, 
		return null;
	} else { // drag from modalite ou tableau brut
		var value = sourceData.records[0].get('modalite')
		return {"type":"value","value":value,"modifiers":null};
	}
	
}

function getFiltre(v) {
	var r = {}; //{"type":"operator","value":"AND","checked":true,"expanded":true,"children"
	if (v.data.type == 'operator') {
		r.type = v.data.type;
		r.value = v.data.value;		
		r.checked = v.data.checked;		
		r.expanded = v.data.expanded;
		r.children=[];
		for(var i=0;i<v.childNodes.length;i++) {
			r.children.push(getFiltre(v.childNodes[i]));
		}
	} else if (v.data.type == 'condition' ) {	// {"type":"value","value":"18","modifiers":null}		
		r.type = v.data.type;	
		r.checked = v.data.checked;		
		r.leaf = v.data.leaf;	
		r.leftpart = v.data.leftpart;
		r.operator = v.data.operator;
		r.rightpart = v.data.rightpart;
	}
	
	return r;
}
