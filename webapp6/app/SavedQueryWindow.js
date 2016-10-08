
Ext.define('QueryModel',{ // was book
        extend: 'Ext.data.Model',
		
        fields: [                 
            'type', 'from','timestamp'
			]
        
    });

Ext.define('Desktop.QueryStore',{
	extend: 'Ext.data.Store',	
	proxy: {
		url:"/service/commands?cmd=listQueries",type:"ajax",
		reader: {
			type: 'json',
			root: 'queries'
		}
	},
	autoLoad:true,
	model:'QueryModel'
});

Ext.define('Desktop.SavedQueryWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'saved-query-win',

    init : function(){
        this.launcher = {
            text: 'Requêtes',
            iconCls:'icon-grid',
            idmenu:"query"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var theapp = this.app;
           var win = desktop.createWindow({
                //itemId: 'grid-win',
                title:'Liste requêtes sauvegardées',
                y:$(window).height()*0.60,
                x:0,
                width:$(window).width(),
                height:$(window).height()*.40-48,
                /*
                y:0,				
				x:$(window).width()*0.7,
				width: $(window).width()*0.3,
                height:$(window).height()-48,
                */
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'fit',
				app:theapp,		
				
					items: [
                    {
						current_filtreId:-1,
                        border: false,
                        xtype: 'grid',
                        store:Ext.create('Desktop.QueryStore'),
						//store : theapp.histoStore,
						//selModel: {selType: 'rowmodel', mode: 'SIMPLE'},
						
						columns: [
							{
								text: "Date", dataIndex: 'timestamp', width:150,sortable: true,
								renderer:function(value, d) {
									var timestamp = new Date(d.record.data.timestamp);
									return Ext.Date.format(timestamp, 'd/m/Y H:i:s');
								}
							},   
							{
								text: "nom", dataIndex: 'name', sortable: true
							},
							{
								text: "type", dataIndex: 'type', sortable: true
							},
							{	text: "from",  dataIndex: 'from'
								
							},
							{	
								text: "", flex: 1, 
								renderer:function(value, d) {	
									var data = d.record.data;
									var res=[];
									if (data.from=='modalite') {
										res.push(removeQName(data.params.path));										
									} else if (data.from=='fulltext') {
										var params = JSON.parse(data.params);
										res.push(params.searchTerm);										
									} else if (data.from=='contingence') {
										var params = JSON.parse(data.params);
										res.push(getLastNode(params.current_row_path));	//removeQName
										res.push(getLastNode(params.current_col_path));	
										res.push(getLastNode(params.current_count_path));
										res.push("["+params.order_by+","+params.count_in+"]");
										//res.push(params.count_in);
									}  else if (data.from=='tableau brut') {
										
										// peut etre qu'il ne faut pas recréer le columns car on perd sinon le fait l'association avec les cloneNode
										var params = JSON.parse(data.params);
										for (var i=0;i<params.colonnes.length;i++) {	
											var c = getLastNode(params.colonnes[i]);											
											res.push(c);
										}
									}
										
										
									
									return res.join("|");
								}
							},
							{	
								text: "filtre", 
								renderer:function(value, d) {	
									var data = d.record.data;
									var params = JSON.parse(data.params);
									if (params.filtreId!=-1) {
										return params.filtreName;
									} else {
										return "";
									}
								}
							}
						]
						
                      }
					  
                ],
                execQueryModalite:function(row) {
						// find the modalite window
				   var desktop = theapp.getDesktop();
				   var modwin=null;
				   for (var i=0;i<desktop.windows.getCount();i++) {
					   var win = desktop.windows.get(i);
					   if (win.itemId=='mod-win') {
						  modwin = win;	
						  break;
					   }
				   }
				  
				   if (modwin==null) {
					   
					   var module = new Desktop.ModalitesWindow();
						module.app = theapp;															
						modwin = module.createWindow();
						modwin.show();
						
						
				   } 
				   
				   setTimeout(function() {
						modwin.execFromHisto(row.data);
					},100);
				},
				execQueryFullText: function(row) {
					// find the modalite window
				   var desktop = theapp.getDesktop();
				   var modwin=null;
				   for (var i=0;i<desktop.windows.getCount();i++) {
					   var win = desktop.windows.get(i);
					   if (win.itemId=='fulltext-win') {
						  modwin = win;	
						  break;
					   }
				   }
				  
				   if (modwin==null) {
					   
					   var module = new Desktop.FullTextWindow();
						module.app = theapp;															
						modwin = module.createWindow();
						modwin.show();
						
						
				   } 
				   
				   setTimeout(function() {
					   var params = JSON.parse(row.data.params);
						modwin.execFromHisto({params:params});
					},100);
				},
				execQueryContingence: function(row) {
					// find the modalite window
				   var desktop = theapp.getDesktop();
				   var modwin=null;
				   for (var i=0;i<desktop.windows.getCount();i++) {
					   var win = desktop.windows.get(i);
					   if (win.itemId=='contingence-win') {
						  modwin = win;	
						  break;
					   }
				   }
				  
				   if (modwin==null) {
					   
					   var module = new Desktop.ContingenceWindow();
						module.app = theapp;															
						modwin = module.createWindow();
						modwin.show();
						
						
				   } 
				   
				   setTimeout(function() {
					   var params = JSON.parse(row.data.params);
						modwin.execFromHisto({params:params,filtreId:params.filtreId});
					},100);
				},
				execQueryTableauBrut: function(row) {
					// find the modalite window
				   var desktop = theapp.getDesktop();
				   var modwin=null;
				   for (var i=0;i<desktop.windows.getCount();i++) {
					   var win = desktop.windows.get(i);
					   if (win.itemId=='brut-win') {
						  modwin = win;	
						  break;
					   }
				   }
				  
				   if (modwin==null) {
					   
					   var module = new Desktop.TableauBrutWindow();
						module.app = theapp;															
						modwin = module.createWindow();
						modwin.show();
						
						
				   } 
				   
				   setTimeout(function() {
					   
					    var colonnes=[];
					    var params = JSON.parse(row.data.params);
						for (var i=0;i<params.colonnes.length;i++) {
							var node = getSchemaNodeFromPath(params.colonnes[i]);
							node.data.expanded = false
							var clone_node = node.clone();
							clone_node.data.expanded = false;
							colonnes.push({originalNode:node,cloneNode:clone_node});
						} 
						 var p = {clioxml_columns:colonnes,filtreId:params.filtreId};
						modwin.execFromHisto({params:p});
					},100);
				},
                tbar: [
						{
							text:"executer",							
							listeners:{								
								'click':function(button) {
									var w = button.up("window");
									var userGrid = w.down("grid");
									if (userGrid.getSelectionModel().hasSelection()) {
										   var row = userGrid.getSelectionModel().getSelection()[0];
										   if (row.data.type=="query" && row.data.from=="modalite") {
											   w.execQueryModalite(row);
										   } else if (row.data.type=="query" && row.data.from=="fulltext") {
											   w.execQueryFullText(row);
										   } else if (row.data.type=="query" && row.data.from=="contingence") {
											   w.execQueryContingence(row);
										   } else if (row.data.type=="query" && row.data.from=="tableau brut") {
											   w.execQueryTableauBrut(row);
										   }
										}				
								}
							}
						}
						]
            });
        
        return win;
    }
});














