

Ext.define('Desktop.NewSchema',{
	extend: 'Ext.window.Window',
	layout:'fit',
	modal:true,
	//width:600,
	width:$(window).width()*0.4,
	y:0,
	//height:500,
	height:$(window).height()-48,
	closeAction:'destroy',
	gotoTabConfig:function(schema_id) {
		var me=this;
		$.getJSON('/service/commands',{cmd:'getSchema',schema_id:schema_id}, function(schema) {								
			var doc = $.parseXML(schema.content);											
			
			 var rootElements = getSchemaRootElements(doc);
			 rootElements.sort(function(a, b){return a.getAttribute("name").localeCompare(b.getAttribute("name"))});
			 var data=[];
			 var selectedIndex = 0;
			 $.each(rootElements,function(i,s) {
				var elementName = s.getAttribute("name");
				var ns_name = get_ns_name_from_QName(schema.pref_root);
				var ns_element = get_ns_element_name(s);
				
				if (ns_name.ns == ns_element.ns && ns_name.name == ns_element.name) {
					selectedIndex=data.length;
				}
				var fullElement="Q{"+ns_element.ns+"}"+ns_element.name;
				data.push({path:elementName,fullpath:fullElement,schemaNode:s});
			 });
			
			var newStore = Ext.create('Ext.data.Store',{
				fields: ['fullpath','path']
				,data:data
			});
			
			 var tabpanel = me.down("tabpanel");
			var config = tabpanel.child("#configuration");
			config.schema_id = schema_id;
			var addSchemaTab = tabpanel.child("#addSchema");
			addSchemaTab.setDisabled(true);
			config.setDisabled(false);
			tabpanel.setActiveTab(config);
			config.down("combo").setStore(newStore);
			config.down("combo").setValue(data[selectedIndex].fullpath);
			
		});
	},
	title:'Nouveau schéma',
	items:[
		{ 
			xtype:'tabpanel',
			items:[
				{
					title:"1 - Ajout de Schéma",
					xtype:'container',	
					itemId:'addSchema',
					layout: {
						type: 'vbox',
						align: 'stretch'
					},
					defaults: {
						xtype: 'form',
						layout: 'anchor',

						bodyPadding: 10,
						style: {
							'margin-bottom': '20px'
						},
						
						defaults: {
							anchor: '100%'
						}
					},
					items:[
					{
						frame:false,
						title:'Importation',
						
						
						items: [					
							{
								xtype: 'textfield',
								fieldLabel: 'Nom du schéma',
								name:'schema-name'
								
							}, 
							{
								xtype: 'fileupload'
								,vtype: 'file'
								,multiple: false // multiupload (multiple attr)
								,acceptMimes: [ 'xsd'] // file types
								,acceptSize: 20000
								,fieldLabel: 'Fichier  <span class="gray">(xsd, 20 Mo max)</span>'
								,inputSize: 76 // size attr
								,msgTarget: 'under'
								,name: 'files'
							}
						],
						 buttons: [{
							text: 'Chargez',
							handler: function() {
								var me=this;
								var f = this.up('form').getForm();
								var schemaName = f.findField('schema-name');
								
								//console.log("schemaFile=",schemaFile.getValue());
								if (schemaName.getValue()=='' ) {
									Ext.Msg.alert("erreur","nom du schéma et fichier obligatoire");
								} else {
									var field = this.up('form').down("fileupload");
									AmazonS3.uploadFile(field, {
										signingUrl: '/UploadSchema',
										withNoProcessing:true,
										fileName : schemaName.getValue(),
										failureCallback:function(response) {
											Ext.MessageBox.alert('Erreur', response);
											me.up("window").close();
										},
										successCallback: function(response) {
											
											var d = $.parseJSON(response);
											if (d.schema_id==-1) {
												Ext.MessageBox.alert('Erreur', d.errorMsg);
												me.up("window").close();
											} else {
												me.up("window").gotoTabConfig(d.schema_id);	
											}
										}
									});
								}
							}
						}]
						
					},
					{
						frame:false,
						title:'Génération',
						generationSchemaEnd: function (currentJobId) {
							this.updateGeneration(100,"terminé");
							var me=this;
							var schemaName = this.getForm().findField('schema-name').getValue();		
							$.getJSON('/service/commands',{cmd:'addSchemaFromJob',schema_name:schemaName,jobId:currentJobId}, function(response) {	
								if (response.errorMsg!=null) {
									Ext.Msg.alert("Erreur",response.errorMsg);
									me.up("window").close();
								} else {
									me.up("window").gotoTabConfig(response.schema_id);								
								}
							});
						},
						updateGeneration:function(progress,text) {
							var progressBar = this.down("progressbar");
							progressBar.updateProgress(parseInt(progress)/100, text);
						},
						checkGeneration:function (currentJobId) {
							var me = this;
							$.getJSON('/service/job',{action:'getProgress',jobid:currentJobId}, function(result) {								
								
								// progress: -1, id: 1, state: "STARTED", error: null} 
								if (result.error != null) {
									Ext.Msg.alert("Erreur",result.error);
								} else {
									if (result.progress == 100) {
										me.generationSchemaEnd(currentJobId);										
									} else {
										if (result.progress>0 ) {
											me.updateGeneration(result.progress,"Génération..."+result.progress+"%");
											
										}										
										Ext.Function.defer(me.checkGeneration,1000,me,[currentJobId]);
									}
								}
							});
						},
						items: [					
							{
								xtype: 'textfield',
								fieldLabel: 'Nom du schéma',
								name:'schema-name'
								
							},
							 {xtype: 'progressbar'}
						],
						
						 buttons: [
							
						 {
							text: 'Générer le schéma automatiquement',
							handler: function() {
								var fo = this.up("form");
								var f = fo.getForm();
								var schemaName = f.findField('schema-name');
								
								if (schemaName.getValue()=='') {
									Ext.Msg.alert("erreur","nom du schéma obligatoire");
								} else {
									fo.updateGeneration(0,"Démarrage");								
									$.getJSON('/service/job',{action:'startJob',type:'generateSchema'}, function(result) {										
										fo.checkGeneration(result.id);	
									});
								}
						}
						}]
						
					}
					]
					
				},
				{
					title:"2 - Configuration",
					disabled:true,
					itemId: 'configuration',
					schema_id:null,
					layout:'fit',
					items:[
						{
							xtype: 'form',
							layout: 'anchor',

							bodyPadding: 10,
							style: {
								'margin-bottom': '20px'
							},
							
							defaults: {
								anchor: '100%'
							},
							items:[
								{
									xtype:'combo',
									fieldLabel:'Noeud racine',
									valueField: 'fullpath',
									displayField: 'path',						
									editable: false		,
									name:'choixRoot'
									},
								{
									xtype      : 'fieldcontainer',
									fieldLabel : 'Définir comme schéma par défaut ?',
									defaultType: 'radiofield',
									/*
									defaults: {
										flex: 1
									},
									*/
									layout: 'hbox',
									items: [
										{
											boxLabel  : 'Oui',
											name      : 'defaultSchema',
											inputValue: 'oui',
											checked:true
										}, {
											boxLabel  : 'Non',
											name      : 'defaultSchema',
											inputValue: 'non'									
										}
									]
								}
							],
							bbar:[
								{
									text:'Valider',
									handler:function() {
										
										var tab = this.up("tabpanel");
										var config = tab.child("#configuration");
										var f = this.up("form").getForm();
										
										var def = f.findField('defaultSchema').getRawValue();
										
										var rootElement = f.findField("choixRoot").getValue();
										var isDefaultSchema = def;
										
										
										$.getJSON('/service/commands',{cmd:'configureSchema',schema_id:config.schema_id,root_element:rootElement,is_default_schema:isDefaultSchema}, function(response) {	
											tab.up("window").schema_window.getHeader().down("combo").getStore().load();
											tab.up("window").close();
											
										});
									}
								}
							]
						}
					]
					
				}
			]
			
		}
	]
	
});

