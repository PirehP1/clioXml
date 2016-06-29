Ext.define('Desktop.BasesStore',{
	extend: 'Ext.data.Store',	
	proxy: {
		url:"/service/commands?cmd=listBases",type:"ajax",
		reader: {
			type: 'json',
			root: 'projects'
		}
	},
	autoLoad:true,
	model:'Project'
										

});
Ext.define('Desktop.ProjectServerModal',{
	extend: 'Ext.window.Window',
	layout:'fit',
        width:800,
        height:550,
        closable: false,
        resizable: true,
		
        //plain: true,
		
        border: true,
		title:'ClioXML',
		removeProject:function(record) {
			Ext.MessageBox.show({
				title: 'Suppression du projet',
				msg: 'Confirmez vous la suppression du projet '+record.get('name'),
				buttons: Ext.MessageBox.OKCANCEL,
				icon: Ext.MessageBox.WARNING,
				fn: function(btn){
					if(btn == 'ok'){
						$.getJSON('/service/commands',{cmd:'removeProject',id:record.data.id}, function(project) {								
									Ext.getCmp("projects_list").getStore().load();
								});
					} else {
						return;
					}
				}
			});
		},
		/*
		editProject:function(record) {
			console.log("todo");
		},
		*/
		editProject:function(record) {
			var w = new Ext.create('Ext.window.Window',{
				title:'Edition projet',
				layout:'fit',
				tbar:[
					{xtype:'textfield',itemId:'project_name',value:record.get('name'),flex:1,required: true,fieldLabel:'Nom du projet ',itemId:"project_name"}					
				],
				items:[										
					{xtype:'htmleditor',fieldLabel:'Description', value:record.get('description'),emptyText:'description',itemId:'description'}
				],
				
				bbar:[
					{
					text:'validez',
					listeners:{										
							'click':function(button) {
								var database = 'local';						
								var nom = w.down("#project_name").getValue();
								var desc = w.down("#description").getValue();
								if (nom==null || nom=='') {
									return;
								}
								$.post('/service/commands', {cmd:'updateProject',id:record.get('id'),name:nom,description:desc}, function(data, textStatus) {
								  
								  if (textStatus == 'success') {
										//id:data.projectID
										w.close();
										Ext.getCmp('projects_list').getStore().load();
								  } else {
									Ext.Msg.Alert("Erreur lors de la modification du projet");
								  }
								}, "json");
							}
						}
					}
				]
			});
			w.modal = true;
			w.show();
		},
		openProject: function(record) {
						var win = this;
						$.getJSON('/service/commands',{cmd:'openProject',id:record.data.id}, function(project) {	
							Ext.getCmp("startWindow").setTitle(record.data.name);
							win.destroy();						
							var app = win.app;						
							var module = app.getModule('schema-win');
							var schemaWindow = module.createWindow();												
							schemaWindow.x = 0;
							schemaWindow.y = 0;						
							schemaWindow.show();						
						});
					},
        items: [{
			xtype:'grid',
			id:"projects_list",
			//store:Ext.create('Desktop.ProjectStore'),
			viewConfig: {
				getRowClass:function() {
					return "project_name";
				}
			}
			,
			columns:[			
				{text: "Projets",flex:1,dataIndex: 'name',cls:'project_name',menuDisabled:true},
				{
				xtype:'actioncolumn',
				menuDisabled:true,
				width:100,
				items: [
					{
						icon: 'resources/images/icon-enter.png',  
						tooltip: 'Ouvrir',
						handler: function(grid, rowIndex, colIndex) {
							var rec = grid.getStore().getAt(rowIndex);
							
							this.up("window").openProject(rec);
						}
					}
					,
					{
						icon: 'resources/images/edit-16.png',  
						tooltip: 'Edition',
						handler: function(grid, rowIndex, colIndex) {
							var rec = grid.getStore().getAt(rowIndex);
							this.up("window").editProject(rec);
							//alert("Edit " + rec.get('name'));
						}
					}
				,{
					icon: 'resources/images/remove.png',
					tooltip: 'Suppression',
					handler: function(grid, rowIndex, colIndex) {
						var rec = grid.getStore().getAt(rowIndex);
						this.up("window").removeProject(rec);
						
					}
				}]
				}
				
				//,{text: "#doc",dataIndex: 'nb_docs'}
			],
			plugins: [{
				ptype: 'rowexpander',
				rowBodyTpl : new Ext.XTemplate(
					'{description}'
				)
			}],
			bbar:[
				
					{
						text:"nouveau projet",
						listeners:{										
							'click':function(button) {
								var w = new Ext.create('Ext.window.Window',{
									title:'Nouveau projet',
									layout:'fit',
									tbar:[
										{xtype:'textfield',itemId:'project_name',flex:1,required: true,fieldLabel:'Nom du projet ',itemId:"project_name"},
										{xtype:'combo',itemId:'base_name',flex:1,required: true,fieldLabel:'Base ',store: Ext.create('Desktop.BasesStore'),displayField: 'name',valueField: 'id'}
									],
									items:[										
										{xtype:'htmleditor',fieldLabel:'Description', emptyText:'description',itemId:'description'}
									],
									bbar:[
										{
										text:'validez',
										listeners:{										
												'click':function(button) {
																	
													var nom = w.down("#project_name").getValue();
													var desc = w.down("#description").getValue();
													var base = w.down("#base_name").getValue();
													
													if (nom==null || nom=='' || base=='') {
														return;
													}
													$.post('/service/commands', {cmd:'newProjectServer',name:nom,description:desc,database:base}, function(data, textStatus) {
													  
													  if (textStatus == 'success') {
															//id:data.projectID
															w.close();
															Ext.getCmp('projects_list').getStore().load();
													  } else {
														Ext.Msg.Alert("Erreur lors de la création du nouveau projet");
													  }
													}, "json");
												}
											}
										}
									]
								});
								w.modal = true;
								w.show();
							}
						}
					}
				
			],
			
			listeners : {
				render:function() {
					
					
					
					var thegrid = this;
					thegrid.setLoading(true);
					
					
					var projects = Ext.create('Desktop.ProjectStore');
					thegrid.setStore(projects);
					thegrid.setLoading(false);
					/*
					$.get("/service/commands?cmd=login&user=&password=",function(response) {
						if ("error" in response && response.error != '') {
							if (response.error=='servermode') {
								alert("should open the login form");
								var login = Ext.create('Desktop.LoginWindow');
								login.show();								
								
							} else {
								alert("xx");
							}
						} else {
							var projects = Ext.create('Desktop.ProjectStore');
							thegrid.setStore(projects);
							thegrid.setLoading(false);
						}
					});	
*/					
				},
				itemdblclickOld: function(dv, record, item, index, e) {
					var win = this.up("window");
					$.getJSON('/service/commands',{cmd:'openProject',id:record.data.id}, function(project) {	
						
						win.destroy();
						console.log("project changed !");
						var app = win.app;
						
						var module = app.getModule('schema-win');
						var schemaWindow = module.createWindow();
						
						//console.log("h=",$(window));
						//schemaWindow.width='25%';
						//schemaWindow.height=$(window).height()-48;
						schemaWindow.x = 0;
						schemaWindow.y = 0;
						
						schemaWindow.show();
						
						// Add the main view to the viewport
						//Ext.widget('MainView');
					});
				}
			}
			
		}]
	});