 
var menuQuantifier = new Ext.menu.Menu({
        items: [
                { text: "aucun noeud",
					listeners: {
						click: function(item){											    
							item.parentMenu.record.set("quantifier","none")	;
							item.parentMenu.treepanel.save();	
						}
					}
				},
				{ text: "au moins un noeud",
					listeners: {
						click: function(item){
							item.parentMenu.record.set("quantifier","some")	;
							item.parentMenu.treepanel.save();	
						}
					}
				},
				{ text: "tous les noeuds",
					listeners: {
						click: function(item){
							item.parentMenu.record.set("quantifier","every")	;
							item.parentMenu.treepanel.save();	
						}
					}
				}
        ]
    });

Ext.define('Desktop.FiltreWindow2', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'filtre2-win',

    init : function(){
        this.launcher = {
            text: 'Filtre Avancé',
            iconCls:'icon-grid',
            idmenu:"requete"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var theapp = this.app;
           var win = desktop.createWindow({               
                title:'Filtre Avancé',
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
									var menuAction = this.down("#theMenuAction");
									if (theapp.user.credential.readwrite == false) {
										menuAction.setDisabled(true);
									}
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
							renderConstraint:function(rec) {
								return rec.get("name");
							},
							renderPath:function(rec) {
								
								var quantifierMap = {"none":"aucun noeud","some":"au moins un noeud","every":"tous les noeuds"};
								var q = quantifierMap[rec.get("quantifier")];
								if (q==null) {
									q="??";
								}
								return q+" "+getLastNode(rec.get("path"))+" où";
							},
							renderNode:function(rec) {
								var r = "";//getLastNode(rec.get("node"));
								var cs=[];
								var conditions = rec.get("conditions");
								for( var i=0;i<conditions.length;i++) {	
									var s=getLastNode(rec.get("node"));
									for (var j=0;j<conditions[i].modifiers.length;j++) {
										s = conditions[i].modifiers[j]+"("+s+")";
									}								
									cs.push(s+" "+formatOperateur(conditions[i].operator) + conditions[i].value1); // TODO il faudra changer formatOperateur pour ajouter value1 et value2 comme la partie java car l'operateur between nécessite deux valeurs
								}
								return r + cs.join(" ou ");
							},
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
								//autoSizeColumn : true,
								flex:1,
								renderer:function (value, metadata, record, rowIndex, colIndex, store,view) {									
									var r_type = record.get("type");
									if (r_type=="constraint") {										
										return view.renderConstraint(record);
									} else if (r_type=="path") {
										return view.renderPath(record);										
									} else if (r_type=="node") {
										return view.renderNode(record);										
									} else {
										return "type non reconnu";
									}
									
								}
							},
							
							{
								xtype: 'actioncolumn',
								width:80,
								  items: [
									{
										  getClass: function(v, metadata, r, rowIndex, colIndex, store) {	
											return "x-myhidden-display";										  												
										  },
										  handler: function(view, rowIndex, colIndex, item, e, record, row) {
												var treeWindow = this.up("treepanel");
												if (record.get("type")=="constraint") {													
													var callback = function (btn, text){
														if (btn != 'ok' || text=="") return;
														record.set("name",text);
														treeWindow.save();
													};
													Ext.MessageBox.prompt('Groupe de contraintes', 'Entrez un nouveau nom:', callback,this,false,record.get("name"));
												} else if (record.get("type")=="path") {
													menuQuantifier.record = record;
													menuQuantifier.treepanel = treeWindow;
													menuQuantifier.showAt(e.getXY());
													e.stopEvent();
												} else if (record.get("type")=="node") { 
													treeWindow.updateNode(record);
												}
																							  
										  },
										  icon: 'resources/images/edit-16.png',
										  tooltip:"edition"
									  },
									{
										  getClass: function(v, metadata, r, rowIndex, colIndex, store) {	
												if (r.get("type")=="constraint") {
												 return "x-myhidden-display";
												} else {
													return "hiddenicon";
												}
										  },
										  handler: function(view, rowIndex, colIndex, item, e, record, row) {											  
											  var treepanel = this.up("treepanel");
											  var filtreId = treepanel.getStore().getProxy().extraParams.filtreId;
											  
											  var parent = record.parentNode;
											  var position = parent.childNodes.indexOf(record);
											  treepanel.exportFiltre(filtreId,position);
											  
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
														/*
														while(record.firstChild) {
															record.removeChild(record.firstChild);
														}
														*/
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
							/*
							itemcontextmenu:function(view, record, item, index, event){
								if (record.get("type")!="path") {
									return;
								}
								
								menuQuantifier.record = record;
								menuQuantifier.treepanel = this;
								menuQuantifier.showAt(event.getXY());
								event.stopEvent();
							},
							*/
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
								if (theapp.user.credential.readwrite==false) {
									tree.setDisabled(true);
								} else {
									dd.dropZone.addToGroup( 'modaliteDrop' );
								}						
								dd.dropZone.notifyOver = function( source, e, data ) {
									
									if (tree.getStore().getProxy().extraParams.filtreId == -1) {
										return this.dropNotAllowed; // aucun filtre sélectionné
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
										if (rec.get("type")=="constraint") { // accept only on constraint record
											return this.dropAllowed;
										} else {
											return this.dropNotAllowed;
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
										// drag/drop from the same view (=deplacement)
										/*
										rec.appendChild(data.records[0]); 
										tree.save();	
										*/
										console.log("deplacement");
									} else {										
										tree.addNewCondition(source,data,rec);
										//TODO : ouvrir la fenetre d'edition de filtre pour la création d'une nouvelle condition
									}
									
									return true;	
								}; // notifyDrop
							}	// viewready
						},
						exportFiltre:function(filtreId,position) {
							var win = Ext.create('Desktop.ExportFiltre');		
							win.app = this;
							win.filtreId = filtreId;
							win.positionConstraint= position;
							win.show();
							
						}	,
						importFiltre:function(filtreId) {
							var win = Ext.create('Desktop.ImportFiltre');									
							win.filtreId = filtreId;												
							win.choix_filtre = this.up("window").down("#choix_filtre");
							win.show();
							
						}	,
						save:function() {
							
							var children = this.getStore().getRootNode().childNodes;
							
							var data = [];
							for (var i=0;i<children.length;i++) {
								data.push(getFiltre(children[i]));
							}
							
							
							var json = Ext.encode(data); 
							
							
							this.setLoading(true);
							var tree = this;
							
							var filtreId = this.getStore().getProxy().extraParams.filtreId;
							
								$.post("/service/commands",{cmd:'updateFiltre',filtreId:filtreId,filtre:json},function(result) {
									
									tree.setLoading(false);
									tree.getStore().removeAll(); // sinon pas de reinit de modifiedRecords !
									tree.getStore().load();
									tree.up("window").app.fireEvent("filtreUpdated",this,filtreId); 
									
								});
							
							//this.getView().refresh();
							
						},
						
						updateNode:function(record) {
							console.log("edition du noeud ",record.get("node")); // faire comme updateCondition, mais avec toutes les conditions d'un coup
							
							var tree = this;
							var callback=function(new_conditions) {
								//destinationRec.appendChild(condition);
								//record.data = node;
								record.set("conditions",new_conditions);
								tree.save();
							};
							var node = record.data;
							
							var curwin = Ext.create('Desktop.EditFiltre2',{treeWindow :this,node:node,callback:callback});
							this.mask();															
							curwin.show();	
							
						},
						findFiltrePath:function(record,path) { // record is of type constraint , we should search for a direct children (type=path)
							console.log("findFiltrePath = ",record);
							console.log("path = ",path);
							var res = null;
							record.eachChild(function(v) {
								//console.log("v.get('path')=",v.get("path"));
								if (v.get("path")===path) {									
									res = v;
								}								
							});
							return res; // TODO
						},
						removeLastNode:function(path) {
							var index = path.lastIndexOf("/");
							return path.substr(0,index);
							//return "/Q{}prosopographie/Q{}personne"; // TODO
						},
						addNewCondition:function(source,sourceData,destinationRec) {
							//console.log("add new condition");
							//console.log("source.view = ",source.view); // permet de savoir si c'est une modalites ou un path
							//console.log("sourceData",sourceData);
							//console.log("destinationRec",destinationRec);
							
							if (destinationRec.get("type")!="constraint") {
								return;
							}
							
							var leftpart = getDragLeftPart(source,sourceData); // {"path":getFullPathNS_from_array(els),"path_type":sourceData.records[0].data.type};
							var rightpart = getDragRightPart(source,sourceData); // {"value":val}
							
							// TODO nous devons trouver le noeud "parent" (le path) laurent
							var parentPath = this.removeLastNode(leftpart.path);
							var p = this.findFiltrePath(destinationRec,parentPath);
							
							if (p==null) {
								//console.log("p is null");
								// create a path element								
								p = destinationRec.appendChild({type:"path",quantifier:"some",path:parentPath,children:[],checked:true,expanded:true});								
							} else {
								console.log("p found");
							}
							
							var conditions=[];
							var value1="";
							if (rightpart!=null) {
								value1 = rightpart.value;
							}
							conditions.push({type:"condition",modifiers:[],operator:"eq",value1:value1,value2:""});
							p.appendChild({type:"node",node:getLastNode(leftpart.path),conditions:conditions,leaf:true,checked:true});
							
							/*
							console.log("leftpart = ",leftpart);
							console.log("rightpart = ",rightpart);
							*/
							/*
							
							var condition = {"type":"condition","rightpart":rightpart,operator:"eq","leftpart":leftpart,"checked":true,"leaf":true};
							destinationRec.appendChild(condition);
							this.save();
							*/
							
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
											itemId:"theMenuAction",
											items: [{
													text: 'nouveau filtre',
													iconCls: 'edit',
													listeners: {
														click:function(button) {
															var combo = this.up("window").down("combo");
															var tree = this.up("window").down("treepanel");
															var okCallback = function (btn, text){	
																
																 if (btn != 'ok' || text=="") return;
																$.getJSON('/service/commands',{cmd:'createFiltre',name:text}, function(result) {
																	
																	tree.getStore().getProxy().extraParams.filtreId = result.filtreId;
																	
																	combo.getStore().reload();
																	tree.up("window").app.fireEvent("filtreUpdated",this,-1); 
																});
															};
															Ext.MessageBox.prompt('Nouveau filtre', 'Entrez un nom:', okCallback);
														}
													}
												}, {
													text: 'suppression de ce filtre',													
													listeners: {
														click:function(button) {
															var win = this.up("window");
															var me = this.up("window").down("treepanel");
															var combo = this.up("window").down("combo");
															var filtreId = combo.getValue();
														
															  Ext.MessageBox.show({
																title: 'Suppression',
																msg:"Confirmez vous la suppression",
																buttons: Ext.MessageBox.OKCANCEL,
																icon: Ext.MessageBox.WARNING,
																fn: function(btn){
																	if(btn == 'ok'){																	
																		$.getJSON('/service/commands',{cmd:'deleteFiltre',filtreId:filtreId}, function(result) {
																			me.getStore().getProxy().extraParams.filtreId = -1;
																			combo.getStore().reload();
																			win.app.fireEvent("filtreUpdated",this,-1); 
																		});
																	} else {
																		return;
																	}
																}
															  });
														}
													}
												},
												{
													text: 'export de ce filtre',													
													listeners: {
														click:function(button) {
															var treepanel = this.up("window").down("treepanel");
															var combo = this.up("window").down("combo");
															var filtreId = combo.getValue();
															if (filtreId == -1) {
																return;
															}
															treepanel.exportFiltre(filtreId,-1);
														}
													}
												},
												{
													text: 'import filtre',													
													listeners: {
														click:function(button) {
															var treepanel = this.up("window").down("treepanel");
															var combo = this.up("window").down("combo");
																													
															treepanel.importFiltre(-1); // -1 == new filtre, sinon filtreId quand import groupe
														}
													}
												},
												{
													xtype: 'menuseparator'
												},
												{
													text: 'nouveau groupe',
													listeners: {
														click:function(button) {
															var treeWindow = this.up("window").down("treepanel");
															var callback = function (btn, text){
																if (btn != 'ok' || text=="") return;
																treeWindow.getStore().getRootNode().appendChild({type:"constraint",name:text,checked:true,expanded:false,children:[]});
																treeWindow.save();
															};
															Ext.MessageBox.prompt('Nouveau Groupe de contraintes', 'Entrez un nom:', callback);
															
														}
													}
												},
												{
													text: 'import groupe',
													listeners: {
														click:function(button) {
															var treepanel = this.up("window").down("treepanel");
															var combo = this.up("window").down("combo");
															var filtreId = combo.getValue();														
															treepanel.importFiltre(filtreId); // -1 == new filtre, sinon filtreId quand import groupe
														}
													}
												}
												
												]                          
										}
									}
									//---- fin menu action
						] // fin tbar
            });
        
        return win;
    }
});



