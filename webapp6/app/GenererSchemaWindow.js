
Ext.define('Desktop.GenererSchemaWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'generer-schema-win',

    init : function(){
        this.launcher = {
            text: 'Générer le schéma',
            iconCls:'icon-grid',
            idmenu:"schema"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var app=  this.app;
           var win = desktop.createWindow({
                //itemId: 'grid-win',
        	    id:'genererschema',
                title:'Générer un Schéma',
                width:$(window).width()*0.3,
				y:0,
				x:0,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
                layout: 'fit',
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
               											//tab.up("window").schema_window.
               											
               											
               											var desktop = app.getDesktop();
               										   var modwin=null;
               										   for (var i=0;i<desktop.windows.getCount();i++) {
               											   var win = desktop.windows.get(i);
               											   if (win.itemId=='theschema') {
               												  modwin = win;	
               												  break;
               											   }
               										   }
               										  
               										   if (modwin==null) {
               											   
               											   var module = new Desktop.SchemaWindow();
               												module.app = app;															
               												modwin = module.createWindow();
               												modwin.show();
               												
               												
               										   } 
               										   
               										   setTimeout(function() {
               												modwin.getHeader().down("combo").getStore().load();
               												tab.up("window").close();
               											},100);
               										});
               									}
               								}
               							]
               						}
               					]
               					
               				}
               			]
               			
               		}
               	] // items
	       });
        
        return win;
    }
});

