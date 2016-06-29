Ext.define('Desktop.CodageWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'codage-win',

    init : function(){
        this.launcher = {
            text: 'Codage Window',
            iconCls:'icon-grid'
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
       
           var win = desktop.createWindow({
                //itemId: 'grid-win',
                title:'Codages',
				y:0,
				x:400,
                width:740,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'fit',
						
				
					
					items: [
                    
					{						
						xtype:'treepanel',
						
						enableDrag:true,
						
						
						layout:'fit',
						//store:,
						
						rootVisible: true,
						useArrows: true,
						frame: false,
						//hideHeaders: true,
						//disableSelection: true,
						viewConfig: {	
						copy:true,
							listeners : {
								 refresh : function (dataview) {
									
								  Ext.each(dataview.panel.columns, function (column) {
								   if (column.autoSizeColumn === true)
									column.autoSize();
								  })
								 }
								 
								},
							getRowClass: function( record, index ) {
								if (hasParentInactive(record) || record.get('active') == false) {
									//return 'rowinactive'; 
									return "x-item-disabled";
								} else {
									return '';
								}
							}		,
							plugins: {
											ptype: 'treeviewdragdrop',
											//dropGroup: 'modaliteDrop',
											//dragGroup: 'modaliteDrop',
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
						
							
						
						columns:[
							{
								xtype: 'treecolumn',
								text:'',
								width:100,
								dataIndex:'text',
								autoSizeColumn: true ,
								renderer : function (value, metadata, record, rowIndex, colIndex, store){	
									if (record.get('leaf') == true) {
										metadata.tdAttr  = 'data-qtip="' + value + ' occurences"';						
									} 
									return value;
								}
								
								
							},
							{
								xtype:'checkcolumn', dataIndex:'active', width:30,								
								renderer :  function(value, meta, record, rowIndex, colIndex, store) {								
									if(record.get('text')==="Variable"){
										return '<div style="background-color:red" height="100%" width="100%" > </div>';
									} else {
										if (record.get('active')===true) {
											return '<div style="text-align: center"><img class="x-grid-checkcolumn x-grid-checkcolumn-checked" src="data:image/gif;base64,R0lGODlhAQABAID/AMDAwAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw=="></div>';
										} else {
											return '<div style="text-align: center"><img class="x-grid-checkcolumn" src="data:image/gif;base64,R0lGODlhAQABAID/AMDAwAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw=="></div>';
										}
									}
								},
								listeners: {
									beforecheckchange:function(checkbox, rowIndex, checked, eOpts ) {
										//var rec = checkbox.up('panel').getStore().getAt(rowIndex);
										
										var row = this.getView().getRow(rowIndex),
											rec = this.getView().getRecord(row);
										
										if (rec.get('text') === 'Variable') {
											return false;
										}
										
										if (hasParentInactive(rec)) {
											return false; // false = interdire le changement
										}
										return true; 
									},
									checkchange  : function (checkbox, rowIndex, checked, eOpts) {
										
										checkbox.up('panel').getView().refresh();
									}
								}
							},
							{
								dataIndex:'modalite',flex:1,
								text:'modalite',
								renderer : function (value, metadata, record, rowIndex, colIndex, store){	
									if (record.get('leaf') == true) {
										metadata.tdAttr  = 'data-qtip="ancienne modalité"';	
										metadata.tdCls = 'italic-cell'; 		
									} else if (record.get("text")!="Variable") {
										metadata.tdAttr  = 'data-qtip="nouvelle modalité"';																			
									} else { // record.gext == chemin
										metadata.tdCls = 'bold-cell'; 			
										metadata.tdAttr  = 'data-qtip="'+removeQName(record.get("fullpath"))+'"';											
									}
									
									if (value=='') {
										return '(vide)';
									}
									return value;
								},
								getEditor: function(record){	
										//console.log("getEditor",record);
										if (record.get('leaf') == false && record.get("text")!="Variable") {
											var mods=[record.get('modalite')];
											
											var newValue = record.get('newValue'); // on ajoute la valeur initiale (sans la perdre car dans le champs newValue)
											if (mods.indexOf(newValue)<0) { // si cette valeur n'est pas déjà dans la liste alors on peut l'ajouter
												mods.push(newValue);
											}
											
											record.eachChild(function(v) {
												//console.log("each children, children = ",v);
												if (mods.indexOf(v.get('modalite'))<0) {
													mods.push(v.get('modalite'));
												}
											});
											
											/*
											Ext.each(record.get('children'),function(v) {	
												console.log("each children, children = ",v);
												if (mods.indexOf(v.modalite)<0) {
													mods.push(v.modalite);
												}
											});
											*/
											//console.log("mods = ",mods);
											return Ext.create('Ext.grid.CellEditor', {								
												field: {
													xtype:"combo",
													emptyText:'(vide)',
													value:record.get("modalite"),
													store:mods,
													enableKeyEvents: true,
													listeners: {
														specialkey: function (field, e) {
																if (e.getKey() == e.ENTER) {
																	field.setValue(field.getRawValue());
																}
															}
														} // listeners
													}
											});
										}
										
									}
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
											 /*
											 store:Ext.create('Ext.data.Store',{
													fields: ['fullpath','path']
													//,data:[{fullpath:"",path:"All"},{fullpath:"/Q{}prosopographie/Q{}personne/Q{}Sexe",path:"/prosopographie/personne/Sexe"},{fullpath:"/Q{}prosopographie/Q{}personne/Q{}Age",path:"/prosopographie/personne/Age"}]
												}),
												*/
											 value:"",
											 listeners : {
													'change':function(val,newValue, oldValue) {	
														//console.log("newValue=",newValue);
														var panel_store = this.up("panel").getStore();
														if (newValue=="") {
															panel_store.clearFilter();
														} else {
															panel_store.clearFilter();
															panel_store.filterBy(function (record) {
																if (record.get('text') == 'Variable') {
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
										}
										
									]
								}
								/*,
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
										{xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'nouveau codage',itemId:"newCodage",editable:false,emptyText:'coller une ou plusieurs modalité(s)'}
									]
								}*/
							],
							bbar:[
								{
									text:"Appliquer les modifications",
									listeners:{										
										'click':function(button) {
											var codage_panel = this.up('treepanel');
											//var modalites_panel = this.up('window').down('grid');
											var store = codage_panel.getStore();
											
											if (store.getModifiedRecords().length == 0) {
												return;
											}
											
											
											var modifications=[];
											var records = store.getModifiedRecords();
											for (var i=0;i<records.length;i++) {
												
												
												var r = records[i];
												if (r.get("text") == 'Variable') {
													continue;
												}
												if (r.isLeaf()) {
													// take the parent
													r = r.parentNode;										
												}
												
												if (!valInTab(modifications,'extjs_id',r.get('id'))) {
													var old_values=[];
													
													for( var j=0;j<r.childNodes.length;j++) {
														var c = r.childNodes[j];
														old_values.push({old_value:c.get('modalite'),active:c.get('active')});
													}												
													var pmid = r.get('pmid');
													if (r.get("text") == 'nouveau' || pmid==null) {
														pmid=0;													
													}
													
													var chemin_node = r.parentNode;
													
													// modifications.push({extjs_id:r.get('id'),order:r.get('order_modify'),type:r.get('type'),path:chemin_node.get('modalite'),id:pmid,active:r.get('active'),new_value:r.get('modalite'),old_values:old_values});
													modifications.push({extjs_id:r.get('id'),order:r.get('order_modify'),type:r.get('type'),path:chemin_node.get('fullpath'),id:pmid,active:r.get('active'),new_value:r.get('modalite'),old_values:old_values});
												}
											}
											
											//remove of extjs_id
											for (var i=0;i<modifications.length;i++) {
												delete modifications[i].extjs_id;
											}
											
											codage_panel.setLoading(true);
											
											$.post("/service/commands",{cmd:'updateMods',codages:Ext.encode(modifications)},function() {
												codage_panel.setLoading(false);
												store.removeAll(); // sinon pas de reinit de modifiedRecords !
												
												codage_panel.load_codage();
												//modalites_panel.getStore().load();
											});
											
											
											
										}
									}
								}							
							],
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
													cmd:'getAllListModifyModalite'													
												} //  reader : idProperty: 'sitecode'
											},
											listeners:{
												'load':function(store) {
													
													var s = combo;
													var dat=[{fullpath:"",path:"All"}];
													
													for (var i=0;i<store.getCount();i++) {
														var d =  store.getAt(i);
														if (d.get("text")=="Variable") {
															dat.push({fullpath:d.get("fullpath"),path:d.get("modalite")});							
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
								/*
									beforeedit: function(e, editor){
										console.log("beforeedit");
										if (e.record.get('text')==='chemin' )
											return false;
									},
								*/
								viewready: function (tree) {
									
									var view = tree.getView();
									
									/* gestion dd pour modif de modalite */
									var dd = view.findPlugin('treeviewdragdrop'); // http://docs.sencha.com/extjs/5.1/5.1.0-apidocs/#!/api/Ext.tree.ViewDropZone-method-onNodeEnter																		
									
									dd.dragZone.onBeforeDrag = function (data, e) {
										
										var record = view.getRecord(e.getTarget(view.itemSelector));
										
										if (record.get('leaf') == false && record.get("text")!="Variable" ) {
											return true;
										} else  {
											return false;
										}
										
									};
									/*
									dd.dragZone.getDragText = function() {
										return "xx";
									};
									*/
									/*
									dd.dragZone.getDragData = function(e) {
									
										var sourceEl = e.getTarget(view.itemSelector,10); //,10
										if (sourceEl) {
											console.log("sourceEl=",sourceEl);
											var d = sourceEl.cloneNode(true);
											console.log("d=",d);
											d.id = Ext.id();
											console.log("view.getRecord(sourceEl)=",view.getRecord(sourceEl));
											 var x = {
												sourceEl: sourceEl,
												componentClone:d,
												repairXY: Ext.fly(sourceEl).getXY(),
												ddel: d,
												 
												//patientData: dd.getRecord(sourceEl).data
												//patientData:view.getRecord(sourceEl).data
												sourceStore: view.store,
												draggedRecord: view.getRecord(sourceEl)
											}
											//dd.dragZone.dragData = x;
											console.log("return");
											return x;
										}  
									};
									
									dd.dragZone.getRepairXY= function(){
										console.log("getRepairXY");
										console.log("this.dragData=",this.dragData);
										return this.dragData.repairXY;
									};
									//dd.dragZone.ddGroup= 'modaliteDrop';
									*/
									/*
									dd.dragZone.notifyDrag = function(source, e, data ) {
										console.log("notifyDrag");
										
									};
									*/
									dd.dropZone.notifyOut = function(source, e, data ) {
										//console.log("notifyOut");
										
									};
									
									dd.dropZone.beforeDrop = function() {
										console.log("before drop");
										
									};
									
									dd.dropZone.notifyDrop = function( source, e, data ) {
										
										
										var rec = null;
										try {
											rec = view.getRecord(e.getTarget(view.itemSelector));	
										} catch(err) {
												rec = null;
											};
										
										if (source.view == view) {
											// notifyDrop from the same view
											
											rec.appendChild(data.records[0]);
											return true;
										}
										
										if (rec == null) { // drop dans la zone blanche
											//console.log("rec is null");
											var current_path=""; // get the current_path from the grid source (if any, otherwise it's another component)
											try {
												current_path = source.view.grid.current_path;
											}
											catch(err) {
												Ext.Msg.alert("current_path error");
												return false;
												current_path = "";
											};
											
											view.getStore().clearFilter(); // pour savoir si c'est cela qui bloque : oui
											  // suspendLayout : peut etre à faire 
												
												
												
												var chemin_node = view.getStore().findRecord('fullpath',current_path);	
												var new_node = null;
												if (chemin_node == null) {
													var rootNode = view.getStore().getRootNode();
													var n = {
														text:"Variable",
														modalite:getLastNode(current_path),
														fullpath:current_path,
														active:true,		
														expanded:true,
														//conCls:'noicon',
														leaf:false
														};
													new_node = rootNode.appendChild(n);
												}	else {
													// current_rec != null -> cela signifie qu'il y a déjà un stockage
													new_node = chemin_node;
												}												
												
												new_node = new_node.appendChild({text:'nouveau',modalite:'',newValue:'',expanded:false,iconCls:'noicon',active:true}); //
												//console.log(source);
												
												for(var i=0;i<data.records.length;i++) {
													new_node.appendChild({modalite:data.records[i].get('modalite'),leaf:true,iconCls:'noicon',text:'--',active:true}); // getAt(i) sinon												
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
												
												return true;
										} else {
											console.log("rec is not null");
											if (rec.get('text') == 'Variable') { // nouveau codage mais dans un path existant												
												var new_node = rec.appendChild({text:'nouveau',modalite:'',newValue:'',expanded:false,iconCls:'noicon',active:true}); //
												for(var i=0;i<data.records.length;i++) {
													new_node.appendChild({modalite:data.records[i].get('modalite'),leaf:true,iconCls:'noicon',text:'--',active:true}); // getAt(i) sinon												
												}
												
												
												
											} else {
												for(var i=0;i<data.records.length;i++) {
													rec.appendChild({modalite:data.records[i].get('modalite'),leaf:true,iconCls:'noicon',text:'--',active:true}); // getAt(i) sinon												
												}		
											}			
											//console.log("end");
											view.refresh();											
											return true;
										}
										return true;
									}
									
									dd.dropZone.notifyEnter = function( source, e, data ) {
										if (source.view == view) { // drag from the codage tree
											return; 
										}
										Ext.WindowManager.bringToFront(tree.up("window"));
										
										//console.log("notifyEnter",source);
										//console.log("data=",data); // il faut trouver la colonne pour savoir quel path (currentpath)
										var current_path="";
										try {
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
									
									dd.dropZone.notifyOver = function( source, e, data ) {
										
										var rec = null;
										try {
											rec = view.getRecord(e.getTarget(view.itemSelector));
										} catch (err) {
											rec = null;
										}
										//console.log("notifyOver");
										
										
										if (rec!=null && rec.isLeaf()) 
											return  this.dropNotAllowed ;
										else {
											if (source.view == view) {
												if (rec.get('fullpath')!=null) { // disallow drop into the 'Variable' node
													return  this.dropNotAllowed ;
												}
												
												if (rec === data.records[0]) { // disallow drop into the same record
													return  this.dropNotAllowed ;
												}
												
												// check if the source node is parent of target node if so then forbid drop
												if (isParent(data.records[0],rec)) {
													return  this.dropNotAllowed ;
												}
												// drag and drop from the codage tree, so we  must allow drop only inside the same path
												// todo : get the parent path of the dragged record
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
									
									
									/*							
									var tf = this.up("window").down('#newCodage');
									
									var dd = new Ext.dd.DropTarget(tf.getEl(), {
										// must be same as for tree
										 ddGroup:'modaliteDrop'
										,notifyDrop:function(dd, e, node) {
										
											console.log("DROP");
											return true;
										} // eo function notifyDrop
									});
									*/
									
								},
								afterrender:function() {
									this.load_codage();
										//tree.store.load();
								}
							}
					} // fin xtreepanel
					  
                ]
               
            });
        
        return win;
    }
});

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
