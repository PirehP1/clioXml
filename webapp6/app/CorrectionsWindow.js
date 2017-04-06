
Ext.define('CorrectionModel',{ // was book
        extend: 'Ext.data.Model',
		
        fields: [                 
            'id', 'path','oldValue','newValue','nb_applicable','nb_applique','min_value','max_value','datatype' // todo : add nb_applique, nb_applicable, 
			]

    });

Ext.define('Desktop.CorrectionStore',{
	extend: 'Ext.data.Store',	
	proxy: {
		url:"/service/commands?cmd=listCorrections",type:"ajax",
		reader: {
			type: 'json',
			root: 'corrections'
		}
	},
	autoLoad:true,
	model:'CorrectionModel'
});

Ext.define('Desktop.CorrectionsWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'corrections-win',

    init : function(){
        this.launcher = {
            text: 'Corrections',
            iconCls:'icon-grid',
            idmenu:"modifier"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var theapp = this.app;
           var win = desktop.createWindow({
                //itemId: 'grid-win',
                title:'Liste des corrections',
                /*
                y:0,
				x:$(window).width()*0.3,
                width:$(window).width()*0.4,
                height:$(window).height()-48,
                */
                y:0,				
				x:$(window).width()*0.7,
				width: $(window).width()*0.3,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'fit',
				app:theapp,		
				refresh:function() {
					this.down("grid").getStore().reload();
				},
				listeners: {
					afterrender:function() {	
						var grid = this.down("grid");
						var dd = new Ext.dd.DropTarget(this.getEl(), {
							// must be same as for tree
							 ddGroup:'modaliteDrop'															
							,notifyDrop:function(source, e, data) {	
								// ask for new value
								
								//console.log("drop of data=",data);
								//TODO faire comme codageWindow2 !!!!
								// recup de current_path
								
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
								var modalites=[];
								
								//console.log("nb records = "+data.records.length);
								for (var i=0;i<data.records.length;i++) {
									var node = data.records[i];
									modalites.push(node.data.modalite);
									/*
									var els = schemaNode_to_array(node);								
									var xx = getFullPath_from_array(els);
									console.log("xx=",xx);
									*/
								}
								//console.log("modalites=",modalites);
								var me = this;
								var nb_applicable;
								var callback = function (btn, text){
									if (btn != 'ok' || text=="") {
										grid.setLoading(false);
										return;
									}
									//console.log("nouvelle valeur :",text);
									// todo GET pour valider 
									$.get("/service/commands",{cmd:'addCorrection',nbApplicable:nb_applicable,path:current_path,modalites:modalites,newValue:text},function(result) {
										if (result.erreur!=null) {
											
											Ext.Msg.alert(result.erreur);
												
										} else {
											
											grid.getStore().reload();
											grid.up("window").app.fireEvent("codageUpdated",this);
											//console.log("TODO reload list corrections");
										}
										grid.setLoading(false);
									});
									
									
								};
								
								grid.setLoading(true);															
								// todo ge
								
								$.get("/service/commands",{cmd:'getCorrectionNbApplicable',path:current_path,modalites:modalites},function(result) {
									if (result.erreur!=null) {
										
										Ext.Msg.alert(result.erreur);
										grid.setLoading(false);	
									} else {
										nb_applicable = result.nb_applicable;
										Ext.MessageBox.prompt('Nouvelles corrections', 'Correction ('+nb_applicable+'):', callback,this,false,"");
									}
								});
								
								
								return true;
							} // eo function notifyDrop
						});
						
						
					} // afterRender
				}, // listeners
				
				 	tbar: [
						{
							text:"Rafraichir stats",
							listeners:{									
								'click':function(button) {
									var grid = button.up("window").down("grid");
									grid.setLoading(true);
									$.get("/service/commands",{cmd:'refreshStatCorrections'},function(result) {
										grid.setLoading(false);
										grid.getStore().reload();
									});
								}
							}
						},
						{
							text:"undo",
							listeners:{									
								'click':function(button) {
									var grid = button.up("window").down("grid");
								
									if (grid.getSelectionModel().hasSelection()) {
										   var row = grid.getSelectionModel().getSelection()[0];
										   console.log(row.data);
										   grid.setLoading(true);
										   $.get("/service/commands",{cmd:'undoCorrection',id:row.data.id},function(result) {
											   if (result.erreur!=null) {													
													Ext.Msg.alert(result.erreur);
													console.log("erreur=",result);
												} else {
													grid.getStore().reload();
													grid.up("window").app.fireEvent("codageUpdated",this);
												}
											   grid.setLoading(false);	
											});
										   
										   /*
										   grid.setLoading(true);
											$.get("/service/commands",{cmd:'undoCorrection'},function(result) {
												grid.setLoading(false);
												grid.getStore().reload();
											});
											*/
									}	
									
									
								}
							}
						},
						{
							text:"relancer",
							listeners:{									
								'click':function(button) {
									var grid = button.up("window").down("grid");
								
									if (grid.getSelectionModel().hasSelection()) {
										   var row = grid.getSelectionModel().getSelection()[0];
										   
										   grid.setLoading(true);
										   $.get("/service/commands",{cmd:'reApplyCorrection',id:row.data.id},function(result) {
											   if (result.erreur!=null) {													
													Ext.Msg.alert(result.erreur);
													console.log("erreur=",result);
												} else {
													grid.getStore().reload();
													grid.up("window").app.fireEvent("codageUpdated",this);
												}
											   grid.setLoading(false);	
											});
										   
										   /*
										   grid.setLoading(true);
											$.get("/service/commands",{cmd:'undoCorrection'},function(result) {
												grid.setLoading(false);
												grid.getStore().reload();
											});
											*/
									}	
									
									
								}
							}
						}
				        ],
					items: [
					        
					 {
						
                        border: false,
                        xtype: 'grid',
                        store:Ext.create('Desktop.CorrectionStore'),
						
						columns: [							   
							{
								text: "variable", dataIndex: 'path',
								renderer : function (value, metadata, record, rowIndex, colIndex, store){
									var path = record.get("path");
									path = removeQName(path);
									var nodes = path.split("/");
									var n=nodes[nodes.length-1];
									if (nodes.length >1) {
										n = nodes[nodes.length-2]+"/"+n;
									}
									
									
									return n;
								}
							},
							{
								text: "Ancienne Valeur", dataIndex: 'oldValue',
								renderer : function (value, metadata, record, rowIndex, colIndex, store){
									var datatype = record.get("datatype");
									if (datatype=="string") return record.get("oldValue");
									else return "["+record.get("min_value")+";"+record.get("max_value")+"[";
									
								}
							},
							{	text: "Nouvelle Valeur",  dataIndex: 'newValue'
								
							},
							{	text: "applicable",  dataIndex: 'nb_applicable'
								
							},
							{	text: "applique",  dataIndex: 'nb_applique'
								
							}

						]
						
                      }
					  
                ],
                
                
            });
        
        return win;
    }
});














