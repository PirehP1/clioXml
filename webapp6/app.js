/*
 * This file is generated and updated by Sencha Cmd. You can edit this file as
 * needed for your application, but these edits will have to be merged by
 * Sencha Cmd when upgrading.
 */
Ext.application({
    name: 'Desktop',

    //-------------------------------------------------------------------------
    // Most customizations should be made to Desktop.Application. If you need to
    // customize this file, doing so below this section reduces the likelihood
    // of merge conflicts when upgrading to new versions of Sencha Cmd.
    //-------------------------------------------------------------------------

    requires: [
        'Desktop.App',
        'Desktop.AdminApp',
        'Desktop.LoginWindow'
    ],
    init: function() {
    	var me=this;
    	
		$.get("/service/commands?cmd=login&user=&password=",function(response) {
			if ("error" in response && response.error != '') {
				if (response.error=='servermode') {					
					var login = Ext.create('Desktop.LoginWindow');					
					login.show();								
					
				} else {
					alert("xx");
				}
			} else {
				var app = new Desktop.App();
				var win = Ext.create('Desktop.ProjectModal');
				win.modal = true;
				win.app = app;
				win.show();
			}
		});	
		
        
    }
});
