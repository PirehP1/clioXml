Ext.define('SimpleModaliteModel',{ // was book
        extend: 'Ext.data.Model',
		
        fields: [
                   
            'modalite']
    });
	
Ext.define('Desktop.ContingenceWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'contingence-win',

    init : function(){
        this.launcher = {
            text: 'Contingence',
            iconCls:'icon-grid',
            idmenu:"tableau"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
		var app = this.app;
		var theapp = app;
           var win = desktop.createWindow({
                itemId:'contingence-win',
                title:'Tableau de contingence',
				y:0,
				x:$(window).width()*0.3,
                width:$(window).width()*0.4,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'fit',
				app:app,		
				execFromHisto(data) {
					
					this.current_row_path = data.params.current_row_path;
					this.current_filtreId = data.params.filtreId;
					this.current_row_path_type = data.params.current_row_path_type;
					this.current_col_path = data.params.current_col_path;
					this.current_col_path_type = data.params.current_col_path_type;
					this.current_count_path = data.params.current_count_path;	
					this.order_by = data.params.order_by;
					this.count_in = data.params.count_in;
					
					this.down('#row_path').setValue(removeQName(this.current_row_path));
					this.down('#col_path').setValue(removeQName(this.current_col_path));
					this.down('#count_path').setValue(removeQName(this.current_count_path));
					// setActiveItem(xx,false)
					
					var c = this.down("#count_in_cycle");
					var item = c.down("#item_"+this.count_in);
					c.setActiveItem(item,true);
					var o = this.down("#order_by_cycle");
					item = o.down("#item_"+this.order_by);
					o.setActiveItem(item,true);
					
					var combo = this.down("#choix_filtre");
					combo.suspendEvents();
					
					combo.setValue(this.current_filtreId);
					combo.resumeEvents(true); // true = discardQueuedEvents
					
					this.reload_contingence(false);
					
				},
				header:{
					xtype: 'header',
					titlePosition: 0,
					defaults: {
						padding: '0 0 0 0'
					},
					items: [
					{
						xtype:'combo',
						 displayField:'name',
						 valueField:'id',											 
						 itemId:"choix_filtre",
						 editable:false,
						 
						 flex:1,
						 store : new Ext.data.Store({
								autoLoad:true,
							fields: ['id','name'],
							proxy: {
								type: 'ajax',
								url: '/service/commands?cmd=listFiltre',
								reader: {
									type:'json',
									rootProperty:"filtres"
								}
							},
							 
								 
							  
						})
					}
					] // fin items
				}	, // fin header
				items:[
					{
						xtype:'panel',
						itemId:'contingence_panel',
						//html:'<div name="contingence" style="width:100%;height:100%;background-color:blue" ></div>',
						html:'<svg width="100%" height="100%" ></svg>',
						listeners:{
							beforerender:function() {
									
									var me=this.up("window");
									var combo = me.down("#choix_filtre");
									combo.store.on('load',function(store, records, successful, eOpts) {									
										  store.insert(0, [{
											  name: 'Aucun filtrage',
											  id: -1
										  }]);					
										
										if (store.indexOfId( me.current_filtreId ) == -1) { // the current_filterId is not in the list (has been deleted)
											me.current_filtreId = -1; // reinit it
											me.tools["refresh"].show();
											combo.suspendEvents();
											combo.setValue(me.current_filtreId);
											combo.resumeEvents(false);
										} else {										
										  combo.setValue(me.current_filtreId);
										}
									 });
									 
									combo.on("change",function(combo, newValue, oldValue, eOpts) {									
										if (newValue!=me.current_filtreId) {
											me.current_filtreId = newValue;	
											me.reload_contingence(true);
										}
									});
								},
							afterrender:function() {
								if (theapp.user.credential.readwrite==false) {
									var saveButton = this.up("window").down("#saveButton");
									saveButton.setDisabled(true);
								} 
								//innerCt																
								//console.log("le svg  :",$("#"+this.id).find("svg"));
								$("#"+this.id).find("svg").attr("id",this.id+"-svg");
								//$("#"+this.id).find("div[name='contingence']").attr("id",this.id+"-svg");
								
								
					  
							},
							resize: function(){
								var graph_id = this.id+"-svg";
								resizeSVG(graph_id);
								//resizeContingence(graph_id);
								/*
								setTimeout(function(){ 										
									resizeContingence(graph_id);
								}, 0.1);
								*/
							}
						}
					}
				],
				current_row_path:null,
				current_filtreId:-1,
				current_row_path_type:null,
				current_col_path:null,
				current_col_path_type:null,
				current_count_path:null,	
				order_by : "modalite",
				count_in : "absolute",
				confirmed : [],
				openEditor : function() {
					var me = this;
					if (me.confirmed.length == 0) {
						return;
					}

					var module = new Desktop.EditorWindow();
					module.app = app;
					var editorWindow = module.createWindow(me.current_row_path,me.current_col_path,me.current_count_path,me.confirmed,me.current_filtreId);
					editorWindow.show();
					
				},
				reload_contingence:function(addToHisto) {
					var cols = $("text.colLabel.selected");
					$.each(cols,function(i,v) {
						$(v).data("dd").destroy();
						$(v).removeData("dd");
					});
					
					var ligs = $("text.rowLabel.selected");
					$.each(ligs,function(i,v) {
						$(v).data("dd").destroy();
						$(v).removeData("dd");
					});
					
					// remove the dd
					//console.log("dd=",this.data("dd"));
					
					
					this.confirmed = [];
					var me=this;
					var panel_id = this.down("#contingence_panel").id;
					
					//console.log("should be the div : ",$("#"+panel_id+"-svg"));
					if (this.current_row_path!=null && this.current_col_path!=null && this.current_count_path!=null) {
						
						if (addToHisto) {
							
							var combo = this.down("#choix_filtre");
							var current_filtreId = combo.getValue();
							//console.log("current_filtreId=",current_filtreId);
							var filtreName = "";
							if (current_filtreId!=-1) {
								filtreName = combo.getStore().findRecord('id',current_filtreId).data.name;
							}
							
							app.addToHisto({from:"contingence",
												type:"query",
												params:{
													current_row_path:this.current_row_path
													,current_row_path_type:this.current_row_path_type
													,current_col_path:this.current_col_path
													,current_col_path_type:this.current_col_path_type
													,current_count_path:this.current_count_path
													,order_by:this.order_by
													,count_in:this.count_in
													,filtreId:this.current_filtreId
													,filtreName:filtreName
													
												}
												,timestamp:null}); //{from:"modalite",type:"query",params:{},timestamp:null}
						}
						
						//count : 'absolute', 'percent', order_by : 'modalite' or 'marge'
						this.setLoading(true);
						$.getJSON('/service/commands',{cmd:'distinctValues',path:[this.current_row_path,this.current_col_path,this.current_count_path],path_type:[this.current_row_path_type,this.current_col_path_type],order_by:this.order_by,count_in:this.count_in,filtreId:this.current_filtreId}, function(result) { //count : 'absolute', 'percent', order_by : 'modalite' or 'marge'
							me.setLoading(false);
							me.tools["refresh"].hide();
							var updateConfirmed = function (confirmed) {
								me.confirmed = confirmed;
							}
							
							contingence(panel_id+"-svg",result,updateConfirmed);
							//contingenceTile(panel_id+"-svg",result,updateConfirmed,1,[]) ;
						});
					}
				},
				
				
				
				listeners: {
					
					afterrender:function() {
						
						var me = this;
						this.addTool({
							  type:'refresh',
							  hidden:true,
							  handler: function() {
								this.reload_contingence(false);
							  },
							  scope:me
							});
							
						
						var showRefresh = function() {me.tools["refresh"].show();};	
						var showRefreshFiltre = function(source,modifiedFiltreId) {
									var combo = me.down("#choix_filtre");
									combo.getStore().reload();
									var current_filtreId = combo.getValue();
									if (current_filtreId!=-1 && current_filtreId == modifiedFiltreId) {
										me.tools["refresh"].show();
									}									
								};						
								
						this.addListener({'beforeclose':function() {
							me.app.removeListener("codageUpdated",showRefresh); 
							me.app.removeListener("filtreUpdated",showRefreshFiltre); 
						}});
						this.app.addListener({'codageUpdated':showRefresh});
						this.app.addListener({'filtreUpdated':showRefreshFiltre});
						//this.app.addListener({'codageUpdated':function() {me.tools["refresh"].show();}});
						
						
						
						
						
						

						
						var row_input = this.down('#row_path');								
						new Ext.dd.DropTarget(row_input.getEl(), {								
							 ddGroup:'t2div'
							,notifyDrop:function(dd, e, node) {
								var els = schemaNode_to_array(node);										
								row_input.setValue(getFullPath_from_array(els));
								
								me.current_row_path = getFullPathNS_from_array(els) ; //getFullPath_from_array(els);
								me.current_row_path_type = node.records[0].data.type;
								me.reload_contingence(true);
								
								return true;
							} // eo function notifyDrop
						});
						
						var col_input = this.down('#col_path');								
						new Ext.dd.DropTarget(col_input.getEl(), {								
							 ddGroup:'t2div'
							,notifyDrop:function(dd, e, node) {
								var els = schemaNode_to_array(node);										
								col_input.setValue(getFullPath_from_array(els));
								
								me.current_col_path = getFullPathNS_from_array(els) ; //getFullPath_from_array(els);
								me.current_col_path_type = node.records[0].data.type;
								me.reload_contingence(true);
								
								return true;
							} // eo function notifyDrop
						});
						
						var count_input = this.down('#count_path');								
						new Ext.dd.DropTarget(count_input.getEl(), {								
							 ddGroup:'t2div'
							,notifyDrop:function(dd, e, node) {
								var els = schemaNode_to_array(node);										
								count_input.setValue(getFullPath_from_array(els));
								
								me.current_count_path = getFullPathNS_from_array(els) ; //getFullPath_from_array(els);
								
								me.reload_contingence(true);
								
								return true;
							} // eo function notifyDrop
						});
					}
				},
				// count : 'absolute', 'percent', order_by : 'modalite' or 'marge'
				
				
				dockedItems: [
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [										
										{
											xtype: 'cycle',
											itemId:'count_in_cycle',
											text: '',
											prependText: 'Comptage: ',
											showText: true,
											scope: this,
											changeHandler: function(cycle, activeItem){
												console.log("change : ",activeItem.text);	
														
												if (activeItem.text == 'absolue') {
													cycle.up("window").count_in = 'absolute';
												} else {
													cycle.up("window").count_in = 'percent';
												}
												cycle.up("window").reload_contingence(true);
											} ,
											menu: {												
												items: [{
													text: 'absolue',
													checked: true,
													itemId:"item_absolute"
												}, {
													text: 'pourcentage',
													itemId:"percent"
												}]
											}
										},
										{
											xtype: 'cycle',
											text: '',
											itemId:'order_by_cycle',
											prependText: 'Tri: ',
											showText: true,
											scope: this,
											changeHandler: function(cycle, activeItem){
												if (activeItem.text == 'par modalité') {
													cycle.up("window").order_by = 'modalite';
												} else {
													cycle.up("window").order_by = 'marge';
												}				
												cycle.up("window").reload_contingence(true);
											} ,
											menu: {												
												items: [{
													text: 'par modalité',
													checked: true,
													itemId:"item_modalite"
												}, {
													text: 'par valeur',
													itemId:"item_marge"
												}]
											}
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
															var win = button.up("window");
															location.href="/service/commands?"+$.param( {cmd:'exportDistinctValues',path:[win.current_row_path,win.current_col_path,win.current_count_path],path_type:[win.current_row_path_type,win.current_col_path_type],order_by:win.order_by,count_in:win.count_in,filtreId:win.current_filtreId} );
														}
													}
												},
												{
													text: 'enregistrer',	
													itemId:'saveButton',
													listeners: {
														click:function(button) {
															Ext.Msg.prompt("Enregistrement", "Nom pour cette requête :", function(btnText, sInput){
												                if(btnText === 'ok'){
												                    //console.log("name of query : ",sInput);
												                   
												                    
																		
																	
																	var w = button.up("window");
																	if (w.current_row_path!=null && w.current_col_path!=null && w.current_count_path!=null) {
																		var combo = w.down("#choix_filtre");
																		var current_filtreId = combo.getValue();
																		//console.log("current_filtreId=",current_filtreId);
																		var filtreName = "";
																		if (current_filtreId!=-1) {
																			filtreName = combo.getStore().findRecord('id',current_filtreId).data.name;
																		}
																		
																		var params = {
																								current_row_path:w.current_row_path
																								,current_row_path_type:w.current_row_path_type
																								,current_col_path:w.current_col_path
																								,current_col_path_type:w.current_col_path_type
																								,current_count_path:w.current_count_path
																								,order_by:w.order_by
																								,count_in:w.count_in
																								,filtreId:w.current_filtreId
																								,filtreName:filtreName
																								
																							};
																							
																		
													                    
													                    var url="/service/commands?cmd=saveQuery";
																		$.post(url,{from:"contingence",type:"query",name:sInput,params:JSON.stringify(params)},function(response) {
																			console.log(response);
																		});
																	}
																	
												                }
												            }, this);
															
														}
													}
												},
												{
													text: 'effacer la sélection',
													scale: 'small',
													listeners: {
														click:function(button) {
															
															var win = button.up("window");
															win.confirmed=[];
															var panel_id = win.down("#contingence_panel").id;
															var graph_id = panel_id+"-svg";
															
															$('#'+graph_id).data("resetConfirmed")();
														}
													}
												},
												{
													text: 'voir les fiches sélectionnées',
													scale: 'small',
													listeners: {
														click:function(button) {
															
															var win = button.up("window");
															win.openEditor();
														}
													}
												},
												
												]                          
										}
									} // menu action
										,{
											text:"xquery",
											listeners:{								
												'click':function(button) {
													// ouverture de l'éditeur xquery 
													
													var win = button.up("window");
													var url="/service/commands?"+$.param( {cmd:'getXQueryContingence',path:[win.current_row_path,win.current_col_path,win.current_count_path],path_type:[win.current_row_path_type,win.current_col_path_type],order_by:win.order_by,count_in:win.count_in,filtreId:win.current_filtreId} );
													$.post(url,{},function(response) {
														var module = new Desktop.XQueryEditorWindow();
														module.app = app;
														var xquery=response;		
														var editorWindow = module.createWindow(xquery);
														editorWindow.show();
													});
													
													
												}
											}
										}	
									/*,
										{
											text:'export',
											listeners: {
												click:function(button) {
													var win = button.up("window");
													location.href="/service/commands?"+$.param( {cmd:'exportDistinctValues',path:[win.current_row_path,win.current_col_path,win.current_count_path],order_by:win.order_by,count_in:win.count_in} );
												}
											}
										}
										*/
									]
								},
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
										{ xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'Chemin en ligne',itemId:"row_path",editable:false,emptyText:'coller un noeud xml'}
									]
								},
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
										
										{ xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'Chemin en colonne',itemId:"col_path",editable:false,emptyText:'coller un noeud xml'}
										
									]
								},
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [										
										{ xtype:'textfield',flex:1,labelStyle: 'width:220px;white-space: nowrap;',fieldLabel:'Chemin à compter',itemId:"count_path",editable:false,emptyText:'coller un noeud xml'}
									]
								}
								]
								
               
            });
        
        return win;
    }
});