Ext.define('Desktop.SchemaWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'schema-win',

    init : function(){
        this.launcher = {
            text: 'Schéma',
            iconCls:'icon-grid',
            idmenu:"schema"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var app=  this.app;
           var win = desktop.createWindow({
                //itemId: 'grid-win',
                title:'Schéma',
                width:$(window).width()*0.3,
				y:0,
				x:0,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
                layout: 'fit',
				listeners: {
					afterrender:function() {
						if (app.user.credential.readwrite == false) {
							
						} else {
							this.addTool({
							  type:'gear',
							  handler: function() {
								
								var w = Ext.create('Desktop.NewSchema',{});
								w.schema_window = this;
								w.show();
							  },
							  scope:this
							});
						}
					}
				},
                items: [
                    {
                        border: false,
                        xtype: 'treepanel',
                        enableDrag:true,
						rootVisible: true,
						region:'center',
						listeners:{		
							
							beforeitemexpand : function ( node,index, item, eOpts) {
									if (currentSchema==null) {
										return false;
									}
									//console.log("beforeitemexpand");
										if (!node.hasChildNodes( ) ) { 
											
											var elementStructure = getSchemaElementStructure(node.data.schemaNode);						
											
											if (elementStructure.localName =='complexType' ) {
												// start with attributes
												$.each(elementStructure.childNodes,function(key,val) {
													if (val.localName == 'attribute') {										
														var n = {
															"iconCls":"task",
															"name":get_ns_element_name(val).name,
															// pas de ns car attribute (pourtant )
															"leaf":true,
															expanded: false,
															"description":"attribute",
															type:val.getAttribute("type") || val.getAttribute("ref"),
															schemaNode:val
														};													
														node.appendChild(n);									
													}
												});
												
												$.each(elementStructure.childNodes,function(key,val) {
													if (val.localName != 'attribute') { // could be sequence,choice,element or simpleContent
														processSequenceOrChoiceOrElement(val,node);									
													}
												});
												
											} else {
												console.log("onclick : elementStructure "+elementStructure.localName+" not implemented TODO");
											}
											
											
											
											
											
										}
									}
							
						},
						
						//enableLocking : true,
						columns : {
							defaults : {
								menuDisabled:false,
								sortable: false,			
								autoSizeColumn : true
								
							},
							items:[{xtype: 'treecolumn',text:'element',dataIndex:'name'},{text:'type',dataIndex:'type'},{text:'documentation',dataIndex:'description'}]
						},
						viewConfig: {
							//enableLocking : true,
							copy:true,
							plugins: {
								ptype: 'treeviewdragdrop',
								dragGroup: 't2div'
							},
							listeners : {
								 refresh : function (dataview) {
									
								  Ext.each(dataview.panel.columns, function (column) {
								   if (column.autoSizeColumn === true) {
										//console.log("autosize");
										column.autoSize();
									}
								  });
								  /*
								  Ext.Array.each(dataview.panel.query('headercontainer>gridcolumn'), function(c) {
										if (column.autoSizeColumn === true) {
											console.log("autosize2");
											c.autoSize();
										}
									});
									*/
								 }
								 
								}
						},
						
						useArrows: true
						
                    }
                ],
                tbar:[
					{
						fieldLabel:"Noeud racine",
						xtype:"combo",
						itemId:"selectRootNode",						
						valueField: 'fullpath',
						displayField: 'path',						
						editable: false,						
						listeners : {
								'change':function(val,newValue,oldValue) {
										console.log("change root element schema : ",val,newValue,oldValue);
										return true;
									}
							}
					},
					{
						text:'export',
						listeners : {
								'click':function(button) {
										
										var combo = button.up("window").down("#selectSchema");
										var schemaId = combo.getValue();
										var f = $("#formDownload");				
										f.empty();
										
										f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("downloadSchema"));			
										f.append($("<input>").attr("type", "hidden").attr("name", "schema_id").val(schemaId));
										f.submit();
										return true;
									}
							}
					}
				],
				header: {
					xtype: 'header',
					titlePosition: 0,
					defaults: {
						padding: '0 0 0 0'
					},
					items: [
						{
							xtype:"combo",
							editable:false,
							itemId:"selectSchema",												
							valueField: 'id',
							displayField: 'name',
							
							listeners : {
								'render':function() {
									var s = this;
									this.store.on('load',function(store, records, successful, eOpts) {
										for (var i=0;i<store.getCount();i++) {
												var d =  store.getAt(i);
												if (d.get("pref")==true) {
													s.setValue(d.get("id"));
												}
											}
									});
									this.store.load();
								},
								'change':function(val,newValue, oldValue) {									
										//var t = Ext.ComponentQuery.query("SchemaTree")[0];
										var thecombo = this;
										var t = thecombo.up("window").down("treepanel");
										
										t.setLoading(true);
										
										$.getJSON('/service/commands',{cmd:'getSchema',schema_id:newValue}, function(schema) {
											
											//var s = Ext.ComponentQuery.query("SchemaTree #selectRootNode")[0];	
											var s = thecombo.up("window").down('#selectRootNode');
											//console.log("should be selectRootNode",s);
											if (schema.error!="") {
												Ext.Msg.alert("Erreur chargement du schéma",schema.error);
												s.reset();
												s.store.removeAll();
												t.setLoading(false);
												return;
											}
											
											var doc = $.parseXML(schema.content);				
											cleanXmlSchema(doc);
											currentSchema=doc;
											s.reset();
											
											
																					
											var values = [];
											var rootElements = getSchemaRootElements(doc);
											var schemaRootNode = {};
											 rootElements.sort(function(a, b){return a.getAttribute("name").localeCompare(b.getAttribute("name"))});
											 $.each(rootElements,function(i,el) {
												var elementName = el.getAttribute("name");
												var ns_name = get_ns_name_from_QName(schema.pref_root);
												var ns_element = get_ns_element_name(el); //{ns:ns,name:name}
												
												
												values.push({fullpath:"Q{"+ns_element.ns+"}"+ns_element.name,path:ns_element.name});
												if (ns_name.ns == ns_element.ns && ns_name.name == ns_element.name) {
													schemaRootNode = {
														"iconCls":"task-folder", // task-folder
														"name":ns_element.name,
														"ns":ns_element.ns,
														"leaf":false,
														expanded: false,
														"description":"",
														schemaNode:el,
														children:[],
														allowDrag:true
													};
												}
											 });
											 
											 var newStore = Ext.create('Ext.data.Store',{
												fields: ['fullpath','path']
												,data:values
											});
											
											
										
											s.setStore(newStore);
											
											s.setValue(schema.pref_root);
											
											var st = new Ext.data.TreeStore({root:schemaRootNode,rootVisible:true});
											t.setStore(st);
											st.getRootNode().expand();
											
											t.setLoading(false);
										});
										
										
									}
							},
							
							store : new Ext.data.Store({
								
								fields: ['id','name'],
								proxy: {
									type: 'ajax',
									url: '/service/commands?cmd=listSchemas',
									reader: {
										type:'json',
										root:"schemas"
									}
								}
							})
							
						}
					]
				} // header
            });
        
        return win;
    }
});

