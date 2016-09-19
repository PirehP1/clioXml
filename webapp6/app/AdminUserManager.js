Ext.define('User', {
    extend: 'Ext.data.Model',
    fields: ['id','email', 'firstname','lastname']
});

Ext.define('Desktop.UserStore',{
	extend: 'Ext.data.Store',	
	proxy: {
		url:"/service/commands?cmd=listUsers",type:"ajax",
		reader: {
			type: 'json',
			root: 'users'
		}
	},
	autoLoad:true,
	model:'User'
});


Ext.define('Desktop.AdminUserManager', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'admin-user-win',

    init : function(){
        this.launcher = {
            text: 'Gestion des utilisateurs',
            iconCls:'icon-grid',
            idmenu:"admin"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
       
           var win = desktop.createWindow({
                //itemId: 'grid-win',
                title:'Utilisateurs',
                width:$(window).width()*0.3,
				y:0,
				x:$(window).width()*0.7,
                height:$(window).height()-48,
                iconCls: 'icon-grid',
                animCollapse:false,
                constrainHeader:true,
                layout: 'fit',
                tbar:[
						{
							text:'Nouveau',
							listeners:{										
									'click':function(button) {
										
										var win = Ext.create('Desktop.NewUser');
										win.grid = button.up("window").down("grid");
										win.show();
									}
								}
							}
                       ],
                items:[
                       
                       	{
                       		xtype:'grid',
                       		columns:[		
                       		      //{text: "id",flex:1,dataIndex: 'id',menuDisabled:true},
                       		      {text: "identifiant",flex:1,dataIndex: 'email',menuDisabled:true},
	                       		   {text: "nom",flex:1,dataIndex: 'firstname',menuDisabled:true},
	                       		   {text: "prénom",flex:1,dataIndex: 'lastname',menuDisabled:true},
	                       		{text: "R/W",xtype: 'checkcolumn',menuDisabled:true,
	                       			   renderer:function(value, metaData, record, rowIndex, colIndex, store) {
	                       				   if (store.getAt(rowIndex).data['credential']!=null && store.getAt(rowIndex).data['credential'].readwrite) {
	                       			        return '<input type="checkbox" checked="yes" disabled="true" />';
	                       				   } else {
	                       					 return '<input type="checkbox" disabled="true"/>'; 
	                       				   }
	                       			        
	                       			    
	                       			}
	                       		},
	                       		{text: "admin",xtype: 'checkcolumn',menuDisabled:true,
	                       			   renderer:function(value, metaData, record, rowIndex, colIndex, store) {
	                       				   if (store.getAt(rowIndex).data['credential']!=null && store.getAt(rowIndex).data['credential'].admin_projet) {
	                       			        return '<input type="checkbox" checked="yes" disabled="true" />';
	                       				   } else {
	                       					 return '<input type="checkbox" disabled="true"/>'; 
	                       				   }
	                       			        
	                       			    
	                       			}
	                       		},
	                       		
	                       		{text: "Base",
	                       			   renderer:function(value, metaData, record, rowIndex, colIndex, store) {
	                       				   var pu="";
	                       				   if (store.getAt(rowIndex).data['credential']!=null && store.getAt(rowIndex).data['credential'].projet_unique>0) {
	                       			        pu = store.getAt(rowIndex).data['credential'].projet_unique;
	                       				   } 
	                       				return pu;
	                       				   
	                       			        
	                       			    
	                       			}
	                       		},
	                       		{
	                   				xtype:'actioncolumn',
	                   				menuDisabled:true,
	                   				width:60,
	                   				items: [
										{
											icon: 'resources/images/edit-16.png',  
											tooltip: 'Edition',
											handler: function(grid, rowIndex, colIndex) {
												console.log("edit");
												var win = Ext.create('Desktop.NewUser');
												win.grid = grid;
												win.user = grid.getStore().getAt(rowIndex);
												win.show();
											}
										}
									,{
										icon: 'resources/images/remove.png',
										tooltip: 'Suppression',
										handler: function(grid, rowIndex, colIndex,item, e, record, row) {
											var msg="Confirmez vous la suppression";
											  Ext.MessageBox.show({
												title: 'Suppression',
												msg:msg,
												buttons: Ext.MessageBox.OKCANCEL,
												icon: Ext.MessageBox.WARNING,
												fn: function(btn){
													if(btn == 'ok'){
														$.get("/service/commands?cmd=removeUser",{id:record.get("id")},function(response) {
				    										
				    										if ("error" in response && response.error != '') {											
				    											alert("erreur",response.error);
				    										} else {
				    											//todo reload users list
				    											grid.getStore().reload();
				    											
				    											
				    										}
				    									});	
													} else {
														return;
													}
												}
											  });
											
										}   
									}
	                   				] // items de actioncolumn
	                       		}
                       		], // grid columns 
                       		listeners : {
                        		render:function() {
                					var thegrid = this;
                					thegrid.setLoading(true);
                					    					
                					var users = Ext.create('Desktop.UserStore');
                					thegrid.setStore(users);
                					thegrid.setLoading(false);
                					
                				}
                        	} // listeners de grid
                       	}
                ], // items
            	
                });
        
        return win;
    }
});