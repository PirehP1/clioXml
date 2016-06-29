
Ext.define('Desktop.ContingenceWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'contingence-win',

    init : function(){
        this.launcher = {
            text: 'Tableau de contingence',
            iconCls:'icon-grid'
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
       
           var win = desktop.createWindow({
              
                title:'Tableau de contingence',
				y:0,
				x:400,
                width:740,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
				layout: 'fit',
						
				
				current_row_path:null,
				current_col_path:null,
				current_count_path:null,	
					    
				reload_contingence:function() {
					var me=this;
					if (this.current_row_path!=null && this.current_col_path!=null && this.current_count_path!=null) {
						var order_by = "modalite";
						var count_in = "absolute";
						//count : 'absolute', 'percent', order_by : 'modalite' or 'marge'
						this.setLoading(true);
						$.getJSON('/service/commands',{cmd:'distinctValues',path:[this.current_row_path,this.current_col_path,this.current_count_path],order_by:order_by,count_in:count_in}, function(result) { //count : 'absolute', 'percent', order_by : 'modalite' or 'marge'
							me.setLoading(false);								
							
							var columns=[{text:"",dataIndex:'modaliteLigne',sortable:false,locked:true,width:100,menuDisabled: true}];
							var fields=['modaliteLigne'];
							for (var i=0;i<result.modaliteColonne.length;i++) {		
								fields.push('col'+i);
								columns.push({text:result.modaliteColonne[i],dataIndex:'col'+i,width:30,componentCls:"headerText",sortable:false,menuDisabled: true});
							}
							var data = [];
							for (var ligne=0;ligne<result.modaliteLigne.length;ligne++) {
								var row = {modaliteLigne:result.modaliteLigne[ligne]};
								for (var colonne=0;colonne<result.modaliteColonne.length;colonne++) {
									row['col'+colonne] = "";
								}
								data.push(row);
							}
							
							for (var i =0;i<result.values.length;i++) {
								var v = result.values[i];
								data[v.row]['col'+v.col] = v.value;
							}
							
							var store = Ext.create('Ext.data.Store',{	                                            			
												fields:fields,
												data:data,
												proxy: {
												  type: 'memory',
												  reader: {
													type: 'json'
													
												  }
												}	
											});
							var c = new Ext.grid.Panel({
														selModel:{
															type:'spreadsheet',
															mode :'MULTI',
															rowNumbererHeaderWidth:0
														},
														plugins: ['bufferedrenderer'],
														store:store,
														columns:columns,
														border:false,
														columnLines:true,
														stripeRows:true
													});	

							var parent = me;
							parent.removeAll();
							parent.add(c);
							parent.doLayout();
							//me.reconfigure(store,columns);
						});
					}
					
				},
				
				listeners: {
					afterrender:function() {
						this.addTool({
						  type:'refresh',
						  handler: function() {
							this.up("window").load_contingence();
						  },
						  scope:this
						});
						
						
						var me = this;
						
						var row_input = this.down('#row_path');								
						new Ext.dd.DropTarget(row_input.getEl(), {								
							 ddGroup:'t2div'
							,notifyDrop:function(dd, e, node) {
								var els = schemaNode_to_array(node);										
								row_input.setValue(getFullPath_from_array(els));
								
								me.current_row_path = getFullPathNS_from_array(els) ; //getFullPath_from_array(els);
								
								me.reload_contingence();
								
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
								
								me.reload_contingence();
								
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
								
								me.reload_contingence();
								
								return true;
							} // eo function notifyDrop
						});
					}
				},
				    
				dockedItems: [
								{
									xtype: 'toolbar',
									dock: 'top',
									items: [
										{text:'toto'},
										{text:'titi'}
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
