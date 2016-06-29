

Ext.define('Desktop.AdminDBManager', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'admin-db-win',

    init : function(){
        this.launcher = {
            text: 'Gestion des bases',
            iconCls:'icon-grid',
            idmenu:"admin"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
       
           var win = desktop.createWindow({
                //itemId: 'grid-win',
                title:'Bases',
                width:$(window).width()*0.3,
				y:0,
				x:0,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
                layout: 'fit',
                removeProject:function(record) {
        			Ext.MessageBox.show({
        				title: 'Suppression de Base',
        				msg: 'Confirmez vous la suppression de la base '+record.get('name'),
        				buttons: Ext.MessageBox.OKCANCEL,
        				icon: Ext.MessageBox.WARNING,
        				fn: function(btn){
        					if(btn == 'ok'){
        						$.getJSON('/service/commands',{cmd:'removeProject',id:record.data.id}, function(project) {								
        									Ext.getCmp("bases_list").getStore().load();
        								});
        					} else {
        						return;
        					}
        				}
        			});
        		},
                tbar:[
						{
							text:'Nouvelle base',
							listeners:{										
									'click':function(button) {										
										var win = Ext.create('Desktop.NewBase');
										//win.grid = button.up("window").down("grid");
										win.show();
									}
								}
							}
                     ],
                     items:[
                     {
             			xtype:'grid',
             			id:"bases_list",
             			//store:Ext.create('Desktop.ProjectStore'),
             			viewConfig: {
             				getRowClass:function() {
             					return "project_name";
             				}
             			}
             			,
             			columns:[			
             			    {text: "id",dataIndex: 'id',cls:'project_name',menuDisabled:true},     
             				{text: "Base",flex:1,dataIndex: 'name',cls:'project_name',menuDisabled:true},
             				{
	             				xtype:'actioncolumn',
	             				menuDisabled:true,
	             				width:100,
	             				items: [
	             					
		             					
		             				{
		             					icon: 'resources/images/remove.png',
		             					tooltip: 'Suppression',
		             					handler: function(grid, rowIndex, colIndex) {
		             						var rec = grid.getStore().getAt(rowIndex);
		             						this.up("window").removeProject(rec);
		             						
		             					}
		             				}
		             				] // items
	             			} // actioncolumns
             				], // columns
             				listeners : {
                				render:function() {
                					var thegrid = this;
                					thegrid.setLoading(true);
                					
                					
                					var projects = Ext.create('Desktop.ProjectStore');
                					thegrid.setStore(projects);
                					thegrid.setLoading(false);
                					
                				}
                 			} // listeners
             			} // grid
             				
             				//,{text: "#doc",dataIndex: 'nb_docs'}
             			] // items de window
             			
                         });
        
        return win;
    }
});