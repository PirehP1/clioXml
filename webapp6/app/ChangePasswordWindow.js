

Ext.define('Desktop.ChangePasswordWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
        
    ],

    id:'admin-password-win',

    init : function(){
        this.launcher = {
            text: 'Mot de passe',
            iconCls:'icon-grid',
            idmenu:"admin"
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
       
           var win = desktop.createWindow({
                layout:'fit',
        width:500,
        height:300,
        closable: true,
        resizable: true,
		modal:true,
		
        //plain: true,
		
        border: true,
		title:"Changement de mot de passe",
		
		
        items: [ 
                 
                 {
				xtype:'form'
				
				
				,frame: false
				
				//,title: 'File Upload Form'
				,bodyPadding: '10 10 0'
				//method="post" enctype="multipart/form-data"
				,defaults: {
					anchor: '100%'
					,allowBlank: false
					,labelAlign: 'top'
				}

				,items: [
				        {
		               	 xtype:'textfield',                	                	 
		            	 flex:1,
		            	 required: true,
		            	 inputType: 'password',
		            	 fieldLabel:"ancien mot de passe",
		            	 itemId:"old_password"
		            	 
            		 },{
		               	 xtype:'textfield',                	                	 
		            	 flex:1,
		            	 required: true,
		            	 inputType: 'password',
		            	 fieldLabel:"nouveau mot de passe",
		            	 itemId:"new_password"
            		 }]

				,buttons: [{
					text: 'Appliquer'
					,formBind: true
					,handler: function(){
						var form = this.up("window").down('form');
						//form.setLoading(true);
						var old_password = form.down("#old_password").getValue();
						var new_password = form.down("#new_password").getValue();
						var me = this;
						$.get("/service/commands?cmd=changePassword",{old_password:old_password,new_password:new_password},function(response) {
							
							if (response.updated) {
								alert("mot de passe modifié");
								me.up("window").close();
							} else {
								alert("Mauvais ancien mot de passe");
							
							}
							
						});	
						
						
						
						return;
						
						
					}
				}
				
				]
			}
		]
                         });
        
        return win;
    }
});