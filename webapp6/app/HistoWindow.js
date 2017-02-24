


Ext.define('Desktop.HistoWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'histo-win',

    init : function(){
        this.launcher = {
            text: 'Historique',
            iconCls:'icon-grid',
            idmenu:"historique"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var theapp = this.app;
           var win = desktop.createWindow({
                //itemId: 'grid-win',
                title:'Historique',
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
						store : theapp.histoStore,
						//selModel: {selType: 'rowmodel', mode: 'SIMPLE'},
						
						columns: [
							{
								text: "Date", dataIndex: 'timestamp', width:150,sortable: true,
								renderer:function(value, d) {
									var timestamp = d.record.data.timestamp;
									return Ext.Date.format(timestamp, 'd/m/Y H:i:s');
								}
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
										res.push(removeQName(data.params.ref_path));
										res.push(removeQName(data.params.path));											
									} else if (data.from=='fulltext') {
										res.push(data.params.searchTerm);										
									} else if (data.from=='contingence') {
										res.push(removeQName(data.params.current_row_path));	
										res.push(removeQName(data.params.current_col_path));	
										res.push(removeQName(data.params.current_count_path));
										res.push(data.params.order_by);
										res.push(data.params.count_in);
									}  else if (data.from=='tableau brut') {
										
										// peut etre qu'il ne faut pas recréer le columns car on perd sinon le fait l'association avec les cloneNode
										
										for (var i=0;i<data.params.clioxml_columns.length;i++) {	
											var c = getColumn(data.params.clioxml_columns[i].cloneNode);											
											res.push(c.text);
										}
									}
										
										
									
									return res.join("|");
								}
							},
							{	
								text: "filtre", 
								renderer:function(value, d) {	
									var data = d.record.data;
									if (data.params.filtreId!=-1) {
										return data.params.filtreName;
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
					   if (win.itemId=='mod-json-win') {
						  modwin = win;	
						  break;
					   }
				   }
				  
				   if (modwin==null) {
					   
					   var module = new Desktop.ModalitesWindowJson();
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
						modwin.execFromHisto(row.data);
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
						modwin.execFromHisto(row.data);
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
						modwin.execFromHisto(row.data);
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
						},
						{
							text:"enregistrer",
							disabled:true,
							listeners:{
								
								'click':function(button) {
													
								}
							}
						}
						]
            });
        
        return win;
    }
});














