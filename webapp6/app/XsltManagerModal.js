Ext.define('XsltModel',{ // was book
        extend: 'Ext.data.Model',
		
        fields: [                 
            'id', 'name','type'  
			]

    });

Ext.define('Desktop.XsltStore',{
	extend: 'Ext.data.Store',	
	proxy: {
		url:"/service/commands?cmd=getXsltList",type:"ajax",
		reader: {
			type: 'json'
			//root: 'corrections'
		}
	},
	//autoLoad:true,
	model:'XsltModel'
});

Ext.define('Desktop.XsltManagerModal',{
	extend: 'Ext.window.Window',
	
        //width:500,
        //height:300,
	height:$(window).height()-48,
	width:$(window).width()*0.4,
	y:0,
        closable: true,
        resizable: true,
		modal:true,
		
        //plain: true,
		
        border: true,
		title:'Gestion des Feuilles de Style',
		exportXslt: function() {
			var w = new Ext.create('Ext.window.Window',{
				title:'Export des feuilles',
				width:200,
				
				layout:'fit',
				tbar:[
					{xtype:'textfield',itemId:'export_name',value:"",flex:1,required: true,fieldLabel:"Nom de l'export",itemId:"export_name"}
				],
				bbar:[
					{
					text:'Exporter',
					listeners:{										
							'click':function(button) {
													
								var nom_export = w.down("#export_name").getValue();
								
								if (nom_export==null || nom_export=='') {
									return;
								}
								var f = $("#formDownload");				
								f.empty();
								
								f.append($("<input>").attr("type", "hidden").attr("name", "cmd").val("exportXslt"));			
								
								f.append($("<input>").attr("type", "hidden").attr("name", "name").val(nom_export));
								f.submit();
								w.close();	
								
							}
						}
					}
				]
			});
			w.modal = true;
			w.show();
			
		},
		/*
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
		*/
		items:[
		{
			frame:false,
			title:'Ajout nouvelle feuille XSL/CSS',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			
			items: [					
				{
					xtype: 'textfield',
					fieldLabel: 'Nom de la feuille',
					name:'xslt-name',
					itemId:'xslt-name'
					
				}, 
				{
					xtype: 'fileupload'
					,vtype: 'file'
					,multiple: false // multiupload (multiple attr)
					,acceptMimes: [ 'xsl','xslt','css'] // file types
					,acceptSize: 20000
					,fieldLabel: 'Fichier  <span class="gray">(xst,xslt,css, 20 Mo max)</span>'
					,inputSize: 76 // size attr
					,msgTarget: 'under'
					,name: 'files'
				}
			],
			 buttons: [{
				text: 'Chargez',
				handler: function() {
					var me=this;
					/*
					var form = this.up('form');
					console.log("form1 is=",form);
					var f = form.getForm();
					
					var xsltName = f.findField('xslt-name').getValue();
					*/
					var field = this.up("window").down("#xslt-name");
					var xsltName = field.getValue();
					
					
					//console.log("schemaFile=",schemaFile.getValue());
					if (xsltName=='' ) {
						Ext.Msg.alert("erreur","nom de la feuille et fichier obligatoire");
					} else {
						var field = this.up('window').down("fileupload");
						AmazonS3.uploadFile(field, {
							signingUrl: '/UploadXslt?name='+xsltName,
							successCallback: function(response) {
								me.up("window").down("#listXslt").getStore().reload();
								//form.up("window").close();
								
							}
						});
					}
				}
			}]
			
		},
		{
			frame:false,
			title:'Liste des feuilles XSL/CSS',
			//layout:'border',
			
			items: [					
				{
					xtype:'grid',
					itemId:'listXslt',
					id:'listXslt',
					height:'100%',
					//region:'center',
					store:Ext.create('Desktop.XsltStore'),
					listeners: {
						afterrender:function() {
							this.getStore().reload();
						}
					},
					columns:[
						{
							text: "Nom", dataIndex: 'name', sortable: true,flex: 1
						},
						{
							text: "Type", dataIndex: 'type', sortable: true,flex: 1
						},
						{
							xtype: 'actioncolumn',
							icon: 'resources/images/remove.png',
							tooltip:"supprimer",
							handler: function(view, rowIndex, colIndex, item, e, record, row) {								
								var xslt_id = record.data.id;
								
								$.getJSON('/service/commands',{cmd:'removeXslt',xslt_id:xslt_id}, function(response) {	
									if (response.errorMsg!=null) {
										Ext.Msg.alert("Erreur",response.errorMsg);										
									} else {
										view.up("window").down("#listXslt").getStore().reload();									
									}
								});
								
							}
						}
					]
				}
			] // items
			,dockedItems:{
				xtype: 'toolbar',
				dock: 'bottom',
				items:[
					{	text: 'import',
						listeners: {
							"click":function(button) {
								
								var win = Ext.create('Desktop.ImportXsltModal');		
								win.app = this.app;
								win.show();
							}
						}
					}
					,{	text: 'export', listeners:{click:function() { this.up("window").exportXslt(); }}}
					]
				
			}
			//,buttons: [{	text: 'import'},{	text: 'export'}]
			
		}
		]
	});