function getDragLeftPart(source,sourceData) {
	if (sourceData.records[0]!=null && sourceData.records[0].get("schemaNode")!=null) { // drag from a schema treepanel, 
		var els = schemaNode_to_array(sourceData);										
		
		return {"path":getFullPathNS_from_array(els),"path_type":sourceData.records[0].data.type};
	} else {// drag from modalite ou tableau brut
		var current_path = source.view.grid.current_path;
		var current_path_type = source.view.grid.current_path_type;
		return {"path":current_path,"path_type":current_path_type};
	}
	
}

function getDragRightPart(source,sourceData) {
	if (sourceData.records[0]!=null && sourceData.records[0].get("schemaNode")!=null) { // drag from a schema treepanel, 
		return null;
	} else { // drag from modalite ou tableau brut
		var value = sourceData.records[0].get('modalite');
		return {"value":value};
	}
	
}

function getFiltre(v) {
	var r = {}; //{"type":"operator","value":"AND","checked":true,"expanded":true,"children"
	if (v.data.type == 'constraint') { // "type":"constraint","expanded":true,"checked":true,"name":"c1","children"
		r.type = v.data.type;		
		r.checked = v.data.checked;		
		r.expanded = v.data.expanded;
		r.name = v.data.name;
		r.children=[];
		for(var i=0;i<v.childNodes.length;i++) {
			r.children.push(getFiltre(v.childNodes[i]));
		}
	} else if (v.data.type == 'path' ) {	// 	{"type":"path","expanded":true,"checked":true,"path":"/Q{}prosopographie/Q{}personne/Q{}carriere/Q{}poste","quantifier":"every","children":[
		r.type = v.data.type;	
		r.checked = v.data.checked;		
		r.expanded = v.data.expanded;
		r.path = v.data.path;
		r.quantifier = v.data.quantifier;
		r.children=[];
		for(var i=0;i<v.childNodes.length;i++) {
			r.children.push(getFiltre(v.childNodes[i]));
		}
	} else if (v.data.type == 'node' ) {	// 	{"type":"node","node":"Q{}P_fonction","leaf":true,"checked":true,"conditions":[{"type":"condition","modifiers":[],"operator":"eq","value1":"directeur","value2":""}]},
		r.type = v.data.type;	
		r.checked = v.data.checked;		
		r.leaf = v.data.leaf;	
		r.node = v.data.node;
		r.conditions = v.data.conditions;
	} 
	
	return r;
}
