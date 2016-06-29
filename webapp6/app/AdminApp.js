/*!
 * Ext JS Library
 * Copyright(c) 2006-2014 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */


		
Ext.define('Desktop.AdminApp', {
    extend: 'Ext.ux.desktop.App',
	
    requires: [        
        'Desktop.AdminDBManager',
        'Desktop.AdminUserManager',
        'Desktop.NewUser',
        'Desktop.NewBase',
        'Desktop.ChangePasswordWindow'
    ],

    init: function() {
        // custom logic before getXYZ methods get called...

        this.callParent();

        // now ready...
		//Ext.Msg.alert("coucou");
		
		/*		
		var win = Ext.create('Desktop.AdminDBManager');
		win.modal = true;
		win.app = this;
		win.show();
		*/
        
        var module = this.getModule('admin-db-win');
		var w = module.createWindow();												
		w.x = 0;
		w.y = 0;						
		w.show();	
		
		
		var w2 = this.getModule('admin-user-win').createWindow();
		w2.show();
		
    },
    
   
    getModules : function(){
        return [           
			new Desktop.AdminDBManager(),
			new Desktop.AdminUserManager(),
			new Desktop.ChangePasswordWindow()
			
        ];
    },

    getDesktopConfig: function () {
        var me = this, ret = me.callParent();

        return Ext.apply(ret, {
            
            wallpaper: 'resources/images/wallpapers/desk.jpg',
            wallpaperStretch: false
        });
    },

    // config for the start menu
    getStartConfig : function() {
        var me = this, ret = me.callParent();

        return Ext.apply(ret, {
            title: 'ClioXML',
			id:'startWindow',
            iconCls: 'user',
            height: 300,
            toolConfig: {
                width: 100,
                items: [
					
                    {
                        text:'Déconnexion',
                        iconCls:'settings',
                        handler: me.onCloseProject,
                        scope: me
                    }
					
                ]
            }
        });
    },

    getTaskbarConfig: function () {
        var ret = this.callParent();

        return Ext.apply(ret, {
		
			alwaysOnTop:true,
            trayItems: [
                { html:'ClioXML' }
            ]
        });
    },

    onLogout: function () {
        Ext.Msg.confirm('Logout', 'Are you sure you want to logout?');
    },
	
    onCloseProject: function () {
        Ext.MessageBox.show({
				title: 'Déconnexion',
				msg: 'Confirmez vous la déconnexion ? ',
				buttons: Ext.MessageBox.OKCANCEL,
				icon: Ext.MessageBox.WARNING,
				fn: function(btn){
					if(btn == 'ok'){
						location.reload(true);
					} else {
						return;
					}
				}
			});
    }
});
